/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.vessels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.agents.behaviours.BehaviourFactory;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import javax.measure.Quantity;
import javax.measure.quantity.Speed;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.ERROR;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VesselsFromFileFactory extends SimulationScopeFactory<List<Vessel>> {

    private static final System.Logger logger =
        System.getLogger(VesselsFromFileFactory.class.getName());

    private Factory<? extends Path> path;
    private String vesselIdColumn;
    private String vesselNameColumn;
    private String portIdColumn;

    private BehaviourFactory<?> initialBehaviour;
    private Factory<? extends VesselField> vesselField;
    private Factory<? extends PortGrid> portGrid;
    private Factory<? extends Quantity<Speed>> speed;

    @Override
    protected List<Vessel> newInstance(final Simulation simulation) {
        final VesselField vesselField = this.vesselField.get(simulation);
        return Table.read().file(path.get(simulation).toFile())
            .stream()
            .flatMap(row -> {
                // TODO: check that vessel id is unique
                final PortGrid portGrid = this.portGrid.get(simulation);
                return Optional
                    .ofNullable(portGrid.getPort(row.getString(portIdColumn)))
                    .map(homePort -> {
                        // checkNotNull(homePort, "Port %s not found", row.getString(portIdColumn));
                        final var vessel = new Vessel(
                            row.getString(vesselIdColumn),
                            portGrid,
                            homePort,
                            speed.get(simulation),
                            vesselField,
                            new ForwardingEventManager(simulation.getEventManager())
                        );
                        // TODO: I don't think this is the right place to set and schedule
                        //  behaviours.
                        //       That should probably be handled by a separate behaviour factory
                        //       that
                        //       is given a list of vessels and takes care of setting that up.
                        final TemporalSchedule temporalSchedule = simulation.getTemporalSchedule();
                        vessel.setCurrentCell(portGrid.getLocation(vessel.getHomePort()));
                        vessel.pushBehaviour(initialBehaviour.get(simulation, vessel));
                        temporalSchedule.scheduleOnce(__ ->
                            vessel.scheduleNextAction(temporalSchedule)
                        );
                        return vessel;
                    })
                    .map(Stream::of)
                    .orElseGet(() -> {
                        logger.log(
                            ERROR,
                            "Home port " +
                                row.getString(portIdColumn) +
                                " not found for vessel " +
                                row.getString(vesselIdColumn) +
                                "."
                        );
                        return Stream.empty();
                    });
            }).toList();
    }
}
