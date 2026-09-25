/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.beanutils.PropertyUtils;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.poseidon.agents.vessels.FleetEvent.Type.*;

/**
 * A {@link SimulationScopeFactory} that turns a vessel register table (one row per vessel
 * activation/deactivation/modification event) into a {@link Fleet}, scheduling every row as a
 * {@link FleetEvent} at its own event date. Built directly via its {@code @SuperBuilder}, not
 * from {@code Factories}, given how many optional columns/mappings it takes.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FleetFromVesselRegisterFactory extends SimulationScopeFactory<Fleet> {

    /** The vessel register table. */
    private Factory<? super SimulationScope, ? extends Table> data;
    /** The fleet every row's event is applied to. */
    private Factory<? super SimulationScope, ? extends Fleet> fleet;

    /** Column giving each row's vessel id. */
    @Builder.Default private String vesselIdColumn = "cfr";
    /** Column giving each row's vessel name. */
    @Builder.Default private String vesselNameColumn = "name_of_vessel";
    /** Column giving each row's home port code. */
    @Builder.Default private String portCodeColumn = "place_of_registration";
    /** Column giving each row's event code. */
    @Builder.Default private String eventCodeColumn = "event";
    /** Column giving each row's event date. */
    @Builder.Default private String eventDateColumn = "event_start_date";
    /** Columns to exclude from a row's tags, on top of the other named columns. */
    @Builder.Default private List<String> ignoredColumns =
        List.of("event_end_date");
    /** Event codes that map to {@link FleetEvent.Type#ACTIVATION}. */
    @Builder.Default private List<String> activationEventCodes =
        List.of("CEN", "CST", "IMP", "CHA");
    /** Event codes that map to {@link FleetEvent.Type#DEACTIVATION}. */
    @Builder.Default private List<String> deactivationEventCodes =
        List.of("DES", "EXP", "RET");
    /** Event codes that map to {@link FleetEvent.Type#MODIFICATION}. */
    @Builder.Default private List<String> modificationEventCodes =
        List.of("MOD");

    /** Builds each affected vessel's behaviour. */
    private Factory<? super VesselScope, ? extends Behaviour> behaviour;
    /** Builds each affected vessel's hold. */
    private Factory<? super VesselScope, ? extends Hold> hold;
    /** Builds each affected vessel's gear. */
    private Factory<? super VesselScope, ? extends Gear> gear;
    /** Builds each affected vessel's engine. */
    private Factory<? super VesselScope, ? extends Engine> engine;

    /** Extra per-vessel components to build for each affected vessel. */
    @Singular
    private List<Factory<? super VesselScope, ?>> extraFactories;

    /**
     * Maps a bean property path on this factory to a table column: before resolving
     * {@link #behaviour}/{@link #hold}/{@link #gear}/{@link #engine}/{@link #extraFactories} for
     * a row, each mapped property is set to that row's value in the named column.
     */
    @Singular
    private Map<String, String> dataMappings;

    private void setProperty(
        final String propertyName,
        final Object propertyValue
    ) {
        try {
            PropertyUtils.setProperty(this, propertyName, propertyValue);
        } catch (
            final IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return {@link #fleet}, with one {@link FleetEvent} per row of {@link #data} scheduled at
     * that row's event date
     */
    @Override
    protected Fleet newInstance(final SimulationScope scope) {
        final Fleet fleet = this.fleet.get(scope);
        final List<Entry<LocalDateTime, FleetEvent>> eventByDateTime =
            data.get(scope)
                .stream()
                .map(row -> {
                    final LocalDateTime dateTime = row.getDate(eventDateColumn).atStartOfDay();
                    return entry(
                        dateTime,
                        makeUpdate(scope, dateTime, row, fleet)
                    );
                })
                .toList();
        scope.getSimulation().getTemporalSchedule().scheduleByDateTime(eventByDateTime);
        return fleet;
    }

    <C> Function<Vessel, C> makeFactoryFunction(
        final SimulationScope scope,
        final Map<String, Object> valuesFromRow,
        final ImmutableMap<String, String> mappings,
        final Factory<? super VesselScope, ? extends C> factory
    ) {
        if (factory == null) {
            return __ -> null;
        } else {
            return vessel -> {
                synchronized (this) {
                    mappings.forEach((propertyName, columnName) ->
                        setProperty(propertyName, valuesFromRow.get(columnName))
                    );
                    return factory.get(new VesselScope(scope, vessel));
                }
            };
        }
    }

    private FleetEvent makeUpdate(
        final SimulationScope scope,
        final LocalDateTime dateTime,
        final Row row,
        final Fleet fleet
    ) {
        final ImmutableMap<String, String> dataMappings =
            ImmutableMap.copyOf(this.dataMappings);
        final Map<String, Object> valuesFromRow =
            dataMappings
                .values()
                .stream()
                .distinct()
                .collect(toMap(identity(), row::getObject));
        return new FleetEvent(
            dateTime,
            fleet,
            eventType(row.getString(eventCodeColumn)),
            row.getString(vesselIdColumn),
            row.getString(vesselNameColumn),
            row.getString(portCodeColumn),
            makeTags(row),
            makeFactoryFunction(scope, valuesFromRow, dataMappings, behaviour),
            makeFactoryFunction(scope, valuesFromRow, dataMappings, hold),
            makeFactoryFunction(scope, valuesFromRow, dataMappings, gear),
            makeFactoryFunction(scope, valuesFromRow, dataMappings, engine),
            extraFactories.stream()
                .map(f ->
                    makeFactoryFunction(scope, valuesFromRow, dataMappings, f)
                )
                .collect(toList())
        );
    }

    private Map<String, Object> makeTags(final Row row) {
        final Set<String> ignoredColumns =
            Streams.concat(
                Stream.of(
                    vesselIdColumn,
                    vesselNameColumn,
                    eventCodeColumn,
                    eventDateColumn
                ),
                this.ignoredColumns.stream()
            ).collect(toSet());

        // Populating the map with forEach because Collectors.toMap rejects null values
        final Map<String, Object> tags = new HashMap<>();
        row
            .columnNames()
            .stream()
            .filter(not(ignoredColumns::contains))
            .forEach(columName -> tags.put(columName, row.getObject(columName)));
        return Collections.unmodifiableMap(tags);
    }

    private FleetEvent.Type eventType(final String eventCode) {
        if (activationEventCodes.contains(eventCode))
            return ACTIVATION;
        else if (deactivationEventCodes.contains(eventCode))
            return DEACTIVATION;
        else if (modificationEventCodes.contains(eventCode))
            return MODIFICATION;
        else throw new IllegalArgumentException("Unknown event code: " + eventCode);
    }
}
