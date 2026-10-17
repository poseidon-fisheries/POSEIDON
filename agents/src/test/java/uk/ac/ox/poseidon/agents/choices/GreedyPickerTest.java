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

package uk.ac.ox.poseidon.agents.choices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;
import static org.mockito.Mockito.mock;

import ec.util.MersenneTwisterFast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

class GreedyPickerTest {

  private final MersenneTwisterFast rng = new MersenneTwisterFast(0);
  private final Vessel vessel = mock(Vessel.class);

  private GreedyPicker<String> picker(
    final Map<String, Double> values,
    final Predicate<? super String> optionPredicate
  ) {
    return new GreedyPicker<>(
      new ImmutableOptionValues<>(values),
      (pickedFor, option) -> optionPredicate.test(option),
      rng
    );
  }

  @Test
  void picksAmongTheOptionValuesOfTheVesselGivenWithItsPredicate() {
    final Vessel otherVessel = mock(Vessel.class);
    final OptionValues<String> values = new ImmutableOptionValues<>(
      Map.of("A", 1.0, "B", 3.0)
    );
    final OptionValues<String> otherValues = new ImmutableOptionValues<>(
      Map.of("C", 2.0, "D", 1.0)
    );
    final GreedyPicker<String> picker = new GreedyPicker<>(
      pickedFor -> pickedFor == vessel ? values : otherValues,
      (pickedFor, option) -> pickedFor == vessel || !option.equals("C"),
      rng
    );

    assertThat(picker.apply(vessel)).isEqualTo("B");
    assertThat(picker.apply(otherVessel)).isEqualTo("D");
  }

  @Test
  void picksTheBestOptionPassingThePredicate() {
    final GreedyPicker<String> picker = picker(
      Map.of("A", 1.0, "B", 3.0, "C", 2.0),
      option -> true
    );

    assertThat(picker.apply(vessel)).isEqualTo("B");
  }

  @Test
  void skipsBetterOptionsThatDoNotPassThePredicate() {
    final GreedyPicker<String> picker = picker(
      Map.of("A", 1.0, "B", 3.0, "C", 2.0),
      option -> !option.equals("B")
    );

    assertThat(picker.apply(vessel)).isEqualTo("C");
  }

  @Test
  void givesNullWhenNoOptionPassesThePredicate() {
    final GreedyPicker<String> picker = picker(
      Map.of("A", 1.0, "B", 3.0),
      option -> false
    );

    assertThat(picker.apply(vessel)).isNull();
  }

  @Test
  void givesNullWhenThereAreNoOptions() {
    assertThat(picker(Map.of(), option -> true).apply(vessel)).isNull();
  }

  @Test
  void picksTheBestOptionWhenEveryValueIsNegative() {
    final GreedyPicker<String> picker = picker(
      Map.of("A", -3.0, "B", -1.0, "C", -2.0),
      option -> true
    );

    assertThat(picker.apply(vessel)).isEqualTo("B");
  }

  @Test
  void breaksTiesAtRandom() {
    final GreedyPicker<String> picker = picker(
      Map.of("A", 3.0, "B", 3.0, "C", 1.0),
      option -> true
    );
    final Set<String> picked = new HashSet<>();
    for (int i = 0; i < 100; i++) {
      picked.add(picker.apply(vessel));
    }

    assertThat(picked).containsExactlyInAnyOrder("A", "B");
  }

  @Test
  void doesNotTestOptionsBelowTheBestValueWithAPassingOption() {
    final List<String> tested = new ArrayList<>();
    final GreedyPicker<String> picker = picker(
      Map.of("A", 1.0, "B", 3.0, "C", 2.0, "D", 3.0),
      option -> {
        tested.add(option);
        return !option.equals("B");
      }
    );

    assertThat(picker.apply(vessel)).isEqualTo("D");
    assertThat(tested).doesNotContain("A", "C");
  }

  @Test
  void stopsTestingTiedOptionsOnceOnePasses() {
    final List<String> tested = new ArrayList<>();
    final GreedyPicker<String> picker = picker(
      Map.of("A", 3.0, "B", 3.0, "C", 3.0, "D", 3.0),
      option -> {
        tested.add(option);
        return true;
      }
    );

    picker.apply(vessel);

    assertThat(tested).hasSize(1);
  }

  @Test
  @Timeout(value = 1, threadMode = SEPARATE_THREAD)
  void failsWhenTheOnlyValueIsNaN() {
    final GreedyPicker<String> picker = picker(
      Map.of("A", Double.NaN),
      option -> true
    );

    assertThatThrownBy(() -> picker.apply(vessel))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("A")
      .hasMessageContaining("NaN");
  }

  @Test
  @Timeout(value = 1, threadMode = SEPARATE_THREAD)
  void failsWhenAnyValueIsNaN() {
    final GreedyPicker<String> picker = picker(
      Map.of("A", 1.0, "B", Double.NaN, "C", 2.0),
      option -> true
    );

    assertThatThrownBy(() -> picker.apply(vessel))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("B")
      .hasMessageContaining("NaN");
  }

  @Test
  void treatsInfiniteValuesAsOrdinaryValues() {
    final Map<String, Double> values = Map.of(
      "A",
      Double.POSITIVE_INFINITY,
      "B",
      1.0,
      "C",
      Double.NEGATIVE_INFINITY
    );

    assertThat(picker(values, option -> true).apply(vessel)).isEqualTo("A");
    assertThat(
      picker(values, option -> option.equals("C")).apply(vessel)
    ).isEqualTo("C");
  }
}
