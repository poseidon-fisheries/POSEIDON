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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.functions.Factories.combinedDuration;
import static uk.ac.ox.poseidon.core.functions.Factories.floorMapLookup;
import static uk.ac.ox.poseidon.core.functions.Factories.mapEntry;
import static uk.ac.ox.poseidon.core.functions.Factories.mapLookup;
import static uk.ac.ox.poseidon.core.functions.Factories.multiStringKeyFromFunctions;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;
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

  /** Names of a few numbers. */
  private final Factory<Scope, Map<Integer, String>> names = scope ->
    Map.of(3, "three", 5, "five");

  @Test
  void lookingUpAValueGivesAValue() {
    final Function<String, String> value = mapLookup(contextLength, names).get(
      GLOBAL_SCOPE
    );
    assertThat(value.apply("abc")).isEqualTo("three");
    assertThatThrownBy(() -> value.apply("ab"))
      .isInstanceOf(NoSuchElementException.class)
      .hasMessageContaining("2");
  }

  @Test
  void lookingUpAMappingGivesAMapping() {
    final BiFunction<String, Integer, String> mapping = mapLookup(
      plusLength,
      names
    ).get(GLOBAL_SCOPE);
    assertThat(mapping.apply("ab", 3)).isEqualTo("five");
    assertThatThrownBy(() -> mapping.apply("ab", 2)).isInstanceOf(
      NoSuchElementException.class
    );
  }

  @Test
  void aValueLookupFallsBackOnItsDefault() {
    final Function<String, String> value = mapLookup(
      contextLength,
      names,
      "none"
    ).get(GLOBAL_SCOPE);
    assertThat(value.apply("abc")).isEqualTo("three");
    assertThat(value.apply("ab")).isEqualTo("none");
  }

  @Test
  void aMappingLookupFallsBackOnItsDefault() {
    final BiFunction<String, Integer, String> mapping = mapLookup(
      plusLength,
      names,
      "none"
    ).get(GLOBAL_SCOPE);
    assertThat(mapping.apply("ab", 3)).isEqualTo("five");
    assertThat(mapping.apply("ab", 2)).isEqualTo("none");
  }

  /** A series of names, from 3 and from 5 on. */
  private final Factory<Scope, NavigableMap<Integer, String>> series = scope ->
    new TreeMap<>(Map.of(3, "three", 5, "five"));

  @Test
  void aFloorLookupOfAValueFindsTheNearestEarlierEntry() {
    final Function<String, String> value = floorMapLookup(
      contextLength,
      series
    ).get(GLOBAL_SCOPE);
    assertThat(value.apply("abc")).isEqualTo("three");
    assertThat(value.apply("abcd")).isEqualTo("three");
    assertThat(value.apply("abcde")).isEqualTo("five");
    assertThat(value.apply("abcdefgh")).isEqualTo("five");
    assertThatThrownBy(() -> value.apply("ab"))
      .isInstanceOf(NoSuchElementException.class)
      .hasMessageContaining("2");
  }

  @Test
  void aFloorLookupOfAMappingFindsTheNearestEarlierEntry() {
    final BiFunction<String, Integer, String> mapping = floorMapLookup(
      plusLength,
      series
    ).get(GLOBAL_SCOPE);
    assertThat(mapping.apply("ab", 1)).isEqualTo("three");
    assertThat(mapping.apply("ab", 3)).isEqualTo("five");
    assertThat(mapping.apply("ab", 10)).isEqualTo("five");
    assertThatThrownBy(() -> mapping.apply("ab", 0))
      .isInstanceOf(NoSuchElementException.class)
      .hasMessageContaining("2");
  }

  @Test
  void aFloorLookupReturnsAnEntrysNullValue() {
    final Function<String, String> value = floorMapLookup(
      contextLength,
      (Factory<Scope, NavigableMap<Integer, String>>) scope -> {
        final NavigableMap<Integer, String> map = new TreeMap<>();
        map.put(2, null);
        return map;
      }
    ).get(GLOBAL_SCOPE);
    assertThat(value.apply("abc")).isNull();
  }

  @Test
  void aFloorLookupWorksOnDates() {
    final Factory<Scope, NavigableMap<LocalDate, String>> prices = scope ->
      new TreeMap<>(
        Map.of(
          LocalDate.of(2026, 1, 1),
          "low",
          LocalDate.of(2026, 7, 1),
          "high"
        )
      );
    final BiFunction<String, LocalDate, String> mapping = floorMapLookup(
      (MappingFactory<Scope, String, LocalDate, LocalDate>) scope ->
        (context, date) ->
          date,
      prices
    ).get(GLOBAL_SCOPE);
    assertThat(mapping.apply("", LocalDate.of(2026, 3, 15))).isEqualTo("low");
    assertThat(mapping.apply("", LocalDate.of(2026, 7, 1))).isEqualTo("high");
    assertThatThrownBy(() -> mapping.apply("", LocalDate.of(2025, 12, 31)))
      .isInstanceOf(NoSuchElementException.class)
      .hasMessageContaining("2025-12-31");
  }

  /** A map with an entry whose value is null. */
  private final Factory<Scope, Map<Integer, String>> withNullValue = scope -> {
    final Map<Integer, String> map = new HashMap<>();
    map.put(2, null);
    return map;
  };

  @Test
  void aLookupReturnsAnEntrysNullValueRatherThanItsDefault() {
    final Function<String, String> value = mapLookup(
      contextLength,
      withNullValue,
      "none"
    ).get(GLOBAL_SCOPE);
    assertThat(value.apply("ab")).isNull();
    assertThat(value.apply("abc")).isEqualTo("none");
  }

  @Test
  void aLookupWithoutDefaultReturnsAnEntrysNullValue() {
    final Function<String, String> value = mapLookup(
      contextLength,
      withNullValue
    ).get(GLOBAL_SCOPE);
    assertThat(value.apply("ab")).isNull();
  }

  @Test
  void anEntryWithANullValueIsFoundWhenResolved() {
    assertThat(mapEntry(withNullValue, 2).get(GLOBAL_SCOPE)).isNull();
  }

  @Test
  void anEntryIsFoundWhenResolved() {
    assertThat(mapEntry(names, 3).get(GLOBAL_SCOPE)).isEqualTo("three");
  }

  @Test
  void aMissingEntryThrowsWhenResolved() {
    final var entry = mapEntry(names, 4);
    assertThatThrownBy(() -> entry.get(GLOBAL_SCOPE))
      .isInstanceOf(NoSuchElementException.class)
      .hasMessageContaining("4");
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
