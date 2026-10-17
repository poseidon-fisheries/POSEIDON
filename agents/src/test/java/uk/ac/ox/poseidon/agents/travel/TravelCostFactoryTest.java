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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.travel.Factories.travelCost;

import java.time.Duration;
import java.util.function.BiFunction;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;

class TravelCostFactoryTest {

  private final Simulation simulation = mock(Simulation.class);
  private final Vessel vesselA = newVessel();
  private final VesselScope scopeA = new VesselScope(
    new SimulationScope(simulation),
    vesselA
  );
  private final Vessel vesselB = newVessel();
  private final VesselScope scopeB = new VesselScope(
    new SimulationScope(simulation),
    vesselB
  );
  private final MappingFactory<Scope, Object, Int2D, Route> routeFunction =
    new GlobalRouteFunctionFactory();
  private final MappingFactory<Scope, Object, Route, Money> routeCost =
    new GlobalRouteCostFactory();

  private static Route route(final Int2D destination) {
    return new Route(destination.x, Duration.ofHours(destination.y));
  }

  /** A route function shared by every simulation. */
  private static final class GlobalRouteFunctionFactory
    extends GlobalScopeFactory<BiFunction<Object, Int2D, Route>>
    implements MappingFactory<Scope, Object, Int2D, Route>
  {

    @Override
    protected BiFunction<Object, Int2D, Route> newInstance(final Scope scope) {
      return (context, destination) -> route(destination);
    }
  }

  /** A route cost shared by every simulation. */
  private static final class GlobalRouteCostFactory
    extends GlobalScopeFactory<BiFunction<Object, Route, Money>>
    implements MappingFactory<Scope, Object, Route, Money>
  {

    @Override
    protected BiFunction<Object, Route, Money> newInstance(final Scope scope) {
      return (context, route) ->
        Money.of(CurrencyUnit.of("EUR"), route.getDistanceInKm());
    }
  }

  /** A route function built once per vessel. */
  private static final class PerVesselRouteFunctionFactory
    extends VesselScopeFactory<BiFunction<Object, Int2D, Route>>
    implements MappingFactory<VesselScope, Object, Int2D, Route>
  {

    @Override
    protected BiFunction<Object, Int2D, Route> newInstance(
      final VesselScope scope
    ) {
      return (context, destination) -> route(destination);
    }
  }

  @BeforeEach
  void giveTheMocksStores() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
  }

  private static Vessel newVessel() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getProductStore()).thenReturn(new ProductStore());
    return vessel;
  }

  @Test
  void sharesOneTravelCostBetweenVesselsWhenItsInputsAreShared() {
    final TravelCostFactory<VesselScope> factory = travelCost(
      routeFunction,
      routeCost
    );

    assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeB));
  }

  @Test
  void givesOneTravelCostPerVesselWhenAnInputIsPerVessel() {
    final TravelCostFactory<VesselScope> factory = travelCost(
      new PerVesselRouteFunctionFactory(),
      routeCost
    );

    assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
  }
}
