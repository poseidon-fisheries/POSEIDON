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
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.utils.Factories.listOf;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.core.utils.Factories.pair;
import static uk.ac.ox.poseidon.core.utils.Factories.runId;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

class FactoriesTest {

  private final Simulation simulation = Scenario.builder()
    .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
    .build()
    .startNewSimulation();
  private final SimulationScope scope = new SimulationScope(simulation);

  @Test
  void listOfFactoriesResolvesEachInOrder() {
    final ListFactory<Scope, Object> factory = listOf(object("a"), object(1));

    assertThat(factory.get(GLOBAL_SCOPE)).containsExactly("a", 1);
  }

  @Test
  void listOfLiteralsWrapsThemAsAList() {
    assertThat(listOf("a", "b").get(GLOBAL_SCOPE)).containsExactly("a", "b");
  }

  @Test
  void pairResolvesBothInputs() {
    assertThat(pair(object("a"), object(1)).get(GLOBAL_SCOPE)).isEqualTo(
      Pair.of("a", 1)
    );
  }

  @Test
  void runIdIsTheSimulationsId() {
    assertThat(runId().get(scope)).isEqualTo(simulation.getId());
  }
}
