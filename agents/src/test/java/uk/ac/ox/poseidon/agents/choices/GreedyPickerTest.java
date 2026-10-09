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

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

class GreedyPickerTest {

    private final MersenneTwisterFast rng = new MersenneTwisterFast(0);

    private GreedyPicker<String> picker(
        final Map<String, Double> values,
        final Predicate<? super String> optionPredicate
    ) {
        return new GreedyPicker<>(new ImmutableOptionValues<>(values), optionPredicate, rng);
    }

    @Test
    void picksTheBestOptionPassingThePredicate() {
        final GreedyPicker<String> picker = picker(
            Map.of("A", 1.0, "B", 3.0, "C", 2.0),
            option -> true
        );

        assertThat(picker.get()).isEqualTo("B");
    }

    @Test
    void skipsBetterOptionsThatDoNotPassThePredicate() {
        final GreedyPicker<String> picker = picker(
            Map.of("A", 1.0, "B", 3.0, "C", 2.0),
            option -> !option.equals("B")
        );

        assertThat(picker.get()).isEqualTo("C");
    }

    @Test
    void givesNullWhenNoOptionPassesThePredicate() {
        final GreedyPicker<String> picker = picker(Map.of("A", 1.0, "B", 3.0), option -> false);

        assertThat(picker.get()).isNull();
    }

    @Test
    void givesNullWhenThereAreNoOptions() {
        assertThat(picker(Map.of(), option -> true).get()).isNull();
    }

    @Test
    void picksTheBestOptionWhenEveryValueIsNegative() {
        final GreedyPicker<String> picker = picker(
            Map.of("A", -3.0, "B", -1.0, "C", -2.0),
            option -> true
        );

        assertThat(picker.get()).isEqualTo("B");
    }

    @Test
    void breaksTiesAtRandom() {
        final GreedyPicker<String> picker = picker(
            Map.of("A", 3.0, "B", 3.0, "C", 1.0),
            option -> true
        );
        final Set<String> picked = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            picked.add(picker.get());
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

        assertThat(picker.get()).isEqualTo("D");
        assertThat(tested).containsExactlyInAnyOrder("B", "D");
    }

    @Test
    @Timeout(value = 1, threadMode = SEPARATE_THREAD)
    void failsWhenTheOnlyValueIsNaN() {
        final GreedyPicker<String> picker = picker(Map.of("A", Double.NaN), option -> true);

        assertThatThrownBy(picker::get)
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

        assertThatThrownBy(picker::get)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("B")
            .hasMessageContaining("NaN");
    }

    @Test
    void treatsInfiniteValuesAsOrdinaryValues() {
        final Map<String, Double> values = Map.of(
            "A", Double.POSITIVE_INFINITY,
            "B", 1.0,
            "C", Double.NEGATIVE_INFINITY
        );

        assertThat(picker(values, option -> true).get()).isEqualTo("A");
        assertThat(picker(values, option -> option.equals("C")).get()).isEqualTo("C");
    }
}
