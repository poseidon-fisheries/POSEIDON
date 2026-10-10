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
package uk.ac.ox.poseidon.agents.travel;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Simulation;

import java.time.Duration;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.travel.Factories.travelCost;
import static uk.ac.ox.poseidon.agents.vessels.Factories.perVessel;
import static uk.ac.ox.poseidon.core.utils.Factories.object;

class TravelCostFactoryTest {

    private final Simulation simulation = mock(Simulation.class);
    private final VesselScope scopeA = scopeOfNewVessel(simulation);
    private final VesselScope scopeB = scopeOfNewVessel(simulation);
    private final Function<Int2D, Route> routeFunction =
        destination -> new Route(destination.x, Duration.ofHours(destination.y));
    private final Function<Route, Money> routeCost =
        route -> Money.of(CurrencyUnit.of("EUR"), route.getDistanceInKm());

    private static VesselScope scopeOfNewVessel(final Simulation simulation) {
        final Vessel vessel = mock(Vessel.class);
        final VesselScope scope = mock(VesselScope.class);
        when(scope.getVessel()).thenReturn(vessel);
        when(scope.getSimulation()).thenReturn(simulation);
        return scope;
    }

    @Test
    void sharesOneTravelCostBetweenVesselsWhenItsInputsAreShared() {
        final TravelCostFactory<VesselScope> factory =
            travelCost(object(routeFunction), object(routeCost));

        assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeB));
    }

    @Test
    void givesOneTravelCostPerVesselWhenAnInputIsPerVessel() {
        final TravelCostFactory<VesselScope> factory =
            travelCost(perVessel(object(routeFunction)), object(routeCost));

        assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
    }
}
