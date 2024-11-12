/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.behaviours.choices;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AveragingOptionValuesTest {

    private AveragingOptionValues<String> optionValues;
    private MersenneTwisterFast rng;

    @BeforeEach
    void setUp() {
        optionValues = new AveragingOptionValues<>();
        rng = new MersenneTwisterFast(12345);  // Fixed seed for repeatable results
    }

    @Test
    void testObserveSingleOption() {
        optionValues.observe("OptionA", 10.0);
        assertEquals(Optional.of(10.0), optionValues.getValue("OptionA"));
    }

    @Test
    void testObserveMultipleValuesForSameOption() {
        optionValues.observe("OptionA", 10.0);
        optionValues.observe("OptionA", 20.0);
        assertEquals(Optional.of(15.0), optionValues.getValue("OptionA"));
    }

    @Test
    void testGetBestOptionWithTie() {
        optionValues.observe("OptionA", 20.0);
        optionValues.observe("OptionB", 20.0);

        final List<String> bestOptions = optionValues.getBestOptions();
        assertTrue(bestOptions.contains("OptionA"));
        assertTrue(bestOptions.contains("OptionB"));
        assertEquals(2, bestOptions.size()); // Both options should be in the best options list
    }

    @Test
    void testRandomBestOptionSelectionWithTie() {
        optionValues.observe("OptionA", 20.0);
        optionValues.observe("OptionB", 20.0);
        optionValues.observe("OptionC", 10.0);  // Lower value, should not be selected

        // Collecting multiple random selections to verify randomness with ties
        int countA = 0;
        int countB = 0;
        for (int i = 0; i < 1000; i++) {
            final Optional<String> bestOption = optionValues.getBestOption(rng);
            assertTrue(bestOption.isPresent());
            if (bestOption.get().equals("OptionA")) countA++;
            if (bestOption.get().equals("OptionB")) countB++;
        }

        // With a large sample, both options should be selected approximately equally
        assertTrue(countA > 400 && countB > 400);  // Roughly equal distribution
    }

    @Test
    void testCacheInvalidationOnObserve() {
        optionValues.observe("OptionA", 10.0);
        optionValues.observe("OptionB", 20.0);

        // Cache the best option entries
        final List<String> initialBestOptions = optionValues.getBestOptions();
        assertTrue(initialBestOptions.contains("OptionB"));

        // Update OptionA to make it the best, invalidating the cache
        optionValues.observe("OptionA", 30.0); // Average should now be equal to OptionB

        final List<String> updatedBestOptions = optionValues.getBestOptions();
        assertTrue(updatedBestOptions.contains("OptionA"));
        assertTrue(updatedBestOptions.contains("OptionB"));
    }

    @Test
    void testGetBestWhenNoObservations() {
        assertTrue(optionValues.getBestOptions().isEmpty());
        assertFalse(optionValues.getBestOption(rng).isPresent());
        assertFalse(optionValues.getBestValue().isPresent());
    }
}
