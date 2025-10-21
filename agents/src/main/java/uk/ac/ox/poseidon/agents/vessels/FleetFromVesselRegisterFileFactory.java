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
import uk.ac.ox.poseidon.io.sources.DataSource;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
public class FleetFromVesselRegisterFileFactory extends SimulationScopeFactory<Fleet> {

    // TODO: make sure vessels don't behave when inactive

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
    private Factory<? extends DataSource> dataSource;

    private VesselScopeFactory<? extends Hold> hold;
    @Builder.Default private Map<String, String> holdFactoryMappings = Map.of();
    private VesselScopeFactory<? extends Gear> gear;
    @Builder.Default private Map<String, String> gearFactoryMappings = Map.of();
    private VesselScopeFactory<? extends Engine> engine;
    @Builder.Default private Map<String, String> engineFactoryMappings = Map.of();

    @Override
    protected Fleet newInstance(final Simulation simulation) {
        final Fleet fleet = this.fleet.get(simulation);
        final List<Entry<LocalDateTime, VesselEvent>> eventByDateTime =
            Table.read()
                .csv(dataSource.get(simulation).getReader())
                .stream()
                .map(row ->
                    entry(
                        row.getDate(eventDateColumn).atStartOfDay(),
                        makeEvent(simulation, row, fleet)
                    )
                )
                .toList();
        simulation.getTemporalSchedule().scheduleByDateTime(eventByDateTime);
        return fleet;
    }

    <C> Function<Vessel, C> makeFactoryFunction(
        final Simulation simulation,
        final Row row,
        final Map<String, String> mappings,
        final VesselScopeFactory<? extends C> factory
    ) {
        final Map<String, Object> valuesFromRow =
            mappings
                .entrySet()
                .stream()
                .collect(toMap(
                    Entry::getKey,
                    entry -> row.getObject(entry.getKey())
                ));
        return vessel -> {
            mappings.forEach((columnName, propertyName) ->
                setProperty(factory, propertyName, valuesFromRow.get(columnName))
            );
            return factory.get(simulation, vessel);
        };
    }

    private static <C> void setProperty(
        final VesselScopeFactory<? extends C> factory,
        final String propertyName,
        final Object propertyValue
    ) {
        try {
            PropertyUtils.setProperty(factory, propertyName, propertyValue);
        } catch (
            final IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }

    private VesselEvent makeEvent(
        final Simulation simulation,
        final Row row,
        final Fleet fleet
    ) {
        return new VesselEvent(
            fleet,
            eventType(row.getString(eventCodeColumn)),
            row.getString(vesselIdColumn),
            row.getString(vesselNameColumn),
            row.getString(portCodeColumn),
            makeTags(row),
            makeFactoryFunction(simulation, row, holdFactoryMappings, hold),
            makeFactoryFunction(simulation, row, gearFactoryMappings, gear),
            makeFactoryFunction(simulation, row, engineFactoryMappings, engine)
        );
    }

    private Map<String, Object> makeTags(final Row row) {
        final Set<String> ignoredColumns =
            Streams.concat(
                Stream.of(
                    vesselIdColumn,
                    vesselNameColumn,
                    portCodeColumn,
                    eventCodeColumn,
                    eventDateColumn
                ),
                this.ignoredColumns.stream()
            ).collect(toSet());
        return row
            .columnNames()
            .stream()
            .filter(not(ignoredColumns::contains))
            .collect(toMap(identity(), row::getObject));
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
