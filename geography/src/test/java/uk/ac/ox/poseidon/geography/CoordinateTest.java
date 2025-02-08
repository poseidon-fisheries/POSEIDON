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

package uk.ac.ox.poseidon.geography;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    @Test
    void testFromJTSWithValidInput() {
        // Arrange
        final double x = 1.23;
        final double y = 4.56;
        final com.vividsolutions.jts.geom.Coordinate jtsCoordinate =
            new com.vividsolutions.jts.geom.Coordinate(x, y);

        // Act
        final Coordinate result = Coordinate.fromJTS(jtsCoordinate);

        // Assert
        assertNotNull(result, "The result should not be null");
        assertEquals(x, result.getLon(), "Longitude was not set correctly");
        assertEquals(y, result.getLat(), "Latitude was not set correctly");
    }

    @Test
    void testFromJTSWithExtremelyLargeValues() {
        // Arrange
        final double x = Double.MAX_VALUE;
        final double y = -Double.MAX_VALUE;
        final com.vividsolutions.jts.geom.Coordinate jtsCoordinate =
            new com.vividsolutions.jts.geom.Coordinate(x, y);

        // Act
        final Coordinate result = Coordinate.fromJTS(jtsCoordinate);

        // Assert
        assertNotNull(result, "The result should not be null");
        assertEquals(x, result.getLon(), "Longitude was not set correctly with large values");
        assertEquals(y, result.getLat(), "Latitude was not set correctly with large values");
    }

    @Test
    void testFromJTSWithZeroCoordinates() {
        // Arrange
        final double x = 0.0;
        final double y = 0.0;
        final com.vividsolutions.jts.geom.Coordinate jtsCoordinate =
            new com.vividsolutions.jts.geom.Coordinate(x, y);

        // Act
        final Coordinate result = Coordinate.fromJTS(jtsCoordinate);

        // Assert
        assertNotNull(result, "The result should not be null");
        assertEquals(x, result.getLon(), "Longitude was not set correctly");
        assertEquals(y, result.getLat(), "Latitude was not set correctly");
    }

    @Test
    void testFromJTSWithNegativeCoordinates() {
        // Arrange
        final double x = -12.34;
        final double y = -56.78;
        final com.vividsolutions.jts.geom.Coordinate jtsCoordinate =
            new com.vividsolutions.jts.geom.Coordinate(x, y);

        // Act
        final Coordinate result = Coordinate.fromJTS(jtsCoordinate);

        // Assert
        assertNotNull(result, "The result should not be null");
        assertEquals(x, result.getLon(), "Longitude was not set correctly");
        assertEquals(y, result.getLat(), "Latitude was not set correctly");
    }

    @Test
    void testFromJTSWithNullInput() {
        // Act & Assert
        assertThrows(
            NullPointerException.class, () -> Coordinate.fromJTS(null),
            "Passing a null JTSCoordinate should throw NullPointerException"
        );
    }
}
