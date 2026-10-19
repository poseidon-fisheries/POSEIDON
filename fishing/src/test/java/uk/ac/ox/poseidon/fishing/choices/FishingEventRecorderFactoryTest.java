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

package uk.ac.ox.poseidon.fishing.choices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.fishing.choices.Factories.exponentialMovingAverageOfBuckets;
import static uk.ac.ox.poseidon.fishing.choices.Factories.fishingEventRecorder;
import static uk.ac.ox.poseidon.fishing.choices.Factories.keyedMemory;
import static uk.ac.ox.poseidon.fishing.choices.Factories.keyedMemorySelector;
import static uk.ac.ox.poseidon.fishing.choices.Factories.memoryBasedOptionValues;
import static uk.ac.ox.poseidon.fishing.choices.FishingEventRecorderTest.fishingEvent;
import static uk.ac.ox.poseidon.fishing.vessels.values.attributes.Factories.stringAttribute;

import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class FishingEventRecorderFactoryTest {

  private static final Species SPECIES = new Species("A", null, "A");
  private static final Coordinate COORDINATE = new Coordinate(1.5, 41.5);
  private static final Int2D CELL = new Int2D(3, 4);

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
  private final KeyedMemoryFactory<String, Int2D, Bucket> keyedMemory =
    keyedMemory();
  private final FishingEventRecorderFactory<VesselScope> factory =
    fishingEventRecorder(
      object(modelGrid()),
      keyedMemorySelector(keyedMemory, stringAttribute("gear")),
      exponentialMovingAverageOfBuckets(0.5)
    );

  @BeforeEach
  void giveTheMocksStores() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
  }

  private static Vessel newVessel() {
    final EventManager eventManager = new SimpleEventManager();
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getProductStore()).thenReturn(new ProductStore());
    when(vessel.getEventManager()).thenReturn(eventManager);
    return vessel;
  }

  private static ModelGrid modelGrid() {
    final ModelGrid modelGrid = mock(ModelGrid.class);
    when(modelGrid.toCell(COORDINATE)).thenReturn(CELL);
    return modelGrid;
  }

  private static class PurseSeineKeyFactory
    extends GlobalScopeFactory<Function<Vessel, String>>
    implements ValueFactory<Scope, Vessel, String>
  {

    @Override
    protected Function<Vessel, String> newInstance(final Scope scope) {
      return vessel -> "PS";
    }
  }

  private static class KgOfSpeciesValuationFactory
    extends GlobalScopeFactory<Valuation<Int2D, Bucket>>
  {

    @Override
    protected Valuation<Int2D, Bucket> newInstance(final Scope scope) {
      return vessel -> (cell, bucket) -> bucket.getKg(SPECIES);
    }
  }

  @Test
  void doesNotRegisterTheRecorderOnTheVesselsEventManager() {
    factory.get(scopeA);
    vesselA
      .getEventManager()
      .broadcast(fishingEvent("PS", COORDINATE, Bucket.of(SPECIES, 10)));

    assertThat(keyedMemory.get(scopeA).get("PS").get(CELL)).isEmpty();
  }

  @Test
  void recordsInTheMemoryTheVesselsOptionValuesRead() {
    final MemoryBasedOptionValuesFactory<Int2D, Bucket> optionValues =
      memoryBasedOptionValues(
        keyedMemorySelector(keyedMemory, new PurseSeineKeyFactory()),
        new KgOfSpeciesValuationFactory()
      );

    factory
      .get(scopeA)
      .receive(fishingEvent("PS", COORDINATE, Bucket.of(SPECIES, 10)));

    assertThat(optionValues.get(scopeA).apply(vesselA).getValue(CELL)).contains(
      10.0
    );
    assertThat(
      optionValues.get(scopeB).apply(vesselB).getValue(CELL)
    ).isEmpty();
  }

  @Test
  void givesOneRecorderPerVessel() {
    assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
    assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
  }
}
