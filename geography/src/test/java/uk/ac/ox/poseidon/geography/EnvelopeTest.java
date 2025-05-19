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

package uk.ac.ox.poseidon.geography;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnvelopeTest {

    /**
     * Tests for the static intersects(Coordinate p1, Coordinate p2, Coordinate q) method.
     */
    @Property
    public void testPropertyIntersects_PointInsideRectangle(
        @ForAll final double x1,
        @ForAll final double y1,
        @ForAll final double x2,
        @ForAll final double y2,
        @ForAll final double x,
        @ForAll final double y
    ) {
        // Arrange
        final Coordinate p1 = new Coordinate(Math.min(x1, x2), Math.min(y1, y2));
        final Coordinate p2 = new Coordinate(Math.max(x1, x2), Math.max(y1, y2));
        final Coordinate q = new Coordinate(x, y);

        // Act & Assert
        if (x >= p1.lon && x <= p2.lon && y >= p1.lat && y <= p2.lat) {
            // Point is inside the rectangle
            assertTrue(Envelope.intersects(p1, p2, q));
        } else {
            // Point is outside the rectangle
            assertFalse(Envelope.intersects(p1, p2, q));
        }
    }

    @Property
    public void testPropertyIntersects_PointOutsideRectangle(
        @ForAll final double x1,
        @ForAll final double y1,
        @ForAll final double x2,
        @ForAll final double y2,
        @ForAll final double x,
        @ForAll final double y
    ) {
        // Arrange
        final Coordinate p1 = new Coordinate(Math.min(x1, x2), Math.min(y1, y2));
        final Coordinate p2 = new Coordinate(Math.max(x1, x2), Math.max(y1, y2));
        final Coordinate q = new Coordinate(x, y);

        // Act & Assert
        if (x < p1.lon || x > p2.lon || y < p1.lat || y > p2.lat) {
            assertFalse(Envelope.intersects(p1, p2, q));
        } else {
            assertTrue(Envelope.intersects(p1, p2, q));
        }
    }

    @Test
    public void testIntersects_PointAtCorner() {
        // Arrange
        final Coordinate p1 = new Coordinate(0.0, 0.0);
        final Coordinate p2 = new Coordinate(10.0, 10.0);
        final Coordinate q = new Coordinate(10.0, 10.0);

        // Act & Assert
        assertTrue(Envelope.intersects(p1, p2, q));
    }

    @Test
    public void testIntersects_PointInvertedRectangle() {
        // Arrange
        final Coordinate p1 = new Coordinate(10.0, 10.0);
        final Coordinate p2 = new Coordinate(0.0, 0.0);
        final Coordinate q = new Coordinate(5.0, 5.0);

        // Act & Assert
        assertTrue(Envelope.intersects(p1, p2, q));
    }

    /**
     * Tests for the static intersects(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2)
     * method.
     */
    @Property
    public void testPropertyIntersects_TwoOverlappingRectangles(
        @ForAll final double x1,
        @ForAll final double y1,
        @ForAll final double x2,
        @ForAll final double y2,
        @ForAll final double x3,
        @ForAll final double y3,
        @ForAll final double x4,
        @ForAll final double y4
    ) {
        // Arrange
        final Coordinate p1 = new Coordinate(Math.min(x1, x2), Math.min(y1, y2));
        final Coordinate p2 = new Coordinate(Math.max(x1, x2), Math.max(y1, y2));
        final Coordinate q1 = new Coordinate(Math.min(x3, x4), Math.min(y3, y4));
        final Coordinate q2 = new Coordinate(Math.max(x3, x4), Math.max(y3, y4));

        final Envelope envelope1 = new Envelope(p1, p2);
        final Envelope envelope2 = new Envelope(q1, q2);

        // Act & Assert
        final boolean intersects = envelope1.intersects(envelope2);
        final boolean expected =
            !(q2.lon < p1.lon || q1.lon > p2.lon || q2.lat < p1.lat || q1.lat > p2.lat);

        assertEquals(intersects, expected);
    }

    @Test
    public void testIntersects_OneRectangleFullyInsideAnother() {
        // Arrange
        final Coordinate p1 = new Coordinate(0.0, 0.0);
        final Coordinate p2 = new Coordinate(10.0, 10.0);
        final Coordinate q1 = new Coordinate(2.0, 2.0);
        final Coordinate q2 = new Coordinate(8.0, 8.0);

        // Act & Assert
        assertTrue(Envelope.intersects(p1, p2, q1, q2));
    }

    @Test
    public void testIntersects_RectanglesTouchAtEdge() {
        // Arrange
        final Coordinate p1 = new Coordinate(0.0, 0.0);
        final Coordinate p2 = new Coordinate(10.0, 10.0);
        final Coordinate q1 = new Coordinate(10.0, 5.0);
        final Coordinate q2 = new Coordinate(15.0, 5.0);

        // Act & Assert
        assertTrue(Envelope.intersects(p1, p2, q1, q2));
    }

    @Test
    public void testIntersects_RectanglesTouchAtCorner() {
        // Arrange
        final Coordinate p1 = new Coordinate(0.0, 0.0);
        final Coordinate p2 = new Coordinate(10.0, 10.0);
        final Coordinate q1 = new Coordinate(10.0, 10.0);
        final Coordinate q2 = new Coordinate(15.0, 15.0);

        // Act & Assert
        assertTrue(Envelope.intersects(p1, p2, q1, q2));
    }

    @Test
    public void testIntersects_InvertedRectanglesOverlap() {
        // Arrange
        final Coordinate p1 = new Coordinate(10.0, 10.0);
        final Coordinate p2 = new Coordinate(0.0, 0.0);
        final Coordinate q1 = new Coordinate(15.0, 15.0);
        final Coordinate q2 = new Coordinate(5.0, 5.0);

        // Act & Assert
        assertTrue(Envelope.intersects(p1, p2, q1, q2));
    }
}
