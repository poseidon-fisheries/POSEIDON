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

package uk.ac.ox.poseidon.core;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;
import uk.ac.ox.poseidon.core.utils.ListFactory;
import uk.ac.ox.poseidon.core.utils.ObjectFactory;

class ScenarioTest {

  private static final DateTimeFactory START = new DateTimeFactory(
    2000,
    1,
    1,
    0,
    0,
    0
  );

  @Test
  void aNodeIsReplacedEverywhereItIsHeld() {
    final NumberFactory number = number(1);
    final Scenario scenario = scenario(
      Map.of("number", number, "pair", pair(number, number))
    );
    final NumberFactory replacement = number(2);

    final Scenario derived = scenario.with("number", replacement);

    final PairFactory pair = derived.component("pair", PairFactory.class);
    assertThat(derived.component("number")).isSameAs(replacement);
    assertThat(pair.getFirst()).isSameAs(replacement);
    assertThat(pair.getSecond()).isSameAs(replacement);
  }

  @Test
  void aValueIsReplacedInItsContainerEverywhereItIsHeld() {
    final NumberFactory number = number(1);
    final Scenario scenario = scenario(
      Map.of("number", number, "pair", pair(number, number))
    );

    final Scenario derived = scenario.with("number.value", 2.0);

    final NumberFactory copy = derived.component("number", NumberFactory.class);
    final PairFactory pair = derived.component("pair", PairFactory.class);
    assertThat(copy.getValue()).isEqualTo(2.0);
    assertThat(pair.getFirst()).isSameAs(copy);
    assertThat(pair.getSecond()).isSameAs(copy);
    assertThat(number.getValue()).isEqualTo(1.0);
  }

  // Both halves of the top pair change, and two components hold it: one
  // copy must hold both changed halves, and serve both components.
  @Test
  void anAncestorWithTwoChangedChildrenIsCopiedOnce() {
    final NumberFactory number = number(1);
    final PairFactory top = pair(
      pair(number, number(3)),
      pair(number, number(4))
    );
    final Scenario scenario = scenario(
      Map.of("number", number, "top", top, "sameTop", top)
    );

    final Scenario derived = scenario.with("number.value", 2.0);

    final NumberFactory copy = derived.component("number", NumberFactory.class);
    final PairFactory topCopy = derived.component("top", PairFactory.class);
    assertThat(derived.component("sameTop")).isSameAs(topCopy);
    assertThat(((PairFactory) topCopy.getFirst()).getFirst()).isSameAs(copy);
    assertThat(((PairFactory) topCopy.getSecond()).getFirst()).isSameAs(copy);
  }

  @Test
  void whatAReplacementDoesNotReachIsKept() {
    final NumberFactory number = number(1);
    final PairFactory untouched = pair(number(3), number(4));
    final Scenario scenario = scenario(
      Map.of("number", number, "untouched", untouched)
    );

    final Scenario derived = scenario.with("number.value", 2.0);

    assertThat(derived.component("untouched")).isSameAs(untouched);
    assertThat(derived.getStartingDateTime()).isSameAs(START);
    assertThat(scenario.component("number")).isSameAs(number);
  }

