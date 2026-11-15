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

  @Test
  void memoryIsOnePerVessel() {
    final MemoryFactory<String, Double> factory = new MemoryFactory<>();

    assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
    assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
    assertThat(factory.get(scopeA)).isNotSameAs(
      factory.get(scopeInOtherSimulation)
    );
  }

  @Test
  void partitionedMemoryIsOnePerVessel() {
    final PartitionedMemoryFactory<String, String, String, Double> factory =
      new PartitionedMemoryFactory<>(new FirstLetterFactory());

    assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
    assertThat(factory.get(scopeA).apply("Trawl")).isNotSameAs(
      factory.get(scopeB).apply("Trawl")
    );
    assertThat(factory.get(scopeA).apply("Trawl")).isNotSameAs(
      factory.get(scopeInOtherSimulation).apply("Trawl")
    );
  }

  @Test
  void separateMemoryFactoriesGiveSeparateMemories() {
    final PartitionedMemoryFactory<String, String, String, Double> writer =
      new PartitionedMemoryFactory<>(new FirstLetterFactory());
    final PartitionedMemoryFactory<String, String, String, Double> reader =
      new PartitionedMemoryFactory<>(new FirstLetterFactory());

    assertThat(writer.get(scopeA).apply("Trawl")).isNotSameAs(
      reader.get(scopeA).apply("Trawl")
    );
  }
}
