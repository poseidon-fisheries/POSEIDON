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
import uk.ac.ox.poseidon.fishing.choices.EventRecorderFactory;
import uk.ac.ox.poseidon.fishing.fuel.FuelStationGridFactory;
import uk.ac.ox.poseidon.fishing.market.BiomassMarketFactory;
import uk.ac.ox.poseidon.fishing.market.MarketGridFactory;
import uk.ac.ox.poseidon.fishing.market.OneBiomassMarketPerPortFactory;
import uk.ac.ox.poseidon.fishing.market.PriceFactory;
import uk.ac.ox.poseidon.fishing.market.PriceUpdatesFromTableFactory;
import uk.ac.ox.poseidon.fishing.market.UniformMarketPricesFactory;
import uk.ac.ox.poseidon.fishing.money.MoneyFactory;
import uk.ac.ox.poseidon.fishing.social.EventSharerFactory;
import uk.ac.ox.poseidon.fishing.tasks.fishing.FishingEventPropertyFactory;
import uk.ac.ox.poseidon.fishing.vessels.gears.FixedBiomassProportionGearFactory;
import uk.ac.ox.poseidon.fishing.vessels.gears.IndexedBiomassCatchabilityGearFactory;
import uk.ac.ox.poseidon.fishing.vessels.values.CurrentPortStayStartDateTimeFactory;
import uk.ac.ox.poseidon.fishing.vessels.values.VesselPropertyFactory;

// In this package, not the module's root, because the test builds a
// VesselComponentScope, whose constructor is package-private on purpose.
final class FactoryProductsTest {

  private static final Set<Class<?>> EXCLUDED = Set.of(
    // The currency code is parsed: a mock's "x" is not a currency.
    BiomassMarketFactory.class,
    MoneyFactory.class,
    OneBiomassMarketPerPortFactory.class,
    PriceFactory.class,
    // The event class is looked up by name: "x" is not an event class.
    EventRecorderFactory.class,
    EventSharerFactory.class,
    // The property path is resolved on a real class: it has no getter x.
    FishingEventPropertyFactory.class,
    VesselPropertyFactory.class,
    // The creation, change and exit codes must differ: a mock gives each "x".
    VesselRegisterFactory.class,
    // The port must be the one a mocked grid holds: it holds none.
    CreateVesselsAtPortFactory.class,
    // The minimum catch threshold must be positive, not a mock's 0.
    FixedBiomassProportionGearFactory.class,
    IndexedBiomassCatchabilityGearFactory.class,
    // The grid is rejected for a mocked field with no width or height.
    FuelStationGridFactory.class,
    MarketGridFactory.class,
    // A mocked currentDateTime returns null into a non-null field.
    CurrentPortStayStartDateTimeFactory.class,
    // The list is collected from a mocked grid's stream, which collects to
    // null.
    PriceUpdatesFromTableFactory.class,
    UniformMarketPricesFactory.class
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
      clazz -> EXCLUDED.contains(clazz)
    );
  }
}
