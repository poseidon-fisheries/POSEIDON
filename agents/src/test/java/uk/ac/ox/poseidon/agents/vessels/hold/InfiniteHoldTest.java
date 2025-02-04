/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab
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
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.DummySpecies;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfiniteHoldTest {

    @Test
    void testAddEmptyBucket() {
        // Arrange
        final Bucket<Biomass> emptyBucket = Bucket.empty();
        final InfiniteHold<Biomass> infiniteHold = new InfiniteHold<>();

        // Act
        final Bucket<Biomass> returnedBucket = infiniteHold.addContent(emptyBucket);

        // Assert
        assertTrue(returnedBucket.isEmpty(), "Returned bucket should be empty");
        assertTrue(infiniteHold.getContent().isEmpty(), "InfiniteHold content should remain empty");
    }

    @Test
    void testAddNonEmptyBucket() {
        // Arrange
        final Bucket<Biomass> initialBucket = Bucket.of(DummySpecies.A, Biomass.ofKg(10));
        final Bucket<Biomass> newBucket = Bucket.of(DummySpecies.B, Biomass.ofKg(20));
        final InfiniteHold<Biomass> infiniteHold = new InfiniteHold<>();
        infiniteHold.addContent(initialBucket); // Add initial content to the hold

        // Act
        final Bucket<Biomass> returnedBucket = infiniteHold.addContent(newBucket);

        // Assert
        assertTrue(returnedBucket.isEmpty(), "Returned bucket should be empty");
        assertEquals(
            Bucket.from(Map.of(
                DummySpecies.A, Biomass.ofKg(10),
                DummySpecies.B, Biomass.ofKg(20)
            )),
            infiniteHold.getContent(),
            "InfiniteHold content should contain combined contents"
        );
    }

    @Test
    void testAddOverlappingSpeciesContent() {
        // Arrange
        final Bucket<Biomass> initialBucket = Bucket.of(DummySpecies.A, Biomass.ofKg(10));
        final Bucket<Biomass> newBucket = Bucket.from(Map.of(
            DummySpecies.A, Biomass.ofKg(5),  // Overlapping species
            DummySpecies.C, Biomass.ofKg(15)
        ));
        final InfiniteHold<Biomass> infiniteHold = new InfiniteHold<>();
        infiniteHold.addContent(initialBucket); // Add initial content to the hold

        // Act
        final Bucket<Biomass> returnedBucket = infiniteHold.addContent(newBucket);

        // Assert
        assertTrue(returnedBucket.isEmpty(), "Returned bucket should be empty");
        assertEquals(
            Bucket.from(Map.of(
                DummySpecies.A, Biomass.ofKg(15), // Combined content for overlapping species
                DummySpecies.C, Biomass.ofKg(15)
            )),
            infiniteHold.getContent(),
            "InfiniteHold content should correctly combine overlapping and new species"
        );
    }
}
