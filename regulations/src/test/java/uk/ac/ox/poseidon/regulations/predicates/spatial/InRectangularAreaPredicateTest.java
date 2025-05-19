/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.regulations.predicates.spatial;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.predicates.InRectangularAreaPredicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InRectangularAreaPredicateTest {

    /**
     * Tests the {@code InRectangularAreaPredicate.test(Coordinate)} method. The method checks if a
     * given {@link Coordinate} lies within the specified {@link Envelope}.
     */

    @Test
    void test_GivenCoordinateInsideEnvelope_ShouldReturnTrue() {
        // Arrange
        final Envelope envelope = mock(Envelope.class);
        final Coordinate coordinate = new Coordinate(1.0, 1.0);
        when(envelope.contains(coordinate)).thenReturn(true);

        final InRectangularAreaPredicate predicate = new InRectangularAreaPredicate(envelope);

        // Act
        final boolean result = predicate.test(coordinate);

        // Assert
        assertTrue(result, "Expected coordinate to be inside the envelope, but it was not.");
    }

    @Test
    void test_GivenCoordinateOutsideEnvelope_ShouldReturnFalse() {
        // Arrange
        final Envelope envelope = mock(Envelope.class);
        final Coordinate coordinate = new Coordinate(2.0, 2.0);
        when(envelope.contains(coordinate)).thenReturn(false);

        final InRectangularAreaPredicate predicate = new InRectangularAreaPredicate(envelope);

        // Act
        final boolean result = predicate.test(coordinate);

        // Assert
        assertFalse(result, "Expected coordinate to be outside the envelope, but it was inside.");
    }

    @Test
    void test_GivenNullCoordinate_ShouldReturnFalse() {
        // Arrange
        final Envelope envelope = mock(Envelope.class);
        final Coordinate coordinate = null;

        final InRectangularAreaPredicate predicate = new InRectangularAreaPredicate(envelope);

        // Act
        final boolean result = predicate.test(coordinate);

        // Assert
        assertFalse(result, "Expected null coordinate to be outside the envelope, but it was not.");
    }
}
