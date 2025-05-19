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

package uk.ac.ox.poseidon.geography.distance;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class HaversineDistanceCalculatorTest {

    @Test
    void testDistanceBetweenSamePoints() {
        final ModelGrid mockModelGrid = mock(ModelGrid.class);
        final HaversineDistanceCalculator calculator = new HaversineDistanceCalculator(
            mockModelGrid);

        // Coordinates for London (long, lat) but here both points are the same
        final Coordinate point = new Coordinate(-0.1278, 51.5074);
        final double result = calculator.distanceInKm(point, point);

        assertEquals(
            0.0, result, 0.0001,
            "Distance between the same points should be zero."
        );
    }

    @Test
    void testDistanceBetweenKnownPoints() {
        final ModelGrid mockModelGrid = mock(ModelGrid.class);
        final HaversineDistanceCalculator calculator = new HaversineDistanceCalculator(
            mockModelGrid);

        // London (longitude, latitude) => (-0.1278, 51.5074)
        final Coordinate london = new Coordinate(-0.1278, 51.5074);
        // Paris  (longitude, latitude) => (2.3522, 48.8566)
        final Coordinate paris = new Coordinate(2.3522, 48.8566);

        final double result = calculator.distanceInKm(london, paris);
        final double expectedDistance = 343.9; // Approximate distance in kilometers

        assertEquals(
            expectedDistance,
            result,
            1.0,
            "Distance between London and Paris should be approximately 343.9 km."
        );
    }

    @Test
    void testDistanceAcrossEquator() {
        final ModelGrid mockModelGrid = mock(ModelGrid.class);
        final HaversineDistanceCalculator calculator = new HaversineDistanceCalculator(
            mockModelGrid);

        // 1 degree north and south of equator, same longitude (37.0)
        final Coordinate pointNorth = new Coordinate(37.0, 1.0);
        final Coordinate pointSouth = new Coordinate(37.0, -1.0);

        final double result = calculator.distanceInKm(pointNorth, pointSouth);
        final double expectedDistance = 222.4;

        assertEquals(
            expectedDistance,
            result,
            1.0,
            "Distance across the equator should be approximately 222.4 km."
        );
    }

    @Test
    void testDistanceBetweenDistantPoints() {
        final ModelGrid mockModelGrid = mock(ModelGrid.class);
        final HaversineDistanceCalculator calculator = new HaversineDistanceCalculator(
            mockModelGrid);

        // New York  => longitude = -74.0060, latitude = 40.7128
        final Coordinate newYork = new Coordinate(-74.0060, 40.7128);
        // Sydney    => longitude = 151.2093, latitude = -33.8688
        final Coordinate sydney = new Coordinate(151.2093, -33.8688);

        final double result = calculator.distanceInKm(newYork, sydney);
        final double expectedDistance = 15988.8; // Approximate distance in kilometers

        assertEquals(
            expectedDistance,
            result,
            50.0,
            "Distance between New York and Sydney should be approximately 15,988.8 km."
        );
    }

    @Test
    void testDistanceForExtremeCoordinates() {
        final ModelGrid mockModelGrid = mock(ModelGrid.class);
        final HaversineDistanceCalculator calculator = new HaversineDistanceCalculator(
            mockModelGrid);

        // North Pole => (longitude = 0.0, latitude = 90.0)
        final Coordinate northPole = new Coordinate(0.0, 90.0);
        // South Pole => (longitude = 0.0, latitude = -90.0)
        final Coordinate southPole = new Coordinate(0.0, -90.0);

        final double result = calculator.distanceInKm(northPole, southPole);
        final double expectedDistance = 20015.1; // Half Earth's approximate circumference

        assertEquals(
            expectedDistance,
            result,
            100.0,
            "Distance between North Pole and South Pole should be approximately 20,015.1 km."
        );
    }

    @Property
    void distanceSymmetry(
        @ForAll @DoubleRange(min = -180, max = 180) final double lon1,
        @ForAll @DoubleRange(min = -90, max = 90) final double lat1,
        @ForAll @DoubleRange(min = -180, max = 180) final double lon2,
        @ForAll @DoubleRange(min = -90, max = 90) final double lat2
    ) {
        final ModelGrid mockModelGrid = mock(ModelGrid.class);
        final HaversineDistanceCalculator calculator =
            new HaversineDistanceCalculator(mockModelGrid);
        final Coordinate pointA = new Coordinate(lon1, lat1);
        final Coordinate pointB = new Coordinate(lon2, lat2);
        assertTrue(
            calculator
                .distance(pointA, pointB)
                .isEquivalentTo(calculator.distance(pointB, pointA))
        );
    }
}
