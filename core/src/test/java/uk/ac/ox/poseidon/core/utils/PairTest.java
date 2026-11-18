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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

class PairTest {

  @Test
  void ofHoldsBothValues() {
    final Pair<String, Integer> pair = Pair.of("a", 1);

    assertThat(pair.getFirst()).isEqualTo("a");
    assertThat(pair.getSecond()).isEqualTo(1);
  }

  @Test
  void equalPairsAreEqual() {
    assertThat(Pair.of("a", 1)).isEqualTo(Pair.of("a", 1));
    assertThat(Pair.of("a", 1)).hasSameHashCodeAs(Pair.of("a", 1));
  }

  @Test
  void differingPairsAreNotEqual() {
    final Pair<String, Integer> pair = Pair.of("a", 1);

    assertThat(pair).isNotEqualTo(Pair.of("b", 1));
    assertThat(pair).isNotEqualTo(Pair.of("a", 2));
  }

  @Test
  void factoryPairsTheValuesOfItsInputs() {
    final PairFactory<Scope, String, Integer> factory = PairFactory.<
      Scope,
      String,
      Integer
    >builder()
      .first(new Value<>("a"))
      .second(new Value<>(1))
      .build();

    assertThat(factory.get(GLOBAL_SCOPE)).isEqualTo(Pair.of("a", 1));
  }

  @Getter
  @AllArgsConstructor
  @SuperBuilder(toBuilder = true)
  private static final class Value<R> extends GlobalScopeFactory<R> {

    private final R value;

    @Override
    protected R newInstance(final Scope scope) {
      return value;
    }
  }
}
