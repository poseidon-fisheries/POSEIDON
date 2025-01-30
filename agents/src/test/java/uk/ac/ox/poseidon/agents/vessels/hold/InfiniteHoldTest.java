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
import org.mockito.Mockito;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.DummySpecies;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfiniteHoldTest {

    private static final Bucket<Biomass> SAMPLE_BUCKET =
        Bucket.from(Map.of(
            DummySpecies.A, Biomass.ofKg(5),
            DummySpecies.B, Biomass.ofKg(10),
            DummySpecies.C, Biomass.ofKg(15)
        ));

    @Test
    void addContent_shouldAddContentsToHold_andReturnEmptyBucket() {
        final InfiniteHold<Biomass> hold = new InfiniteHold<>();
        final Bucket<Biomass> result = hold.addContent(SAMPLE_BUCKET);
        assertEquals(SAMPLE_BUCKET, hold.getContent());
        assertTrue(result.isEmpty());
    }

    @Test
    void addContent_shouldReturnEmptyBucket_whenAddingToEmptyHold() {
        // Arrange
        final Bucket<Biomass> bucketToAdd = Mockito.mock(Bucket.class);
        final Bucket<Biomass> updatedBucket = Mockito.mock(Bucket.class);

        Mockito.when(Bucket.<Biomass>empty()).thenReturn(Mockito.mock(Bucket.class));
        Mockito.when(Bucket.<Biomass>empty().add(bucketToAdd)).thenReturn(updatedBucket);

        final InfiniteHold<Biomass> hold = new InfiniteHold<>();

        // Act
        final Bucket<Biomass> result = hold.addContent(bucketToAdd);

        // Assert
        assertEquals(Bucket.empty(), result);
        assertEquals(updatedBucket, hold.getContent());
    }

    @Test
    void addContent_shouldLeaveHoldEmpty_whenAddingEmptyBucket() {
        // Arrange
        final Bucket<Biomass> emptyBucket = Mockito.mock(Bucket.class);
        Mockito.when(emptyBucket.isEmpty()).thenReturn(true);
        Mockito.when(Bucket.<Biomass>empty()).thenReturn(emptyBucket);

        final InfiniteHold<Biomass> hold = new InfiniteHold<>();

        // Act
        final Bucket<Biomass> result = hold.addContent(emptyBucket);

        // Assert
        assertEquals(emptyBucket, result);
        assertTrue(hold.getContent().isEmpty());
    }

    @Test
    void getContent_shouldReturnCurrentHoldContents() {
        // Arrange
        final Bucket<Biomass> someBucket = Mockito.mock(Bucket.class);

        final InfiniteHold<Biomass> hold = new InfiniteHold<>();
        hold.addContent(someBucket); // Add some content

        // Act
        final Bucket<Biomass> content = hold.getContent();

        // Assert
        assertEquals(someBucket, content);
    }

    @Test
    void removeContent_shouldClearTheHoldAndReturnExistingContents() {
        // Arrange
        final Bucket<Biomass> someBucket = Mockito.mock(Bucket.class);
        final Bucket<Biomass> emptyBucket = Mockito.mock(Bucket.class);

        Mockito.when(Bucket.<Biomass>empty()).thenReturn(emptyBucket);

        final InfiniteHold<Biomass> hold = new InfiniteHold<>();
        hold.addContent(someBucket); // Add some content

        // Act
        final Bucket<Biomass> removedContent = hold.removeContent();

        // Assert
        assertEquals(someBucket, removedContent);
        assertEquals(emptyBucket, hold.getContent()); // Hold should now be empty
    }

    @Test
    void removeContent_shouldReturnEmptyBucket_whenHoldIsInitiallyEmpty() {
        // Arrange
        final Bucket<Biomass> emptyBucket = Mockito.mock(Bucket.class);
        Mockito.when(Bucket.<Biomass>empty()).thenReturn(emptyBucket);

        final InfiniteHold<Biomass> hold = new InfiniteHold<>();

        // Act
        final Bucket<Biomass> removedContent = hold.removeContent();

        // Assert
        assertEquals(emptyBucket, removedContent);
        assertTrue(hold.getContent().isEmpty());
    }
}
