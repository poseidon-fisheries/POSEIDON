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

package uk.ac.ox.poseidon.core.providers;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.providers.random.RandomIntProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RandomIntProviderTest {

    @Test
    void testInclusiveRangeBounds() {
        final MersenneTwisterFast rng = new MersenneTwisterFast(1234L);
        final RandomIntProvider supplier = new RandomIntProvider(rng, 1, 3);

        for (int i = 0; i < 1000; i++) {
            final int value = supplier.getAsInt();
            assertThat(value)
                .as("Value should be within inclusive bounds")
                .isBetween(1, 3);
        }
    }

    @Test
    void testSingleValueRangeReturnsMinimum() {
        final MersenneTwisterFast rng = new MersenneTwisterFast(42L);
        final RandomIntProvider supplier = new RandomIntProvider(rng, 7, 7);

        for (int i = 0; i < 10; i++) {
            assertThat(supplier.getAsInt())
                .as("Single-value range should return minimum")
                .isEqualTo(7);
        }
    }

    @Test
    void testRangeOverflowThrows() {
        final MersenneTwisterFast rng = new MersenneTwisterFast(1L);

        assertThatThrownBy(() -> new RandomIntProvider(rng, Integer.MIN_VALUE, Integer.MAX_VALUE))
            .as("Overflowing range should be rejected")
            .isInstanceOf(IllegalArgumentException.class);
    }
}
