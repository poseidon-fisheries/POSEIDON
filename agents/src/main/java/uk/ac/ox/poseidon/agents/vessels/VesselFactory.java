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
import uk.ac.ox.poseidon.agents.behaviours.BehaviourFactory;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.utils.IdSupplier;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import javax.measure.Quantity;
import javax.measure.quantity.Speed;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VesselFactory implements Factory<Vessel> {

    private BehaviourFactory<?> initialBehaviour;
    private Factory<? extends IdSupplier> idSupplier;
    private Factory<? extends VesselField> vesselField;
    private Factory<? extends Port> homePort;
    private Factory<? extends PortGrid> portGrid;
    private Factory<? extends Quantity<Speed>> speed;

    @Override
    public final Vessel get(final Simulation simulation) {
        final VesselField vesselField = this.vesselField.get(simulation);
        final var vessel = new Vessel(
            idSupplier.get(simulation).nextId(),
            portGrid.get(simulation),
            homePort.get(simulation),
            speed.get(simulation),
            vesselField,
            new ForwardingEventManager(simulation.getEventManager())
        );
        final TemporalSchedule temporalSchedule = simulation.getTemporalSchedule();
        vessel.setCurrentCell(portGrid.get(simulation).getLocation(vessel.getHomePort()));
        vessel.pushBehaviour(initialBehaviour.get(simulation, vessel));
        temporalSchedule.scheduleOnce(__ ->
            vessel.scheduleNextAction(temporalSchedule)
        );
        return vessel;
    }

}
