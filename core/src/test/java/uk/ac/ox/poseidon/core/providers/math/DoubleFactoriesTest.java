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

package uk.ac.ox.poseidon.core.providers.math;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.providers.math.Factories.maxDouble;
import static uk.ac.ox.poseidon.core.providers.math.Factories.minDouble;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

class DoubleFactoriesTest {

  @ParameterizedTest
  @CsvSource({
    "2, 5, 2, 5",
    "5, 2, 2, 5",
    "3, 3, 3, 3",
    "-7, -2, -7, -2",
    "-2, -7, -7, -2",
    "-3, 4, -3, 4",
    "1.25, 1.5, 1.25, 1.5",
    "NaN, 2, NaN, NaN",
    "2, NaN, NaN, NaN",
    "NaN, NaN, NaN, NaN",
    "-Infinity, Infinity, -Infinity, Infinity",
    "Infinity, -Infinity, -Infinity, Infinity",
    "-0.0, 0.0, -0.0, 0.0",
    "0.0, -0.0, -0.0, 0.0"
  })
  void comparesValuesThroughLiveAndConstantOverloads(
    final double a,
    final double b,
    final Double minimum,
    final Double maximum
  ) {
    assertThat(
      minDouble(constant(a), constant(b)).get(GLOBAL_SCOPE).apply(null)
    ).isEqualTo(minimum);
    assertThat(
      maxDouble(constant(a), constant(b)).get(GLOBAL_SCOPE).apply(null)
    ).isEqualTo(maximum);
    assertThat(
      minDouble(constant(a), b).get(GLOBAL_SCOPE).apply(null)
    ).isEqualTo(minimum);
    assertThat(
      maxDouble(constant(a), b).get(GLOBAL_SCOPE).apply(null)
    ).isEqualTo(maximum);
  }

  @Test
  void readsBothInputsOncePerCallUnderTheGivenContext() {
    final double[] context = { 2, 5 };
    final var readsOfA = new AtomicInteger();
    final var readsOfB = new AtomicInteger();
    final ValueFactory<Scope, double[], Double> a = scope -> givenContext -> {
      assertThat(givenContext).isSameAs(context);
      readsOfA.incrementAndGet();
      return givenContext[0];
    };
    final ValueFactory<Scope, double[], Double> b = scope -> givenContext -> {
      assertThat(givenContext).isSameAs(context);
      readsOfB.incrementAndGet();
      return givenContext[1];
    };
    final var minimum = minDouble(a, b).get(GLOBAL_SCOPE);
    final var maximum = maxDouble(a, b).get(GLOBAL_SCOPE);

    assertThat(readsOfA.get()).isZero();
    assertThat(readsOfB.get()).isZero();
    assertThat(minimum.apply(context)).isEqualTo(2.0);
    assertThat(readsOfA.get()).isEqualTo(1);
    assertThat(readsOfB.get()).isEqualTo(1);
    assertThat(maximum.apply(context)).isEqualTo(5.0);
    assertThat(readsOfA.get()).isEqualTo(2);
    assertThat(readsOfB.get()).isEqualTo(2);

    context[0] = 9;
    context[1] = -4;

    assertThat(minimum.apply(context)).isEqualTo(-4.0);
    assertThat(readsOfA.get()).isEqualTo(3);
    assertThat(readsOfB.get()).isEqualTo(3);
    assertThat(maximum.apply(context)).isEqualTo(9.0);
    assertThat(readsOfA.get()).isEqualTo(4);
    assertThat(readsOfB.get()).isEqualTo(4);
  }
}
