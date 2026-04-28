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

package uk.ac.ox.poseidon.agents.vessels;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.agents.tasks.InactiveBehaviour;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class VesselCreator implements Steppable {

    private final EventManager eventManager;
    private final VesselField vesselField;
    private final PortGrid portGrid;
    private final MarketGrid marketGrid;
    private final Supplier<String> vesselIdSupplier;

    private final Factory<? super VesselScope, ? extends String> name;
    private final Factory<? super VesselScope, ? extends Account> account;
    private final Factory<? super VesselScope, ? extends Port> homePort;
    private final Factory<? super VesselScope, ? extends Hold> hold;
    private final Factory<? super VesselScope, ? extends Gear> gear;
    private final Factory<? super VesselScope, ? extends Engine> engine;
    private final Factory<? super VesselScope, ? extends Behaviour> behaviour;
    private final List<Factory<? super VesselScope, ?>> extraFactories;

    private final int numberOfVesselsToCreate;

    @Override
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    public void step(final SimState simState) {
        final Simulation simulation = (Simulation) simState;
        final SimulationScope simulationScope = new SimulationScope(simulation);
        final TemporalSchedule temporalSchedule = simulation.getTemporalSchedule();
        for (int i = 0; i < numberOfVesselsToCreate; i++) {
            final Vessel vessel = new Vessel(
                temporalSchedule,
                new ForwardingEventManager(eventManager),
                InactiveBehaviour.INACTIVE_BEHAVIOUR,
                vesselIdSupplier.get(),
                vesselField,
                portGrid,
                marketGrid
            );
            final VesselScope vesselScope = new VesselScope(simulationScope, vessel);
            vessel.setAccount(account.get(vesselScope));
            vessel.setGear(gear.get(vesselScope));
            vessel.setEngine(engine.get(vesselScope));
            vessel.setBehaviour(behaviour.get(vesselScope));
            vessel.setHold(hold.get(vesselScope));
            vessel.setName(name.get(vesselScope));
            vessel.setHomePort(homePort.get(vesselScope));
            extraFactories.forEach(factory -> factory.get(vesselScope));
            vessel.setRegisteredAsActive(true);
        }
    }
}
