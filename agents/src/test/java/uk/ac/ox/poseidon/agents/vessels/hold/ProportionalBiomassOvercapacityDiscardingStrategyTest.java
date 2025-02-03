/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.agents.vessels.hold;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.hold.OvercapacityDiscardingStrategy.Result;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ox.poseidon.biology.species.DummySpecies.A;
import static uk.ac.ox.poseidon.biology.species.DummySpecies.B;

class ProportionalBiomassOvercapacityDiscardingStrategyTest {

    @Test
    void discard_AllContentFitsWithinCapacity_NoDiscardingDone() {
        // Arrange
        final ProportionalBiomassOvercapacityDiscardingStrategy strategy =
            new ProportionalBiomassOvercapacityDiscardingStrategy();

        final Bucket<Biomass> contentToAdd = Bucket.of(A, Biomass.ofKg(20.0));
        final Bucket<Biomass> currentHoldContent = Bucket.of(B, Biomass.ofKg(30.0));

        final double capacityInKg = 100.0;
        final double toleranceInKg = 0.1;

        // Act
        final Result<Biomass> result = strategy.discard(
            contentToAdd,
            currentHoldContent,
            capacityInKg,
            toleranceInKg
        );

        // Assert
        assertTrue(result.discarded.isEmpty(), "No biomass should be discarded.");
        assertEquals(50.0, result.updatedHoldContent.getTotalBiomass().asKg(), 0.001);
    }

    @Test
    void discard_ContentExceedsCapacity_DiscardProportionally() {
        // Arrange
        final ProportionalBiomassOvercapacityDiscardingStrategy strategy =
            new ProportionalBiomassOvercapacityDiscardingStrategy();

        final Bucket<Biomass> contentToAdd = Bucket.of(A, Biomass.ofKg(40.0));
        final Bucket<Biomass> currentHoldContent = Bucket.of(B, Biomass.ofKg(30.0));
        final double capacityInKg = 50.0;
        final double toleranceInKg = 0.1;

        // Act
        final Result<Biomass> result = strategy.discard(
            contentToAdd,
            currentHoldContent,
            capacityInKg,
            toleranceInKg
        );

        // Assert
        assertEquals(
            20.0,
            result.discarded.getTotalBiomass().asKg(),
            0.001,
            "20kg should be discarded."
        );
        assertEquals(
            50.0,
            result.updatedHoldContent.getTotalBiomass().asKg(),
            0.001,
            "Hold content should be capped to the capacity."
        );
    }

    @Test
    void discard_AllCapacityFull_AllContentDiscarded() {
        // Arrange
        final ProportionalBiomassOvercapacityDiscardingStrategy strategy =
            new ProportionalBiomassOvercapacityDiscardingStrategy();

        final Bucket<Biomass> contentToAdd = Bucket.of(A, Biomass.ofKg(10.0));
        final Bucket<Biomass> currentHoldContent = Bucket.of(B, Biomass.ofKg(30.0));

        final double capacityInKg = 30.0;
        final double toleranceInKg = 0.1;

        // Act
        final Result<Biomass> result = strategy.discard(
            contentToAdd,
            currentHoldContent,
            capacityInKg,
            toleranceInKg
        );

        // Assert
        assertEquals(
            10.0,
            result.discarded.getTotalBiomass().asKg(),
            0.001,
            "All 10kg should be discarded."
        );
        assertEquals(
            30.0,
            result.updatedHoldContent.getTotalBiomass().asKg(),
            0.001,
            "Hold content should be unchanged."
        );
    }

    @Test
    void discard_ContentExceedsCapacityBySmallAmount_RemainderDiscarded() {
        // Arrange
        final ProportionalBiomassOvercapacityDiscardingStrategy strategy =
            new ProportionalBiomassOvercapacityDiscardingStrategy();

        final Bucket<Biomass> contentToAdd = Bucket.of(A, Biomass.ofKg(25.0));
        final Bucket<Biomass> currentHoldContent = Bucket.of(B, Biomass.ofKg(30.0));
        final double capacityInKg = 50.0;
        final double toleranceInKg = 0.1;

        // Act
        final Result<Biomass> result = strategy.discard(
            contentToAdd,
            currentHoldContent,
            capacityInKg,
            toleranceInKg
        );

        // Assert
        assertEquals(
            5.0,
            result.discarded.getTotalBiomass().asKg(),
            0.001,
            "5kg should be discarded."
        );
        assertEquals(
            50.0,
            result.updatedHoldContent.getTotalBiomass().asKg(),
            0.001,
            "Hold content should be capped to the capacity."
        );
    }
}
