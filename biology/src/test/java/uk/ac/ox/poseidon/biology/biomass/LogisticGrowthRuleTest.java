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

package uk.ac.ox.poseidon.biology.biomass;

import org.junit.jupiter.api.Test;

import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class LogisticGrowthRuleTest {

    private static void testPopulationGrowth(
        final double initialBiomass,
        final double carryingCapacity,
        final double growthRate,
        final double[] expectedValues
    ) {
        final BiomassGrowthRule logisticGrowthRule = new LogisticGrowthRule(growthRate);
        assertArrayEquals(
            expectedValues,
            DoubleStream
                .iterate(
                    initialBiomass,
                    currentBiomass -> logisticGrowthRule.newBiomass(
                        currentBiomass,
                        carryingCapacity
                    )
                )
                .skip(1)
                .limit(expectedValues.length)
                .toArray(),
            0.001
        );
    }

    @Test
    void testNormalValues() {
        final double[] expectedValues =
            {109.0, 118.7119, 129.1738, 140.4226, 152.4930, 165.4169, 179.2223, 193.9325,
                209.5647, 226.1295};
        testPopulationGrowth(100, 1000, 0.1, expectedValues);
    }

    @Test
    void testInitialPopulationAtCarryingCapacity() {
        final double[] expectedValues =
            {1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0};
        testPopulationGrowth(1000, 1000, 0.1, expectedValues);
    }

    @Test
    void testInitialPopulationExceedingCarryingCapacity() {
        final double[] expectedValues =
            {1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0};
        testPopulationGrowth(1200, 1000, 0.1, expectedValues);
    }

    @Test
    void testZeroGrowthRate() {
        final double[] expectedValues =
            {100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0};
        testPopulationGrowth(100, 1000, 0.0, expectedValues);
    }

    @Test
    void testVeryHighGrowthRate() {
        final double[] expectedValues =
            {190.0, 343.9, 569.5328, 814.6979, 965.6631, 998.8209,
                999.9987, 999.9999, 999.9999, 1000.0};
        testPopulationGrowth(100, 1000, 1.0, expectedValues);
    }

    @Test
    void testVeryLowInitialPopulation() {
        final double[] expectedValues =
            {1.0999, 1.2098, 1.3305, 1.4634, 1.6096, 1.7703, 1.9470, 2.1413, 2.355, 2.5899};
        testPopulationGrowth(1, 1000, 0.1, expectedValues);
    }

    @Test
    void testNormalValuesWithLowerCarryingCapacity() {
        final double[] expectedValues =
            {108.0, 116.4672, 125.4009, 134.7960, 144.6416, 154.9215, 165.6135, 176.6893,
                188.1144, 199.8484};
        testPopulationGrowth(100, 500, 0.1, expectedValues);
    }

    @Test
    void testInitialPopulationAtCarryingCapacityWithLowerCarryingCapacity() {
        final double[] expectedValues =
            {500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0};
        testPopulationGrowth(500, 500, 0.1, expectedValues);
    }

    @Test
    void testInitialPopulationExceedingLowerCarryingCapacity() {
        final double[] expectedValues =
            {500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0};
        testPopulationGrowth(600, 500, 0.1, expectedValues);
    }

    @Test
    void testZeroGrowthRateWithLowerCarryingCapacity() {
        final double[] expectedValues =
            {100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0};
        testPopulationGrowth(100, 500, 0.0, expectedValues);
    }

    @Test
    void testVeryHighGrowthRateWithLowerCarryingCapacity() {
        final double[] expectedValues =
            {180.0, 295.2, 416.1139, 485.9262, 499.6038, 499.9996, 500.0, 500.0, 500.0, 500.0};
        testPopulationGrowth(100, 500, 1.0, expectedValues);
    }

    @Test
    void testVeryLowInitialPopulationWithLowerCarryingCapacity() {
        final double[] expectedValues =
            {1.0998, 1.2095, 1.3302, 1.4628, 1.6086, 1.7690, 1.9453, 2.1390, 2.3526, 2.5870};
        testPopulationGrowth(1, 500, 0.1, expectedValues);
    }
}
