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

package uk.ac.ox.poseidon.fishing.vessels;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.testing.FactoryProducts;

final class FactoryProductsTest {

  // Factories whose inputs must be real, not mocks: a currency, an event
  // class or a property path that is parsed, a positive threshold, a model
  // grid that has cells, or a result that is legitimately null.
  private static final Set<String> EXCLUDED = Set.of(
    "BiomassMarketFactory",
    "CurrentPortStayStartDateTimeFactory",
    "EventRecorderFactory",
    "EventSharerFactory",
    "FishingEventPropertyFactory",
    "FixedBiomassProportionGearFactory",
    "FuelStationGridFactory",
    "IndexedBiomassCatchabilityGearFactory",
    "MarketGridFactory",
    "MoneyFactory",
    "OneBiomassMarketPerPortFactory",
    "PriceFactory",
    "PriceUpdatesFromTableFactory",
    "RoutePropertyFactory",
    "UniformMarketPricesFactory",
    "VesselPropertyFactory"
  );

  private final Simulation simulation = mock(Simulation.class);
  private final Vessel vessel = mock(Vessel.class);

  @BeforeEach
  void stubSimulation() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(simulation.getEventManager()).thenReturn(mock(EventManager.class));
    when(vessel.getEventManager()).thenReturn(mock(EventManager.class));
    when(vessel.getProductStore()).thenReturn(new ProductStore());
  }

  @TestFactory
  Stream<DynamicTest> everyFactoryBuildsItsProduct() throws IOException {
    return FactoryProducts.everyFactoryBuildsItsProduct(
      "uk.ac.ox.poseidon.fishing",
      new VesselComponentScope(
        new VesselScope(new SimulationScope(simulation), vessel),
        new ProductStore()
      ),
      clazz -> EXCLUDED.contains(clazz.getSimpleName())
    );
  }
}
