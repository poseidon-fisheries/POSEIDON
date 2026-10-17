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

package uk.ac.ox.poseidon.core.predicates;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.predicates.Factories.condition;
import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.greaterThan;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * A condition on a value is a value, and a condition on a mapping a mapping,
 * both asking what they test under the context they are asked under.
 */
class ConditionFactoryTest {

  @Test
  void aConditionOnAValueIsAValue() {
    final ValueFactory<Scope, String, Double> contextLength = scope ->
      context ->
        (double) context.length();
    final Function<String, Boolean> value = condition(
      contextLength,
      greaterThan(2)
    ).get(GLOBAL_SCOPE);
    assertThat(value.apply("abc")).isTrue();
    assertThat(value.apply("ab")).isFalse();
  }

  @Test
  void aConditionOnAMappingIsAMapping() {
    final MappingFactory<Scope, String, Double, Double> plusLength = scope ->
      (context, x) ->
        x + context.length();
    final BiFunction<String, Double, Boolean> mapping = condition(
      plusLength,
      greaterThan(2)
    ).get(GLOBAL_SCOPE);
    assertThat(mapping.apply("a", 1.5)).isTrue();
    assertThat(mapping.apply("a", 0.5)).isFalse();
  }
}
