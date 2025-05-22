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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.money.CurrencyUnit;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.agents.behaviours.BehaviourFactory;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.ERROR;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

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
    private String portCodeColumn;

    private BehaviourFactory<?> initialBehaviour;
    private Factory<? extends VesselField> vesselField;
    private Factory<? extends PortGrid> portGrid;
    private Factory<? extends Quantity<Speed>> speed;
    private String accountCurrencyCode;

    @Override
    protected List<Vessel> newInstance(final Simulation simulation) {
        final VesselField vesselField = this.vesselField.get(simulation);
        final List<Vessel> vessels =
            Table.read().file(path.get(simulation).toFile())
                .stream()
                .flatMap(row -> {
                    final PortGrid portGrid = this.portGrid.get(simulation);
                    return portGrid
                        .getObject(row.getString(portCodeColumn))
                        .map(homePort -> {
                            final var vessel = new Vessel(
                                row.getString(vesselIdColumn),
                                row.getString(vesselNameColumn),
                                portGrid,
                                homePort,
                                speed.get(simulation),
                                new Account(CurrencyUnit.of(accountCurrencyCode)),
                                vesselField,
                                new ForwardingEventManager(simulation.getEventManager())
                            );
                            // TODO: I don't think this is the right place to set and schedule
                            //  behaviours. That should probably be handled by a separate
                            //  behaviour factory that is given a list of vessels and takes care
                            //  of setting that up.
                            final TemporalSchedule temporalSchedule =
                                simulation.getTemporalSchedule();
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
                                    row.getString(portCodeColumn) +
                                    " not found for vessel " +
                                    row.getString(vesselIdColumn) +
                                    "."
                            );
                            return Stream.empty();
                        });
                }).toList();
        final Map<String, List<Vessel>> duplicateVesselIds = vessels
            .stream()
            .collect(groupingBy(Vessel::getId))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(toMap(Entry::getKey, Entry::getValue));
        if (!duplicateVesselIds.isEmpty()) {
            logger.log(
                ERROR,
                "Duplicate vessel ids found: " + duplicateVesselIds
            );
        }
        return vessels;
    }
}
