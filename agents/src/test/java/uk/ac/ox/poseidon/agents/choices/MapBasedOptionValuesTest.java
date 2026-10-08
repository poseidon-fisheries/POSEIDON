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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapBasedOptionValuesTest {

    @Test
    void testGetBestEntriesWhenEmpty() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        assertThat(values.getBestEntries()).isEmpty();
    }

    @Test
    void testGetBestEntriesSingleEntry() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 10.0);
        assertThat(values.getBestEntries())
            .hasSize(1)
            .first()
            .satisfies(entry -> {
                assertThat(entry.getKey()).isEqualTo("A");
                assertThat(entry.getValue()).isEqualTo(10.0);
            });
    }

    @Test
    void testGetBestEntriesUniqueMax() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 10.0);
        values.observe("B", 20.0);
        values.observe("C", 15.0);
        assertThat(values.getBestEntries())
            .hasSize(1)
            .first()
            .satisfies(entry -> {
                assertThat(entry.getKey()).isEqualTo("B");
                assertThat(entry.getValue()).isEqualTo(20.0);
            });
    }

    @Test
    void testGetBestEntriesWithTie() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 20.0);
        values.observe("B", 20.0);
        values.observe("C", 10.0);
        assertThat(values.getBestEntries())
            .hasSize(2)
            .allSatisfy(entry -> assertThat(entry.getValue()).isEqualTo(20.0))
            .extracting(Map.Entry::getKey)
            .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void testGetBestEntriesAllEqual() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 5.0);
        values.observe("B", 5.0);
        values.observe("C", 5.0);
        assertThat(values.getBestEntries())
            .hasSize(3)
            .allSatisfy(entry -> assertThat(entry.getValue()).isEqualTo(5.0));
    }

    @Test
    void testGetBestEntriesCacheReuse() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 10.0);
        assertThat(values.getBestEntries())
            .containsExactlyElementsOf(values.getBestEntries());
    }

    @Test
    void testGetBestEntriesCacheInvalidation() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 10.0);
        values.observe("B", 20.0);

        assertThat(values.getBestEntries())
            .hasSize(1)
            .first()
            .satisfies(entry -> assertThat(entry.getKey()).isEqualTo("B"));

        values.observe("A", 30.0);

        assertThat(values.getBestEntries())
            .hasSize(2)
            .allSatisfy(entry -> assertThat(entry.getValue()).isEqualTo(20.0))
            .extracting(Map.Entry::getKey)
            .containsExactlyInAnyOrder("A", "B");
    }

    private static <O> Map<O, Double> entries(final OptionValues<O> values) {
        final Map<O, Double> entries = new HashMap<>();
        values.forEachEntry(entries::put);
        return entries;
    }

    @Test
    void forEachEntryGivesEveryEntryNotJustTheBest() {
        final ImmutableOptionValues<String> values =
            new ImmutableOptionValues<>(Map.of("A", 10.0, "B", 20.0, "C", 15.0));

        assertThat(entries(values)).containsOnly(
            Map.entry("A", 10.0),
            Map.entry("B", 20.0),
            Map.entry("C", 15.0)
        );
    }

    @Test
    void forEachEntryOnEmptyValuesGivesNothing() {
        assertThat(entries(new ImmutableOptionValues<String>(Map.of()))).isEmpty();
    }

    @Test
    void forEachEntryGivesTheStoredValuesOfMutableOptionValues() {
        final AverageOptionValues<String> values = new AverageOptionValues<>();
        values.observe("A", 10.0);
        values.observe("A", 20.0);
        values.observe("B", 5.0);

        assertThat(entries(values)).containsOnly(
            Map.entry("A", 15.0),
            Map.entry("B", 5.0)
        );
    }
}
