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
import static uk.ac.ox.poseidon.core.utils.Factories.prefixedId;

import java.util.function.BiFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

class PrefixedIdTest {

  private final Simulation first = newSimulation();
  private final Simulation second = newSimulation();

  private static Simulation newSimulation() {
    return Scenario.builder()
      .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
      .build()
      .startNewSimulation();
  }

  @AfterEach
  void finish() {
    first.finish();
    second.finish();
  }

  @Test
  void eachCallGivesTheNextIdWhateverTheContextAndWhateverItMaps() {
    final PrefixedId ids = new PrefixedId("P");

    assertThat(ids.apply(null, null)).isEqualTo("P0");
    assertThat(ids.apply("a", 1)).isEqualTo("P1");
    assertThat(ids.apply(null, 1)).isEqualTo("P2");
  }

  @Test
  void consumersOfOneFactoryShareASequenceWithinASimulation() {
    final PrefixedIdFactory factory = prefixedId("V");
    final SimulationScope scope = new SimulationScope(first);

    final BiFunction<Object, Object, String> one = factory.get(scope);
    final BiFunction<Object, Object, String> other = factory.get(scope);

    assertThat(one.apply(null, "Benoa")).isEqualTo("V0");
    assertThat(other.apply(null, "Kupang")).isEqualTo("V1");
  }

  @Test
  void eachSimulationStartsItsOwnSequence() {
    final PrefixedIdFactory factory = prefixedId("V");

    factory.get(new SimulationScope(first)).apply(null, null);

    assertThat(
      factory.get(new SimulationScope(second)).apply(null, null)
    ).isEqualTo("V0");
  }
}
