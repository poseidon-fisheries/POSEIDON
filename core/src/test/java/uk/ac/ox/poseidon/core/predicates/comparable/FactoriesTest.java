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

package uk.ac.ox.poseidon.core.predicates.comparable;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.between;
import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.greaterThan;
import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.lessThan;

import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

class FactoriesTest {

  private final Factory<Scope, Double> thresholdFactory = scope -> 1.5;
  private final ValueFactory<Scope, Object, Double> thresholdValue = scope ->
    context ->
      1.5;
  private final ValueFactory<Scope, Object, Double> minimum = scope ->
    context ->
      1.0;
  private final ValueFactory<Scope, Object, Double> maximum = scope ->
    context ->
      2.0;

  @Test
  void lessThanADoubleIsTrueBelowTheThreshold() {
    assertLessThan(lessThan(1.5).get(GLOBAL_SCOPE));
  }

  @Test
  void lessThanAConstantFactoryIsTrueBelowTheThreshold() {
    assertLessThan(lessThan(thresholdFactory).get(GLOBAL_SCOPE));
  }

  @Test
  void lessThanAValueFactoryIsTrueBelowTheThreshold() {
    assertLessThan(lessThan(thresholdValue).get(GLOBAL_SCOPE));
  }

  @Test
  void greaterThanADoubleIsTrueAboveTheThreshold() {
    assertGreaterThan(greaterThan(1.5).get(GLOBAL_SCOPE));
  }

  @Test
  void greaterThanAConstantFactoryIsTrueAboveTheThreshold() {
    assertGreaterThan(greaterThan(thresholdFactory).get(GLOBAL_SCOPE));
  }

  @Test
  void greaterThanAValueFactoryIsTrueAboveTheThreshold() {
    assertGreaterThan(greaterThan(thresholdValue).get(GLOBAL_SCOPE));
  }

  @Test
  void betweenTwoDoublesIncludesBothBounds() {
    final BiFunction<Object, Comparable<Double>, Boolean> predicate = between(
      1.0,
      2.0
    ).get(GLOBAL_SCOPE);

    assertThat(predicate.apply(null, 1.0)).isTrue();
    assertThat(predicate.apply(null, 2.0)).isTrue();
    assertThat(predicate.apply(null, 0.9)).isFalse();
    assertThat(predicate.apply(null, 2.1)).isFalse();
  }

  @Test
  void betweenTwoDoublesCanExcludeItsBounds() {
    final BiFunction<Object, Comparable<Double>, Boolean> predicate = between(
      1.0,
      2.0,
      false,
      false
    ).get(GLOBAL_SCOPE);

    assertThat(predicate.apply(null, 1.0)).isFalse();
    assertThat(predicate.apply(null, 2.0)).isFalse();
    assertThat(predicate.apply(null, 1.5)).isTrue();
  }

  @Test
  void betweenTwoValueFactoriesIncludesBothBounds() {
    final BiFunction<Object, Comparable<Double>, Boolean> predicate = between(
      minimum,
      maximum
    ).get(GLOBAL_SCOPE);

    assertThat(predicate.apply(null, 1.0)).isTrue();
    assertThat(predicate.apply(null, 2.0)).isTrue();
    assertThat(predicate.apply(null, 0.9)).isFalse();
    assertThat(predicate.apply(null, 2.1)).isFalse();
  }

  @Test
  void betweenTwoValueFactoriesCanExcludeItsBounds() {
    final BiFunction<Object, Comparable<Double>, Boolean> predicate = between(
      minimum,
      maximum,
      false,
      false
    ).get(GLOBAL_SCOPE);

    assertThat(predicate.apply(null, 1.0)).isFalse();
    assertThat(predicate.apply(null, 2.0)).isFalse();
    assertThat(predicate.apply(null, 1.5)).isTrue();
  }

  private static void assertLessThan(
    final BiFunction<Object, Comparable<Double>, Boolean> predicate
  ) {
    assertThat(predicate.apply(null, 1.0)).isTrue();
    assertThat(predicate.apply(null, 1.5)).isFalse();
    assertThat(predicate.apply(null, 2.0)).isFalse();
  }

  private static void assertGreaterThan(
    final BiFunction<Object, Comparable<Double>, Boolean> predicate
  ) {
    assertThat(predicate.apply(null, 2.0)).isTrue();
    assertThat(predicate.apply(null, 1.5)).isFalse();
    assertThat(predicate.apply(null, 1.0)).isFalse();
  }
}
