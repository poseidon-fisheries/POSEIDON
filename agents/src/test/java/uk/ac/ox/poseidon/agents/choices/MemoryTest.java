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

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class MemoryTest {

    private static final BinaryOperator<Double> SUM = Double::sum;

    private static <O, M> Map<O, M> entries(final Memory<O, M> memory) {
        final Map<O, M> entries = new HashMap<>();
        memory.forEach(entries::put);
        return entries;
    }

    @Test
    void emptyMemoryHasNoEntries() {
        final Memory<String, Double> memory = new Memory<>();

        assertThat(memory.get("A")).isEmpty();
        assertThat(entries(memory)).isEmpty();
    }

    @Test
    void firstObservationIsStoredAsIs() {
        final Memory<String, Double> memory = new Memory<>();
        memory.observe("A", 3.0, (recollection, observation) -> {
            throw new AssertionError("the rule must not be called on a first observation");
        });

        assertThat(memory.get("A")).contains(3.0);
        assertThat(entries(memory)).containsExactly(Map.entry("A", 3.0));
    }

    @Test
    void laterObservationGoesThroughTheRuleWithRecollectionFirst() {
        final Memory<String, Double> memory = new Memory<>();
        memory.observe("A", 3.0, SUM);
        memory.observe("A", 5.0, (recollection, observation) -> recollection * 10 + observation);

        assertThat(memory.get("A")).contains(35.0);
    }

    @Test
    void optionsAreIndependent() {
        final Memory<String, Double> memory = new Memory<>();
        memory.observe("A", 3.0, SUM);
        memory.observe("B", 5.0, SUM);
        memory.observe("A", 1.0, SUM);

        assertThat(entries(memory)).containsOnly(
            Map.entry("A", 4.0),
            Map.entry("B", 5.0)
        );
    }

    @Test
    void nullObservationIsRejected() {
        final Memory<String, Double> memory = new Memory<>();

        assertThatNullPointerException().isThrownBy(() -> memory.observe("A", null, SUM));
        assertThat(entries(memory)).isEmpty();
    }

    @Test
    void ruleReturningNullIsRejectedAndOptionIsNotForgotten() {
        final Memory<String, Double> memory = new Memory<>();
        memory.observe("A", 3.0, SUM);

        assertThatNullPointerException()
            .isThrownBy(() -> memory.observe("A", 5.0, (recollection, observation) -> null));
        assertThat(memory.get("A")).contains(3.0);
    }
}
