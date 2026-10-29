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

import static com.google.common.base.Preconditions.checkState;
import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sim.engine.Steppable;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.AttributeChange;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.Exit;

/**
 * Turns a vessel register table, one row per event in a vessel's life, into
 * dated statements about the vessels of a {@link VesselSpecification}. A row
 * whose event code is a creation code creates the vessel, with the row's
 * attributes; a change code changes the vessel's attributes to the row's; an
 * exit code makes the vessel exit. A row's attributes are its values in every
 * column but the vessel id, event code and event date columns and the ignored
 * columns; a missing value is an absent attribute.
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
  /** Column giving each row's vessel id. */
  private final String vesselIdColumn;
  /** Column giving each row's event code. */
  private final String eventCodeColumn;
  /** Column giving each row's event date. */
  private final String eventDateColumn;

  /**
   * Columns left out of a row's attributes, on top of the vessel id, event
   * code and event date columns.
   */
  @Singular
  private final ImmutableList<String> ignoredColumns;

  /** Event codes whose rows create a vessel. */
  @Singular
  private final ImmutableList<String> creationEventCodes;

  /** Event codes whose rows change a vessel's attributes. */
  @Singular
  private final ImmutableList<String> changeEventCodes;

  /** Event codes whose rows make a vessel exit. */
  @Singular
  private final ImmutableList<String> exitEventCodes;

  /**
   * @throws IllegalStateException    if a vessel's rows dated at or before the
   *                                  start do not begin with its creation, or
   *                                  go on after its exit
   * @throws IllegalArgumentException if a row's event code is none of the
   *                                  configured ones
   */
  @Override
  protected List<Entry<LocalDateTime, Steppable>> newInstance(
    final SimulationScope scope
  ) {
    final VesselSpecification specification = vesselSpecification.get(scope);
    final Fleet fleet = specification.getFleet();
    final LocalDateTime start = startingDateTime.get(scope);
    final Table table = data.get(scope);
    final Map<String, Map<String, Object>> attributesAtStart =
      new LinkedHashMap<>();
    final Map<String, LocalDateTime> exitDatesAtStart = new LinkedHashMap<>();
    final List<Entry<LocalDateTime, Steppable>> laterStatements =
      new ArrayList<>();
    IntStream.range(0, table.rowCount())
      .mapToObj(table::row)
      .sorted(comparing(row -> row.getDate(eventDateColumn)))
      .forEach(row -> {
        final String vesselId = row.getString(vesselIdColumn);
        final LocalDateTime date = row.getDate(eventDateColumn).atStartOfDay();
        final String eventCode = row.getString(eventCodeColumn);
        final boolean dueAfterTheStart = date.isAfter(start);
        if (creationEventCodes.contains(eventCode)) {
          final Map<String, Object> attributes = attributeChange(row).values();
          if (dueAfterTheStart) {
            laterStatements.add(
              entry(date, _ -> specification.create(vesselId, attributes))
            );
          } else {
            checkState(
              !attributesAtStart.containsKey(vesselId),
              "Vessel %s already exists",
              vesselId
            );
            checkNotExited(vesselId, exitDatesAtStart);
            attributesAtStart.put(vesselId, attributes);
          }
        } else {
          final VesselStatement statement = statement(row, eventCode);
          if (dueAfterTheStart) {
            laterStatements.add(
              entry(date, _ -> fleet.queue(vesselId, statement))
            );
          } else {
            checkNotExited(vesselId, exitDatesAtStart);
            final Map<String, Object> attributes = attributesAtStart.get(
              vesselId
            );
            checkState(
              attributes != null,
              "Vessel %s does not exist",
              vesselId
            );
            switch (statement) {
              case final AttributeChange change -> attributesAtStart.put(
                vesselId,
                change.applyTo(attributes)
              );
              case final Exit _ -> {
                attributesAtStart.remove(vesselId);
                exitDatesAtStart.put(vesselId, date);
              }
            }
          }
        }
      });
    final List<Entry<LocalDateTime, Steppable>> statements = new ArrayList<>();
    attributesAtStart.forEach((vesselId, attributes) ->
      statements.add(
        entry(start, _ -> specification.create(vesselId, attributes))
      )
    );
    exitDatesAtStart.forEach((vesselId, exitDate) ->
      statements.add(entry(start, _ -> fleet.recordExit(vesselId, exitDate)))
    );
    statements.addAll(laterStatements);
    return statements;
  }

  /**
   * @return the statement of {@code row}, whose event code is not a creation
   *         code
   * @throws IllegalArgumentException if {@code eventCode} is none of the
   *                                  configured ones
   */
  private VesselStatement statement(final Row row, final String eventCode) {
    final VesselStatement statement;
    if (changeEventCodes.contains(eventCode)) {
      statement = attributeChange(row);
    } else if (exitEventCodes.contains(eventCode)) {
      statement = new Exit();
    } else {
      throw new IllegalArgumentException("Unknown event code: " + eventCode);
    }
    return statement;
  }

  /**
   * @return the change to the attributes {@code row} gives: its values in
   *         every column but those left out of the attributes, a missing value
   *         making the attribute absent
   */
  private AttributeChange attributeChange(final Row row) {
    final Set<String> leftOutColumns = new HashSet<>(ignoredColumns);
    leftOutColumns.addAll(
      List.of(vesselIdColumn, eventCodeColumn, eventDateColumn)
    );
    final ImmutableMap.Builder<String, Object> values = ImmutableMap.builder();
    final ImmutableSet.Builder<String> absentAttributes =
      ImmutableSet.builder();
    row
      .columnNames()
      .stream()
      .filter(not(leftOutColumns::contains))
      .forEach(column -> {
        if (row.isMissing(column)) absentAttributes.add(column);
        else values.put(column, row.getObject(column));
      });
    return new AttributeChange(values.build(), absentAttributes.build());
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
