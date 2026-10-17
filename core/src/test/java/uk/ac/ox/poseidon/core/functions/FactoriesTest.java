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

package uk.ac.ox.poseidon.core.functions;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.functions.Factories.combinedDuration;
import static uk.ac.ox.poseidon.core.functions.Factories.defaultIfNull;
import static uk.ac.ox.poseidon.core.functions.Factories.mapLookup;
import static uk.ac.ox.poseidon.core.functions.Factories.multiStringKeyFromFunctions;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * Each combinator of functions keeps the shape of what it combines: values give
 * a value, mappings a mapping, and both pass the context they are asked under
 * to what they combine.
 */
class FactoriesTest {

  /** The length of the context, a string. */
  private final ValueFactory<Scope, String, Integer> contextLength = scope ->
    String::length;

  /** The thing mapped, an integer, plus the length of the context. */
  private final MappingFactory<Scope, String, Integer, Integer> plusLength =
    scope -> (context, i) -> i + context.length();

  /** Null for an empty context, else the context itself. */
  private final ValueFactory<Scope, String, String> nullIfEmpty = scope ->
    context ->
      context.isEmpty() ? null : context;

  /** Names of a few numbers. */
  private final Factory<Scope, Map<Integer, String>> names = scope ->
    Map.of(3, "three", 5, "five");

  @Test
  void lookingUpAValueGivesAValue() {
    final Function<String, String> value = mapLookup(contextLength, names).get(
      GLOBAL_SCOPE
    );
    assertThat(value.apply("abc")).isEqualTo("three");
    assertThat(value.apply("ab")).isNull();
  }

  @Test
  void lookingUpAMappingGivesAMapping() {
    final BiFunction<String, Integer, String> mapping = mapLookup(
      plusLength,
      names
    ).get(GLOBAL_SCOPE);
    assertThat(mapping.apply("ab", 3)).isEqualTo("five");
    assertThat(mapping.apply("ab", 2)).isNull();
  }

  @Test
  void aDefaultReplacesANullValue() {
    final Function<String, String> value = defaultIfNull(
      nullIfEmpty,
      "none"
    ).get(GLOBAL_SCOPE);
    assertThat(value.apply("")).isEqualTo("none");
    assertThat(value.apply("a")).isEqualTo("a");
  }

  @Test
  void aDefaultReplacesANullMapping() {
    final MappingFactory<Scope, Object, String, String> nullIfEmptyMapping =
      scope -> (context, s) -> s.isEmpty() ? null : s;
    final BiFunction<Object, String, String> mapping = defaultIfNull(
      nullIfEmptyMapping,
      "none"
    ).get(GLOBAL_SCOPE);
    assertThat(mapping.apply(null, "")).isEqualTo("none");
    assertThat(mapping.apply(null, "a")).isEqualTo("a");
  }

  @Test
  void durationsOfValuesAddUp() {
    final ValueFactory<Scope, String, Duration> contextLengthInHours = scope ->
      context ->
        Duration.ofHours(context.length());
    final Function<String, Duration> value = combinedDuration(
      contextLengthInHours,
      constant(Duration.ofHours(1))
    ).get(GLOBAL_SCOPE);
    assertThat(value.apply("ab")).isEqualTo(Duration.ofHours(3));
  }

  @Test
  void durationsOfMappingsAddUp() {
    final MappingFactory<Scope, String, Integer, Duration> hours = scope ->
      (context, i) ->
        Duration.ofHours(i + context.length());
    final BiFunction<String, Integer, Duration> mapping = combinedDuration(
      hours,
      hours
    ).get(GLOBAL_SCOPE);
    assertThat(mapping.apply("a", 1)).isEqualTo(Duration.ofHours(4));
  }

  @Test
  void aKeyFromValuesJoinsThem() {
    final Function<String, String> value = multiStringKeyFromFunctions(
      contextLength,
      constant("x")
    ).get(GLOBAL_SCOPE);
    assertThat(value.apply("abc")).isEqualTo(
      uk.ac.ox.poseidon.core.utils.Utils.multiStringKey(3, "x")
    );
  }

  @Test
  void aKeyFromMappingsJoinsThem() {
    final BiFunction<String, Integer, String> mapping =
      multiStringKeyFromFunctions(plusLength, plusLength).get(GLOBAL_SCOPE);
    assertThat(mapping.apply("a", 1)).isEqualTo(
      uk.ac.ox.poseidon.core.utils.Utils.multiStringKey(2, 2)
    );
  }
}
