/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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
import uk.ac.ox.poseidon.agents.behaviours.BehaviourFactory;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import javax.measure.Quantity;
import javax.measure.quantity.Speed;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VesselFactory extends SimulationScopeFactory<Vessel> {

    private BehaviourFactory<?> initialBehaviour;
    private String id;
    private String name;
    private Factory<? extends VesselField> vesselField;
    private Factory<? extends Port> homePort;
    private Factory<? extends PortGrid> portGrid;
    private Factory<? extends Quantity<Speed>> speed;
    private String accountCurrencyCode;

    @Override
    protected Vessel newInstance(final Simulation simulation) {
        final VesselField vesselField = this.vesselField.get(simulation);
        final var vessel = new Vessel(
            checkNotNull(id),
            name == null ? "Vessel " + id : name,
            portGrid.get(simulation),
            homePort.get(simulation),
            speed.get(simulation),
            new Account(CurrencyUnit.of(accountCurrencyCode)),
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
