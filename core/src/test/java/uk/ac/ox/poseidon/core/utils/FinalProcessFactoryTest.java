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

package uk.ac.ox.poseidon.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.utils.Factories.finalProcess;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

class FinalProcessFactoryTest {

  private final Simulation simulation = Scenario.builder()
    .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
    .build()
    .startNewSimulation();

  @Test
  void theBuiltProcessIsRegisteredToRunAtFinish() {
    final List<String> ran = new ArrayList<>();
    final Factory<SimulationScope, Steppable> process = scope ->
      (final SimState simState) -> ran.add("ran");

    finalProcess(process).get(new SimulationScope(simulation));

    assertThat(ran).isEmpty();
    simulation.finish();
    assertThat(ran).containsExactly("ran");
  }

  @Test
  void theFactoryResolvesToTheProcessItself() {
    final Steppable steppable = simState -> {};
    final Factory<SimulationScope, Steppable> process = scope -> steppable;

    assertThat(finalProcess(process).get(new SimulationScope(simulation)))
      .isSameAs(steppable);
  }
}
