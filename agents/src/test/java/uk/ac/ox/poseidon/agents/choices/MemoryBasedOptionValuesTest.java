/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.ToDoubleBiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MemoryBasedOptionValuesTest {

    private static final BinaryOperator<Double> SUM = Double::sum;

    private final KeyedMemory<String, String, Double> keyedMemory = new KeyedMemory<>();
    private final AtomicReference<String> contextKey = new AtomicReference<>("trawl");
    private final AtomicReference<ToDoubleBiFunction<String, Double>> valuation =
        new AtomicReference<>((option, recollection) -> recollection);
    private final MemoryBasedOptionValues<String, String, Double> optionValues =
        new MemoryBasedOptionValues<>(
            keyedMemory,
            mock(Vessel.class),
            vessel -> contextKey.get(),
            (option, recollection) -> valuation.get().applyAsDouble(option, recollection)
        );

    @Test
    void bestOptionChangesWhenTheValuationChangesWithoutNewObservations() {
        keyedMemory.get("trawl").observe("A", 1.0, SUM);
        keyedMemory.get("trawl").observe("B", 2.0, SUM);
        assertThat(optionValues.getBestOptions()).containsExactly("B");

        valuation.set((option, recollection) -> -recollection);

        assertThat(optionValues.getBestOptions()).containsExactly("A");
        assertThat(optionValues.getBestValue()).contains(-1.0);
    }

    @Test
    void bestOptionChangesWhenTheContextKeyChanges() {
        keyedMemory.get("trawl").observe("A", 1.0, SUM);
        keyedMemory.get("seine").observe("B", 5.0, SUM);
        assertThat(optionValues.getBestOptions()).containsExactly("A");

        contextKey.set("seine");

        assertThat(optionValues.getBestOptions()).containsExactly("B");
    }

    @Test
    void tiesGiveEveryTiedOption() {
        keyedMemory.get("trawl").observe("A", 2.0, SUM);
        keyedMemory.get("trawl").observe("B", 2.0, SUM);
        keyedMemory.get("trawl").observe("C", 1.0, SUM);

        assertThat(optionValues.getBestOptions()).containsExactlyInAnyOrder("A", "B");
        assertThat(optionValues.getBestEntries())
            .containsExactlyInAnyOrder(Map.entry("A", 2.0), Map.entry("B", 2.0));
        assertThat(optionValues.getBestValue()).contains(2.0);
        final MersenneTwisterFast rng = new MersenneTwisterFast(42);
        assertThat(optionValues.getBestOption(rng)).hasValueSatisfying(
            option -> assertThat(option).isIn("A", "B")
        );
        assertThat(optionValues.getBestEntry(rng)).hasValueSatisfying(
            entry -> assertThat(entry).isIn(Map.entry("A", 2.0), Map.entry("B", 2.0))
        );
    }

    @Test
    void emptyMemoryHasNoBestOption() {
        final MersenneTwisterFast rng = new MersenneTwisterFast(42);

        assertThat(optionValues.getBestOptions()).isEmpty();
        assertThat(optionValues.getBestEntries()).isEmpty();
        assertThat(optionValues.getBestValue()).isEmpty();
        assertThat(optionValues.getBestOption(rng)).isEmpty();
        assertThat(optionValues.getBestEntry(rng)).isEmpty();
    }

    @Test
    void valuesGoThroughTheValuation() {
        keyedMemory.get("trawl").observe("A", 1.0, SUM);
        keyedMemory.get("trawl").observe("B", 2.0, SUM);
        valuation.set((option, recollection) -> recollection * 10);

        assertThat(optionValues.getValue("A")).contains(10.0);
        assertThat(optionValues.getValue("C")).isEmpty();
        final Map<String, Double> entries = new HashMap<>();
        optionValues.forEachEntry(entries::put);
        assertThat(entries).containsOnly(Map.entry("A", 10.0), Map.entry("B", 20.0));
    }

    @Test
    void valuationCanDependOnTheOption() {
        keyedMemory.get("trawl").observe("near", 1.0, SUM);
        keyedMemory.get("trawl").observe("far", 2.0, SUM);
        valuation.set((option, recollection) -> recollection - (option.equals("far") ? 5 : 0));

        assertThat(optionValues.getValue("far")).contains(-3.0);
        assertThat(optionValues.getBestOptions()).containsExactly("near");
    }
}
