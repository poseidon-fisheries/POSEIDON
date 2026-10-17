/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.core.predicates.logical;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.functions.MappingAsValue;
import uk.ac.ox.poseidon.core.functions.ValueAsMapping;

/**
 * A disjunction of mappings, and of values through the adapter and the lift,
 * as the value overload builds it.
 */
class AnyOfTest {

  private static final BiFunction<Object, Object, Boolean> YES = (c, t) -> true;
  private static final BiFunction<Object, Object, Boolean> NO = (c, t) -> false;
  private static final Function<Object, Boolean> YES_VALUE = c -> true;
  private static final Function<Object, Boolean> NO_VALUE = c -> false;

  @Test
  void mappingIsTrueWhenEveryComponentIs() {
    assertThat(
      new AnyOf<>(ImmutableList.of(YES, YES)).apply(null, "t")
    ).isTrue();
  }

  @Test
  void mappingWithOneTrueComponentAmongFalseOnes() {
    assertThat(
      new AnyOf<>(ImmutableList.of(NO, YES, NO)).apply(null, "t")
    ).isEqualTo(true);
  }

  @Test
  void mappingIsFalseWhenEveryComponentIs() {
    assertThat(
      new AnyOf<>(ImmutableList.of(NO, NO)).apply(null, "t")
    ).isFalse();
  }

  @Test
  void mappingWithNoComponents() {
    assertThat(
      new AnyOf<Object, Object>(ImmutableList.of()).apply(null, "t")
    ).isEqualTo(false);
  }

  @Test
  void mappingPassesTheContextAndTheThingToItsComponents() {
    final BiFunction<String, String, Boolean> sameAsContext = String::equals;
    assertThat(
      new AnyOf<>(ImmutableList.of(sameAsContext)).apply("a", "a")
    ).isTrue();
    assertThat(
      new AnyOf<>(ImmutableList.of(sameAsContext)).apply("a", "b")
    ).isFalse();
  }

  @Test
  void valueWithOneTrueComponentAmongFalseOnes() {
    assertThat(anyOfValues(List.of(NO_VALUE, YES_VALUE)).apply(null)).isEqualTo(
      true
    );
  }

  @Test
  void valueIsTrueWhenEveryComponentIs() {
    assertThat(anyOfValues(List.of(YES_VALUE, YES_VALUE)).apply(null)).isTrue();
  }

  @Test
  void valueWithNoComponents() {
    assertThat(
      anyOfValues(List.<Function<Object, Boolean>>of()).apply(null)
    ).isEqualTo(false);
  }

  @Test
  void valuePassesTheContextToItsComponents() {
    final Function<String, Boolean> isA = "a"::equals;
    assertThat(anyOfValues(List.of(isA)).apply("a")).isTrue();
    assertThat(anyOfValues(List.of(isA)).apply("b")).isFalse();
  }

  private static <C> Function<C, Boolean> anyOfValues(
    final List<? extends Function<? super C, Boolean>> values
  ) {
    return new MappingAsValue<>(
      new AnyOf<C, Void>(values.stream().map(ValueAsMapping::new).toList())
    );
  }
}
