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
import static uk.ac.ox.poseidon.core.providers.constant.Factories.always;
import static uk.ac.ox.poseidon.fishing.choices.Factories.greedyPicker;

import ec.util.MersenneTwisterFast;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;

class GreedyPickerFactoryTest {

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

  private static Simulation simulation() {
    final Simulation simulation = mock(Simulation.class);
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    simulation.random = new MersenneTwisterFast(0);
    return simulation;
  }

  private static Vessel newVessel() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getProductStore()).thenReturn(new ProductStore());
    return vessel;
  }

  private static final class OptionValuesFactory
    extends GlobalScopeFactory<OptionValues<String>>
  {

    @Override
    protected OptionValues<String> newInstance(final Scope scope) {
      return new ImmutableOptionValues<>(Map.of("A", 1.0, "B", 3.0));
    }
  }

  @Test
  void givesOnePickerPerVessel() {
    final GreedyPickerFactory<String> factory = greedyPicker(
      new OptionValuesFactory(),
      always(true)
    );

    assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
    assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
    assertThat(factory.get(scopeA).apply(vesselA)).isEqualTo("B");
  }
}
