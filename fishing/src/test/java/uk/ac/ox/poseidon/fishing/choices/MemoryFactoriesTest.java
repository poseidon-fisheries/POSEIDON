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
import static uk.ac.ox.poseidon.fishing.choices.Factories.keyedMemory;
import static uk.ac.ox.poseidon.fishing.choices.Factories.keyedMemorySelector;
import static uk.ac.ox.poseidon.fishing.choices.Factories.memory;
import static uk.ac.ox.poseidon.fishing.choices.Factories.memoryBasedOptionValues;

import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;

class MemoryFactoriesTest {

  private final Simulation simulation = simulation();
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
  private final Simulation otherSimulation = simulation();
  private final Vessel vesselInOtherSimulation = newVessel();
  private final VesselScope scopeInOtherSimulation = new VesselScope(
    new SimulationScope(otherSimulation),
    vesselInOtherSimulation
  );

  private static Simulation simulation() {
    final Simulation simulation = mock(Simulation.class);
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    return simulation;
  }

  private static Vessel newVessel() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getProductStore()).thenReturn(new ProductStore());
    return vessel;
  }

  private static final class FirstLetterFactory
    extends GlobalScopeFactory<Function<String, String>>
    implements ValueFactory<Scope, String, String>
  {

    @Override
    protected Function<String, String> newInstance(final Scope scope) {
      return input -> input.substring(0, 1);
    }
  }

  private static final class UpperCaseFactory
    extends GlobalScopeFactory<Function<String, String>>
    implements ValueFactory<Scope, String, String>
  {

    @Override
    protected Function<String, String> newInstance(final Scope scope) {
      return String::toUpperCase;
    }
  }

  private static final class TrawlKeyFactory
    extends GlobalScopeFactory<Function<Vessel, String>>
    implements ValueFactory<Scope, Vessel, String>
  {

    @Override
    protected Function<Vessel, String> newInstance(final Scope scope) {
      return vessel -> "trawl";
    }
  }

  private static final class RecollectionValuationFactory
    extends GlobalScopeFactory<Valuation<String, Double>>
  {

    @Override
    protected Valuation<String, Double> newInstance(final Scope scope) {
      return vessel -> (option, recollection) -> recollection;
    }
  }

  @Test
  void memoryIsOnePerVessel() {
    final MemoryFactory<String, Double> factory = memory();

    assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
    assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
    assertThat(factory.get(scopeA)).isNotSameAs(
      factory.get(scopeInOtherSimulation)
    );
  }

  @Test
  void keyedMemoryIsOnePerVessel() {
    final KeyedMemoryFactory<String, String, Double> factory = keyedMemory();

    assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
    assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
    assertThat(factory.get(scopeA)).isNotSameAs(
      factory.get(scopeInOtherSimulation)
    );
  }

  @Test
  void selectorIsOnePerVesselWhenItsKeyedMemoryIs() {
    final KeyedMemorySelectorFactory<
      VesselScope,
      String,
      String,
      String,
      Double
    > factory = keyedMemorySelector(keyedMemory(), new FirstLetterFactory());

    assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
    assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
  }

  @Test
  void selectorsSharingAPerVesselKeyedMemorySelectFromTheSameMemory() {
    final KeyedMemoryFactory<String, String, Double> keyedMemory =
      keyedMemory();
    final KeyedMemorySelectorFactory<
      VesselScope,
      String,
      String,
      String,
      Double
    > writer = keyedMemorySelector(keyedMemory, new FirstLetterFactory());
    final KeyedMemorySelectorFactory<
      VesselScope,
      String,
      String,
      String,
      Double
    > reader = keyedMemorySelector(keyedMemory, new UpperCaseFactory());

    assertThat(writer.get(scopeA).apply("Trawl")).isSameAs(
      reader.get(scopeA).apply("t")
    );
    assertThat(writer.get(scopeA).apply("Trawl")).isNotSameAs(
      reader.get(scopeB).apply("t")
    );
  }

  @Test
  void separateMemoryFactoriesGiveSeparateMemories() {
    final KeyedMemorySelectorFactory<
      VesselScope,
      String,
      String,
      String,
      Double
    > writer = keyedMemorySelector(keyedMemory(), new FirstLetterFactory());
    final KeyedMemorySelectorFactory<
      VesselScope,
      String,
      String,
      String,
      Double
    > reader = keyedMemorySelector(keyedMemory(), new UpperCaseFactory());

    assertThat(writer.get(scopeA).apply("Trawl")).isNotSameAs(
      reader.get(scopeA).apply("t")
    );
  }

  @Test
  void optionValuesReadTheMemoryTheVesselsSelectorGives() {
    final KeyedMemoryFactory<String, String, Double> keyedMemory =
      keyedMemory();
    final MemoryBasedOptionValuesFactory<String, Double> factory =
      memoryBasedOptionValues(
        keyedMemorySelector(keyedMemory, new TrawlKeyFactory()),
        new RecollectionValuationFactory()
      );
    keyedMemory.get(scopeA).get("trawl").observe("X", 3.0, Double::sum);

    assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
    assertThat(factory.get(scopeA).apply(vesselA).getValue("X")).contains(3.0);
    assertThat(factory.get(scopeB).apply(vesselB).getValue("X")).isEmpty();
  }
}
