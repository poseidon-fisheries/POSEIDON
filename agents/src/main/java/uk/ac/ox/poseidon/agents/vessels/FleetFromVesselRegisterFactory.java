/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.vessels;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.poseidon.agents.vessels.FleetEvent.Type.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.beanutils.PropertyUtils;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SchedulingFactory;
import uk.ac.ox.poseidon.core.SchedulingScope;
import uk.ac.ox.poseidon.core.SimulationScope;

/**
 * A {@link SchedulingFactory} that turns a vessel register table (one row
 * per vessel activation/deactivation/modification event) into the
 * {@link Fleet} of a {@link VesselSpecification}, scheduling every row as a
 * {@link FleetEvent} at its own event date. Built
 * directly via its {@code @SuperBuilder}, not from {@code Factories}, given how
 * many optional columns/mappings it takes.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FleetFromVesselRegisterFactory extends SchedulingFactory<Fleet> {

  /** The vessel register table. */
  private Factory<? super SimulationScope, ? extends Table> data;
  /**
   * Creates the vessels; every row's event is applied to its fleet.
   */
  private Factory<
    ? super SimulationScope,
    ? extends VesselSpecification
  > vesselSpecification;

  /** Column giving each row's vessel id. */
  @Builder.Default
  private String vesselIdColumn = "cfr";

  /** Column giving each row's event code. */
  @Builder.Default
  private String eventCodeColumn = "event";

  /** Column giving each row's event date. */
  @Builder.Default
  private String eventDateColumn = "event_start_date";

  /**
   * Columns to exclude from a row's attributes, on top of the vessel id, event
   * code and event date columns.
   */
  @Builder.Default
  private List<String> ignoredColumns = List.of("event_end_date");

  /** Event codes that map to {@link FleetEvent.Type#ACTIVATION}. */
  @Builder.Default
  private List<String> activationEventCodes = List.of(
    "CEN",
    "CST",
    "IMP",
    "CHA"
  );

  /** Event codes that map to {@link FleetEvent.Type#DEACTIVATION}. */
  @Builder.Default
  private List<String> deactivationEventCodes = List.of("DES", "EXP", "RET");

  /** Event codes that map to {@link FleetEvent.Type#MODIFICATION}. */
  @Builder.Default
  private List<String> modificationEventCodes = List.of("MOD");

  /**
   * Maps a bean property path on this factory to a table column: before a
   * row's event builds a vessel's components, each mapped property is set to
   * that row's value in the named column.
   */
  @Singular
  private Map<String, String> dataMappings;

  private Object getProperty(final String propertyName) {
    try {
      return PropertyUtils.getProperty(this, propertyName);
    } catch (
      final IllegalAccessException
      | InvocationTargetException
      | NoSuchMethodException e
    ) {
      throw new RuntimeException(e);
    }
  }

  private void setProperty(
    final String propertyName,
    final Object propertyValue
  ) {
    try {
      PropertyUtils.setProperty(this, propertyName, propertyValue);
    } catch (
      final IllegalAccessException
      | InvocationTargetException
      | NoSuchMethodException e
    ) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return the fleet of {@link #vesselSpecification}, with one
   *         {@link FleetEvent} per row of {@link #data} scheduled at that row's
   *         event date
   */
  @Override
  protected Fleet newInstance(final SchedulingScope scope) {
    final VesselSpecification specification = vesselSpecification.get(scope);
    final List<Entry<LocalDateTime, FleetEvent>> eventByDateTime = data
      .get(scope)
      .stream()
      .map(row -> {
        final LocalDateTime dateTime = row
          .getDate(eventDateColumn)
          .atStartOfDay();
        return entry(dateTime, makeUpdate(scope, dateTime, row, specification));
      })
      .toList();
    scope.getSchedule().scheduleByDateTime(eventByDateTime);
    return specification.getFleet();
  }

  /**
   * Builds with each mapped property set to its value from the row: the
   * values are set on this factory (and the factories it holds) only for the
   * time of the build, then the original values are put back, since other
   * simulations of the same scenario may be using this factory at the same
   * time.
   */
  private <T> T withMappedValues(
    final Map<String, Object> valuesFromRow,
    final ImmutableMap<String, String> mappings,
    final Supplier<T> build
  ) {
    synchronized (this) {
      final Map<String, Object> originalValues = new LinkedHashMap<>();
      mappings
        .keySet()
        .forEach(propertyName ->
          originalValues.put(propertyName, getProperty(propertyName))
        );
      try {
        mappings.forEach((propertyName, columnName) ->
          setProperty(propertyName, valuesFromRow.get(columnName))
        );
        return build.get();
      } finally {
        originalValues.forEach(this::setProperty);
      }
    }
  }

  private FleetEvent makeUpdate(
    final SimulationScope scope,
    final LocalDateTime dateTime,
    final Row row,
    final VesselSpecification specification
  ) {
    final ImmutableMap<String, String> dataMappings = ImmutableMap.copyOf(
      this.dataMappings
    );
    final Map<String, Object> valuesFromRow = dataMappings
      .values()
      .stream()
      .distinct()
      .collect(toMap(identity(), row::getObject));
    final String vesselId = row.getString(vesselIdColumn);
    final Map<String, Object> attributes = makeAttributes(row);
    return new FleetEvent(
      dateTime,
      specification.getFleet(),
      eventType(row.getString(eventCodeColumn)),
      vesselId,
      attributes,
      () ->
        withMappedValues(valuesFromRow, dataMappings, () ->
          specification.create(vesselId, attributes)
        ),
      vessel ->
        withMappedValues(valuesFromRow, dataMappings, () -> {
          final VesselScope vesselScope = new VesselScope(scope, vessel);
          specification
            .listedComponents()
            .forEach(component ->
              vessel.buildComponent(component, vesselScope)
            );
          return vessel;
        })
    );
  }

  /**
   * @return the value of each of the row's columns but those excluded from its
   *         attributes, leaving out missing values
   */
  private Map<String, Object> makeAttributes(final Row row) {
    final Set<String> ignoredColumns = Streams.concat(
      Stream.of(vesselIdColumn, eventCodeColumn, eventDateColumn),
      this.ignoredColumns.stream()
    ).collect(toSet());

    return row
      .columnNames()
      .stream()
      .filter(not(ignoredColumns::contains))
      .filter(not(row::isMissing))
      .collect(toImmutableMap(identity(), row::getObject));
  }

  private FleetEvent.Type eventType(final String eventCode) {
    if (activationEventCodes.contains(eventCode)) return ACTIVATION;
    else if (deactivationEventCodes.contains(eventCode)) return DEACTIVATION;
    else if (modificationEventCodes.contains(eventCode)) return MODIFICATION;
    else throw new IllegalArgumentException("Unknown event code: " + eventCode);
  }
}