  @Test
  void addressesGoIntoMapsAndLists() {
    final Scenario scenario = scenario(
      Map.of(
        "table",
        new ObjectFactory<>(Map.of("DBO;adult", 1.0, "HKE;adult", 3.0)),
        "list",
        new ListFactory<>(ImmutableList.of(number(1), number(2)))
      )
    );

    final Scenario derived = scenario
      .with("table.value(DBO;adult)", 2.0)
      .with("list.factories[1].value", 5.0);

    assertThat(
      derived.component("table", ObjectFactory.class).getValue()
    ).isEqualTo(Map.of("DBO;adult", 2.0, "HKE;adult", 3.0));
    final ListFactory<?, ?> list = derived.component("list", ListFactory.class);
    assertThat(list.getFactories())
      .extracting(factory -> ((NumberFactory) factory).getValue())
      .containsExactly(1.0, 5.0);
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "missing",
      "number.missing",
      "number.value.further",
      "table.value(missing)",
      "list.factories[2]"
    }
  )
  void refusesAnAddressThatLeadsNowhere(final String address) {
    final Scenario scenario = scenario(
      Map.of(
        "number",
        number(1),
        "table",
        new ObjectFactory<>(Map.of("key", 1.0)),
        "list",
        new ListFactory<>(ImmutableList.of(number(1)))
      )
    );

    assertThatThrownBy(() -> scenario.with(address, 2.0)).isInstanceOf(
      IllegalArgumentException.class
    );
  }

  @ParameterizedTest
  @ValueSource(strings = { ".", "(", ")", "[", "]" })
  void refusesAComponentNameAnAddressCannotStartWith(final String character) {
    final String name = "a" + character + "b";
    final Scenario scenario = scenario(Map.of());

    assertThatThrownBy(() -> scenario.withComponent(name, number(1)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining(name);
  }

  @Test
  void refusesAComponentNameAlreadyTaken() {
    final Scenario scenario = scenario(Map.of("number", number(1)));

    assertThatThrownBy(() ->
      scenario.withComponent("number", number(2))
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aDerivedStartingDateTimeStartsTheSchedule() {
    final Scenario scenario = scenario(Map.of());

    final Simulation simulation = scenario
      .withStartingDateTime(LocalDateTime.of(2001, 2, 3, 4, 5))
      .startNewSimulation();

    try {
      assertThat(
        simulation.getTemporalSchedule().getStartingDateTime()
      ).isEqualTo(LocalDateTime.of(2001, 2, 3, 4, 5));
      assertThat(scenario.getStartingDateTime()).isSameAs(START);
    } finally {
      simulation.finish();
    }
  }

  // Each build waits until the other has started, so the two derived
  // scenarios are built at the same time.
  @Test
  void simulationsOfScenariosDerivedInParallelKeepTheirOwnValues()
    throws Exception {
    final CountDownLatch bothBuilding = new CountDownLatch(2);
    final Scenario scenario = scenario(
      Map.of(
        "number",
        LatchedNumberFactory.builder().value(0).latch(bothBuilding).build()
      )
    );
    try (final ExecutorService executor = Executors.newFixedThreadPool(2)) {
      final List<CompletableFuture<Object>> numbers = List.of(1.0, 2.0)
        .stream()
        .map(value ->
          CompletableFuture.supplyAsync(
            () -> builtNumber(scenario.with("number.value", value)),
            executor
          )
        )
        .toList();

      assertThat(numbers.get(0).get(30, SECONDS)).isEqualTo(1.0);
      assertThat(numbers.get(1).get(30, SECONDS)).isEqualTo(2.0);
    }
  }

  private static Object builtNumber(final Scenario scenario) {
    final Simulation simulation = scenario.startNewSimulation();
    try {
      return simulation.components.get("number");
    } finally {
      simulation.finish();
    }
  }

  private static Scenario scenario(
    final Map<String, ? extends Factory<? super SimulationScope, ?>> components
  ) {
    return Scenario.builder()
      .startingDateTime(START)
      .components(components)
      .build();
  }

  private static NumberFactory number(final double value) {
    return NumberFactory.builder().value(value).build();
  }

  private static PairFactory pair(
    final Factory<? super Scope, ?> first,
    final Factory<? super Scope, ?> second
  ) {
    return PairFactory.builder().first(first).second(second).build();
  }

  @Getter
  @SuperBuilder(toBuilder = true)
  static class NumberFactory extends GlobalScopeFactory<Double> {

    private final double value;

    @Override
    protected Double newInstance(final Scope scope) {
      return value;
    }
  }

  @Getter
  @SuperBuilder(toBuilder = true)
  static class PairFactory extends GlobalScopeFactory<List<Object>> {

    private final Factory<? super Scope, ?> first;
    private final Factory<? super Scope, ?> second;

    @Override
    protected List<Object> newInstance(final Scope scope) {
      return List.of(first.get(scope), second.get(scope));
    }
  }

  @Getter
  @SuperBuilder(toBuilder = true)
  static class LatchedNumberFactory extends SimulationScopeFactory<Double> {

    private final double value;
    private final CountDownLatch latch;

    @Override
    protected Double newInstance(final SimulationScope scope) {
      latch.countDown();
      try {
        if (!latch.await(10, SECONDS)) {
          throw new IllegalStateException("The builds did not overlap");
        }
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(e);
      }
      return value;
    }
  }
}
