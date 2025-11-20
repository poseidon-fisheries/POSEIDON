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

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import lombok.*;
import org.apache.commons.beanutils.PropertyUtils;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static uk.ac.ox.poseidon.agents.vessels.VesselEvent.Type.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetFromVesselRegisterFactory extends SimulationScopeFactory<Fleet> {

    private Factory<? extends Table> data;

    // TODO: make sure vessels don't behave when inactive
    //   Also think about what happens if initial behaviour changes. Maybe the "initial behaviour"
    //   should be a "root behaviour", that is never part of the stack, so if we change it, it
    //   gets used automatically when we pop down to it. Maybe it should also be optional, so that
    //   inactive vessels have no root behaviours. We would just need to make sure that all the
    //   maintenance tasks (i.e., landings, at least) get handled even if we're deactivating the
    //   vessel. Possibly the `active` flag could just be replaced by `rootBehaviour.isDefined`.

    private Factory<? extends Fleet> fleet;

    @Builder.Default private String vesselIdColumn = "cfr";
    @Builder.Default private String vesselNameColumn = "name_of_vessel";
    @Builder.Default private String portCodeColumn = "place_of_registration";
    @Builder.Default private String eventCodeColumn = "event";
    @Builder.Default private String eventDateColumn = "event_start_date";
    @Builder.Default private List<String> ignoredColumns =
        List.of("event_end_date");
    @Builder.Default private List<String> activationEventCodes =
        List.of("CEN", "CST", "IMP", "CHA");
    @Builder.Default private List<String> deactivationEventCodes =
        List.of("DES", "EXP", "RET");
    @Builder.Default private List<String> modificationEventCodes =
        List.of("MOD");

    private VesselScopeFactory<? extends BehaviorTree<Vessel>> behaviour;
    private VesselScopeFactory<? extends Hold> hold;
    private VesselScopeFactory<? extends Gear> gear;
    private VesselScopeFactory<? extends Engine> engine;

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

    @Override
    protected Fleet newInstance(final Simulation simulation) {
        final Fleet fleet = this.fleet.get(simulation);
        final List<Entry<LocalDateTime, VesselEvent>> eventByDateTime =
            data.get(simulation)
                .stream()
                .map(row -> {
                    final LocalDateTime dateTime = row.getDate(eventDateColumn).atStartOfDay();
                    return entry(
                        dateTime,
                        makeEvent(simulation, dateTime, row, fleet)
                    );
                })
                .toList();
        simulation.getTemporalSchedule().scheduleByDateTime(eventByDateTime);
        return fleet;
    }

    <C> Function<Vessel, C> makeFactoryFunction(
        final Simulation simulation,
        final Map<String, Object> valuesFromRow,
        final ImmutableMap<String, String> mappings,
        final VesselScopeFactory<? extends C> factory
    ) {
        if (factory == null) {
            return __ -> null;
        } else {
            return vessel -> {
                synchronized (this) {
                    mappings.forEach((propertyName, columnName) ->
                        setProperty(propertyName, valuesFromRow.get(columnName))
                    );
                    return factory.get(simulation, vessel);
                }
            };
        }
    }

    private VesselEvent makeEvent(
        final Simulation simulation,
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
        return new VesselEvent(
            dateTime,
            fleet,
            eventType(row.getString(eventCodeColumn)),
            row.getString(vesselIdColumn),
            row.getString(vesselNameColumn),
            row.getString(portCodeColumn),
            makeTags(row),
            makeFactoryFunction(simulation, valuesFromRow, dataMappings, behaviour),
            makeFactoryFunction(simulation, valuesFromRow, dataMappings, hold),
            makeFactoryFunction(simulation, valuesFromRow, dataMappings, gear),
            makeFactoryFunction(simulation, valuesFromRow, dataMappings, engine)
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

    private VesselEvent.Type eventType(final String eventCode) {
        if (activationEventCodes.contains(eventCode))
            return ACTIVATION;
        else if (deactivationEventCodes.contains(eventCode))
            return DEACTIVATION;
        else if (modificationEventCodes.contains(eventCode))
            return MODIFICATION;
        else throw new IllegalArgumentException("Unknown event code: " + eventCode);
    }
}
