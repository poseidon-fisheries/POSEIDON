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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReusableOptionValuesTest {

    @Test
    void forEachBestEntryOnlyEmitsTrueMaximum() {
        final ReusableOptionValues<String> values = new ReusableOptionValues<>();
        // Enough distinct entries, in an order unrelated to the map's hash-bucket iteration
        // order, that a single-pass implementation which forgets to retract an
        // entry once a higher value is found later (the original bug) would very
        // likely emit some non-maximal entry alongside the true maximum.
        for (int i = 0; i < 20; i++) {
            values.putIfGreater("option-" + i, i);
        }

        final List<Map.Entry<String, Double>> emitted = new ArrayList<>();
        values.forEachBestEntry((key, value) -> emitted.add(Map.entry(key, value)));

        assertThat(emitted)
            .as("only the entry with the true maximum value should be emitted")
            .containsExactly(Map.entry("option-19", 19.0));
    }

    @Test
    void forEachBestEntryEmitsEveryEntryTiedForMaximum() {
        final ReusableOptionValues<String> values = new ReusableOptionValues<>();
        values.putIfGreater("A", 5.0);
        values.putIfGreater("B", 10.0);
        values.putIfGreater("C", 10.0);
        values.putIfGreater("D", 3.0);

        final List<String> emittedKeys = new ArrayList<>();
        values.forEachBestEntry((key, value) -> emittedKeys.add(key));

        assertThat(emittedKeys).containsExactlyInAnyOrder("B", "C");
    }

    @Test
    void forEachBestEntryOnEmptyValuesEmitsNothing() {
        final ReusableOptionValues<String> values = new ReusableOptionValues<>();

        final List<String> emittedKeys = new ArrayList<>();
        values.forEachBestEntry((key, value) -> emittedKeys.add(key));

        assertThat(emittedKeys).isEmpty();
    }
}
