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

import org.junit.jupiter.api.Test;

import java.util.function.BinaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

class KeyedMemoryTest {

    private static final BinaryOperator<Double> SUM = Double::sum;

    @Test
    void eachKeyHasItsOwnMemory() {
        final KeyedMemory<String, String, Double> keyedMemory = new KeyedMemory<>();
        keyedMemory.get("trawl").observe("A", 3.0, SUM);
        keyedMemory.get("seine").observe("A", 5.0, SUM);

        assertThat(keyedMemory.get("trawl").get("A")).contains(3.0);
        assertThat(keyedMemory.get("seine").get("A")).contains(5.0);
    }

    @Test
    void missingKeyReadsAsEmptyMemory() {
        final KeyedMemory<String, String, Double> keyedMemory = new KeyedMemory<>();

        assertThat(keyedMemory.get("trawl").get("A")).isEmpty();
    }

    @Test
    void sameKeyGivesTheSameMemory() {
        final KeyedMemory<String, String, Double> keyedMemory = new KeyedMemory<>();

        assertThat(keyedMemory.get("trawl")).isSameAs(keyedMemory.get("trawl"));
    }

    @Test
    void nullKeyIsAKeyLikeAnyOther() {
        final KeyedMemory<String, String, Double> keyedMemory = new KeyedMemory<>();
        keyedMemory.get(null).observe("A", 3.0, SUM);

        assertThat(keyedMemory.get(null).get("A")).contains(3.0);
        assertThat(keyedMemory.get("trawl").get("A")).isEmpty();
    }
}
