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

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.functions.Factories.mapEntry;
import static uk.ac.ox.poseidon.core.functions.Factories.mapLookup;
import static uk.ac.ox.poseidon.core.utils.Factories.object;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.functions.MapEntryFactory;
import uk.ac.ox.poseidon.core.functions.MapLookupFactory;

/**
 * The factories that hold a value typed by a type parameter accept only
 * literals: a scalar, a string, an enum, or a list or map of literals, keys
 * included.
 */
class FactoryLiteralChecksTest {

  private static final class Bean {

    @Override
    public String toString() {
      return "a bean";
    }
  }

  private static final Factory<Scope, Object> A_FACTORY = _ -> "x";

  private static Stream<Object> literals() {
    return Stream.of(
      "text",
      1,
      2.5,
      true,
      'c',
      DayOfWeek.MONDAY,
      List.of(),
      List.of(1, 2),
      Map.of("a", 1.0),
      Map.of(1, List.of(Map.of("b", DayOfWeek.FRIDAY))),
      List.of(List.of("a"), Map.of("k", "v"))
    );
  }

  private static Stream<Object> nonLiterals() {
    return Stream.of(
      new Bean(),
      A_FACTORY,
      Set.of("a"),
      List.of("a", new Bean()),
      List.of(A_FACTORY),
      List.of(Set.of(1)),
      Map.of("a", new Bean()),
      Map.of(new Bean(), "a"),
      Map.of("a", List.of(Map.of("b", new Bean())))
    );
  }

  private static Stream<Supplier<Object>> builds(final Object value) {
    return Stream.of(
      () -> ObjectFactory.builder().value(value).build(),
      () ->
        MapEntryFactory.builder()
          .map(_ -> Map.of())
          .key(value)
          .build(),
      () ->
        MapLookupFactory.builder()
          .key((MappingFactory<Scope, Object, Object, Object>) _ -> (_, _) -> 1)
          .map(_ -> Map.of())
          .defaultValue(value)
          .build()
    );
  }

  @ParameterizedTest
  @MethodSource("literals")
  void everyKindOfLiteralIsAccepted(final Object literal) {
    builds(literal).forEach(build ->
      assertThatCode(build::get).doesNotThrowAnyException()
    );
  }

  @ParameterizedTest
  @MethodSource("nonLiterals")
  void anythingElseIsRejected(final Object value) {
    builds(value).forEach(build ->
      assertThatThrownBy(build::get)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must be a literal")
    );
  }

  @Test
  void theMessageShowsTheValue() {
    assertThatThrownBy(() -> object(List.of("a", new Bean()))).hasMessage(
      "value must be a literal, not [a, a bean]."
    );
  }

  @Test
  void aMapLookupWithNoDefaultValueIsAccepted() {
    assertThatCode(() ->
      MapLookupFactory.builder()
        .key((MappingFactory<Scope, Object, Object, Object>) _ -> (_, _) -> 1)
        .map(_ -> Map.of())
        .build()
    ).doesNotThrowAnyException();
  }

  @Test
  void aMapEntryWithANullKeyIsRejected() {
    assertThatThrownBy(() -> mapEntry(_ -> Map.of(), null)).isInstanceOf(
      IllegalArgumentException.class
    );
  }

  @Test
  void theHelpersRejectTheSameValues() {
    assertThatThrownBy(() -> object(new Bean())).isInstanceOf(
      IllegalArgumentException.class
    );
    assertThatThrownBy(() -> mapEntry(_ -> Map.of(), new Bean())).isInstanceOf(
      IllegalArgumentException.class
    );
    final ValueFactory<Scope, Object, Object> key = _ -> _ -> 1;
    assertThatThrownBy(() ->
      mapLookup(key, _ -> Map.of(), new Bean())
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aCopyMadeThroughToBuilderRejectsAChangedValueToo() {
    final ObjectFactory<Object> factory = object("a");

    assertThatThrownBy(() ->
      factory.toBuilder().value(new Bean())
    ).isInstanceOf(IllegalArgumentException.class);
    assertThat(factory.toBuilder().value("b").build().getValue()).isEqualTo(
      "b"
    );
  }

  @Test
  void aMapEntryStillLooksTheKeyUp() throws IOException {
    assertThat(mapEntry(_ -> Map.of("k", 7), "k").get(GLOBAL_SCOPE)).isEqualTo(
      7
    );
    assertThat(entry("k", 7).getValue()).isEqualTo(7);
  }
}
