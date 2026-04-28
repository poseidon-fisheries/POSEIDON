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

import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VesselCreatorTest {

    @Test
    void appliesExtraFactoriesAfterCoreFactories() {
        final Simulation simulation = mock(Simulation.class);
        when(simulation.getTemporalSchedule()).thenReturn(mock(TemporalSchedule.class));

        final VesselField vesselField = mock(VesselField.class);
        final PortGrid portGrid = mock(PortGrid.class);
        final MarketGrid marketGrid = mock(MarketGrid.class);
        final Account account = mock(Account.class);
        final Hold hold = mock(Hold.class);
        final Gear gear = mock(Gear.class);
        final Engine engine = mock(Engine.class);
        final Behaviour behaviour = mock(Behaviour.class);
        final EventManager eventManager = mock(EventManager.class);
        final Port port = mock(Port.class);
        when(portGrid.getLocation(port)).thenReturn(new Int2D(1, 2));

        final AtomicInteger id = new AtomicInteger();
        final AtomicInteger extraFactoryCalls = new AtomicInteger();
        final Factory<VesselScope, Object> extraFactory = scope -> {
            final Vessel vessel = scope.getVessel();
            assertThat(vessel.getAccount()).isSameAs(account);
            assertThat(vessel.getHold()).isSameAs(hold);
            assertThat(vessel.getGear()).isSameAs(gear);
            assertThat(vessel.getEngine()).isSameAs(engine);
            assertThat(vessel.getBehaviour()).isSameAs(behaviour);
            assertThat(vessel.getName()).isEqualTo("Vessel " + vessel.getId());
            assertThat(vessel.getHomePort()).isSameAs(port);
            extraFactoryCalls.incrementAndGet();
            return new Object();
        };

        final VesselCreator creator =
            new VesselCreator(
                eventManager,
                vesselField,
                portGrid,
                marketGrid,
                () -> Integer.toString(id.incrementAndGet()),
                scope -> "Vessel " + scope.getVessel().getId(),
                scope -> account,
                scope -> port,
                scope -> hold,
                scope -> gear,
                scope -> engine,
                scope -> behaviour,
                List.of(extraFactory),
                2
            );

        creator.step(simulation);

        assertThat(extraFactoryCalls).hasValue(2);
    }

    @Test
    void createsVesselsWithSeparateLocalEventManagers() {
        final Simulation simulation = mock(Simulation.class);
        when(simulation.getTemporalSchedule()).thenReturn(mock(TemporalSchedule.class));

        final VesselField vesselField = mock(VesselField.class);
        final PortGrid portGrid = mock(PortGrid.class);
        final MarketGrid marketGrid = mock(MarketGrid.class);
        final Account account = mock(Account.class);
        final Hold hold = mock(Hold.class);
        final Gear gear = mock(Gear.class);
        final Engine engine = mock(Engine.class);
        final Behaviour behaviour = mock(Behaviour.class);
        final Port port = mock(Port.class);
        when(portGrid.getLocation(port)).thenReturn(new Int2D(1, 2));

        final AtomicInteger id = new AtomicInteger();
        final List<Vessel> vessels = new ArrayList<>();
        final VesselCreator creator =
            new VesselCreator(
                new SimpleEventManager(),
                vesselField,
                portGrid,
                marketGrid,
                () -> Integer.toString(id.incrementAndGet()),
                scope -> "Vessel " + scope.getVessel().getId(),
                scope -> account,
                scope -> port,
                scope -> hold,
                scope -> gear,
                scope -> engine,
                scope -> behaviour,
                List.of(scope -> {
                    vessels.add(scope.getVessel());
                    return new Object();
                }),
                2
            );

        creator.step(simulation);
        final List<String> receivedEvents = new ArrayList<>();
        vessels
            .getFirst()
            .getEventManager()
            .addListener(new SimpleListener<>(String.class, receivedEvents::add));

        vessels.getLast().getEventManager().broadcast("second");
        assertThat(receivedEvents).isEmpty();

        vessels.getFirst().getEventManager().broadcast("first");
        assertThat(receivedEvents).containsExactly("first");
    }
}
