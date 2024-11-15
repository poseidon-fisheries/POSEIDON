/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import uk.ac.ox.poseidon.agents.behaviours.BehaviourFactory;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;
import uk.ac.ox.poseidon.core.utils.IdSupplier;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VesselFactory implements Factory<Vessel> {

    @NonNull private BehaviourFactory<?> initialBehaviour;
    @NonNull private Factory<? extends IdSupplier> idSupplier;
    @NonNull private Factory<? extends VesselField> vesselField;
    @NonNull private Factory<? extends Port> portFactory;
    @NonNull private Factory<? extends PortGrid> portGrid;
    private double speed;

    @Override
    public final Vessel get(final Simulation simulation) {
        final VesselField vesselField = this.vesselField.get(simulation);
        final var vessel = new Vessel(
            idSupplier.get(simulation).nextId(),
            portGrid.get(simulation),
            portFactory.get(simulation),
            speed,
            vesselField,
            new ForwardingEventManager(simulation.getEventManager())
        );
        vesselField
            .setCell(
                vessel,
                portGrid.get(simulation).getLocation(vessel.getHomePort())
            );
        vessel.setInitialBehaviour(initialBehaviour.get(simulation, vessel));
        final var schedule = simulation.getTemporalSchedule();
        final var initialAction =
            vessel.getInitialBehaviour().newAction(schedule.getDateTime(), vessel);
        if (initialAction != null) {
            schedule.scheduleOnceIn(initialAction.getDuration(), initialAction);
        }
        return vessel;
    }

}
