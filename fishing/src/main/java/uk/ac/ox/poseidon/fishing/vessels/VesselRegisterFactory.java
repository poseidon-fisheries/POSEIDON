/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.fishing.vessels;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Comparator.comparing;
import static java.util.Map.entry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import sim.engine.Steppable;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.AttributeUpdate;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.Exit;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.HomePortUpdate;

/**
 * Turns a vessel register table, one row per event in a vessel's life, into
 * dated statements about the vessels of a {@link VesselSpecification}. A row
 * whose event code is a creation code creates the vessel, with the row's
 * attributes and home-port code; a change code gives the vessel what differs
 * from its previous row, an attribute update for changed attributes and a
 * home-port update for a changed code, and nothing for what is unchanged; an
 * exit code makes the vessel exit. A row's attributes are its values in the
 * listed attribute columns only; a role column is an attribute only if listed,
 * a missing value is an absent attribute, and a missing home-port code no home
 * port. The home port comes from the home-port column alone, so a listed
 * home-port attribute is the register's value as source data, and can differ
 * from the vessel's actual home port.
 * <p>
 * Each vessel's rows dated at or before the start describe its state at the
 * start, so they are folded into one creation at the start, with their
 * attributes applied in date order; a vessel that exited at or before the
 * start is never created, and its exit is recorded with the fleet at the
 * start. Nothing is scheduled here: pass the result to
 * {@link uk.ac.ox.poseidon.core.schedule.Factories#scheduledByDateTime(Factory)}.
 * Built via {@link Factories#vesselRegister}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class VesselRegisterFactory
  extends SimulationScopeFactory<List<Entry<LocalDateTime, Steppable>>>
{

  /** The vessel register table. */
  private final Factory<? super SimulationScope, ? extends Table> data;
  /** Creates the vessels; its fleet receives the other statements. */
  private final Factory<
    ? super SimulationScope,
    ? extends VesselSpecification
  > vesselSpecification;
  /** The date-time the simulation starts at. */
  private final Factory<
    ? super SimulationScope,
    ? extends LocalDateTime
  > startingDateTime;
  /** What the table's columns mean. */
  private final VesselRegisterColumns columns;
  /** The codes of the rows that create, change and exit vessels. */
  private final VesselRegisterEventCodes eventCodes;

  /**
   * @throws IllegalStateException    if a vessel's rows dated at or before the
   *                                  start do not begin with its creation, or
   *                                  go on after its exit
   * @throws IllegalArgumentException if a row's event code is none of the
   *                                  configured ones, or a column the register
   *                                  names is not in the table
   */
  @Override
  protected List<Entry<LocalDateTime, Steppable>> newInstance(
    final SimulationScope scope
  ) {
    final VesselSpecification specification = vesselSpecification.get(scope);
    final Fleet fleet = specification.getFleet();
    final LocalDateTime start = startingDateTime.get(scope);
    final Table table = data.get(scope);
    columns
      .namedColumns()
      .forEach(column ->
        checkArgument(
          table.containsColumn(column),
          "The vessel register table has no column %s",
          column
        )
      );
    final String eventDateColumn = columns.getEventDateColumn();
    final Map<String, RegisterRow> previousRows = new HashMap<>();
    final Map<String, StartingState> statesAtStart = new LinkedHashMap<>();
    final Map<String, LocalDateTime> exitDatesAtStart = new LinkedHashMap<>();
    final List<Entry<LocalDateTime, Steppable>> laterStatements =
      new ArrayList<>();
    IntStream.range(0, table.rowCount())
      .mapToObj(table::row)
      .sorted(comparing(row -> row.getDate(eventDateColumn)))
      .forEach(row -> {
        final String vesselId = row.getString(columns.getVesselIdColumn());
        final LocalDateTime date = row.getDate(eventDateColumn).atStartOfDay();
        final String eventCode = row.getString(columns.getEventCodeColumn());
        final boolean dueAfterTheStart = date.isAfter(start);
        if (eventCodes.getCreationCodes().contains(eventCode)) {
          final RegisterRow creationRow = registerRow(row);
          previousRows.put(vesselId, creationRow);
          if (dueAfterTheStart) {
            laterStatements.add(
              entry(date, _ ->
                specification.create(
                  vesselId,
                  creationRow.getHomePortCode(),
                  creationRow.getValues()
                )
              )
            );
          } else {
            checkState(
              !statesAtStart.containsKey(vesselId),
              "Vessel %s already exists",
              vesselId
            );
            checkNotExited(vesselId, exitDatesAtStart);
            statesAtStart.put(
              vesselId,
              new StartingState(
                creationRow.getHomePortCode(),
                creationRow.getValues()
              )
            );
          }
        } else if (eventCodes.getChangeCodes().contains(eventCode)) {
          final RegisterRow changeRow = registerRow(row);
          final RegisterRow previousRow = previousRows.getOrDefault(
            vesselId,
            RegisterRow.NOTHING
          );
          previousRows.put(vesselId, changeRow);
          final List<VesselStatement> statements = changeRow.differencesFrom(
            previousRow
          );
          if (dueAfterTheStart) {
            laterStatements.add(
              entry(date, _ -> fleet.queue(vesselId, statements))
            );
          } else {
            checkNotExited(vesselId, exitDatesAtStart);
            final StartingState state = statesAtStart.get(vesselId);
            checkState(state != null, "Vessel %s does not exist", vesselId);
            statesAtStart.put(vesselId, state.after(statements));
          }
        } else if (eventCodes.getExitCodes().contains(eventCode)) {
          if (dueAfterTheStart) {
            laterStatements.add(
              entry(date, _ -> fleet.queue(vesselId, new Exit()))
            );
          } else {
            checkNotExited(vesselId, exitDatesAtStart);
            checkState(
              statesAtStart.remove(vesselId) != null,
              "Vessel %s does not exist",
              vesselId
            );
            exitDatesAtStart.put(vesselId, date);
          }
        } else {
          throw new IllegalArgumentException(
            "Unknown event code: " + eventCode
          );
        }
      });
    final List<Entry<LocalDateTime, Steppable>> statements = new ArrayList<>();
    statesAtStart.forEach((vesselId, state) ->
      statements.add(
        entry(start, _ ->
          specification.create(
            vesselId,
            state.getHomePortCode(),
            state.getAttributes()
          )
        )
      )
    );
    exitDatesAtStart.forEach((vesselId, exitDate) ->
      statements.add(entry(start, _ -> fleet.recordExit(vesselId, exitDate)))
    );
    statements.addAll(laterStatements);
    return statements;
  }

  /**
   * @return what {@code row} says: its home-port code and its values in every
   *         column but those left out of the attributes, a missing value making
   *         the attribute absent
   */
  private RegisterRow registerRow(final Row row) {
    final ImmutableMap.Builder<String, Object> values = ImmutableMap.builder();
    final ImmutableSet.Builder<String> absentAttributes =
      ImmutableSet.builder();
    columns.getAttributeColumns().forEach(column -> {
      if (row.isMissing(column)) absentAttributes.add(column);
      else values.put(column, row.getObject(column));
    });
    return new RegisterRow(
      row.isMissing(columns.getHomePortColumn())
        ? Optional.empty()
        : Optional.of(row.getString(columns.getHomePortColumn())),
      values.build(),
      absentAttributes.build()
    );
  }

  /** What one row of the register says about a vessel. */
  @Value
  private static final class RegisterRow {

    /** A row of a vessel that has none yet: it says nothing. */
    static final RegisterRow NOTHING = new RegisterRow(
      Optional.empty(),
      ImmutableMap.of(),
      ImmutableSet.of()
    );

    /** The row's home-port code, or nothing if it is missing. */
    Optional<String> homePortCode;

    /** The row's attributes that have a value, with it. */
    ImmutableMap<String, Object> values;

    /** The row's attributes that are missing. */
    ImmutableSet<String> absentAttributes;

    /**
     * @param previousRow the vessel's previous row
     * @return the statements this row makes: an attribute update for the
     *         attributes whose value differs from {@code previousRow}, a
     *         missing value counting as a difference, and a home-port update
     *         if the code differs; nothing for what is unchanged
     */
    List<VesselStatement> differencesFrom(final RegisterRow previousRow) {
      final ImmutableMap<String, Object> changedValues = values
        .entrySet()
        .stream()
        .filter(
          entry ->
            !entry.getValue().equals(previousRow.values.get(entry.getKey()))
        )
        .collect(toImmutableMap(Entry::getKey, Entry::getValue));
      final ImmutableSet<String> newlyAbsentAttributes = absentAttributes
        .stream()
        .filter(previousRow.values::containsKey)
        .collect(toImmutableSet());
      final List<VesselStatement> statements = new ArrayList<>();
      if (!changedValues.isEmpty() || !newlyAbsentAttributes.isEmpty()) {
        statements.add(
          new AttributeUpdate(changedValues, newlyAbsentAttributes)
        );
      }
      if (!homePortCode.equals(previousRow.homePortCode)) {
        statements.add(new HomePortUpdate(homePortCode));
      }
      return statements;
    }
  }

  /** A vessel's state at the start, as its register rows give it. */
  @Value
  private static final class StartingState {

    Optional<String> homePortCode;
    Map<String, Object> attributes;

    /** @return this state with {@code statements} applied, in order */
    StartingState after(final List<VesselStatement> statements) {
      Optional<String> code = homePortCode;
      Map<String, Object> newAttributes = attributes;
      for (final VesselStatement statement : statements) {
        switch (statement) {
          case final AttributeUpdate update -> newAttributes = update.applyTo(
            newAttributes
          );
          case final HomePortUpdate update -> code = update.getCode();
          case final Exit _ -> throw new IllegalStateException(
            "A row's differences never include an exit."
          );
        }
      }
      return new StartingState(code, newAttributes);
    }
  }

  /**
   * @throws IllegalStateException if {@code vesselId} is among the vessels that
   *                               exited by the start, naming its exit date
   */
  private static void checkNotExited(
    final String vesselId,
    final Map<String, LocalDateTime> exitDatesAtStart
  ) {
    final LocalDateTime exitDate = exitDatesAtStart.get(vesselId);
    checkState(exitDate == null, "Vessel %s exited on %s", vesselId, exitDate);
  }
}
