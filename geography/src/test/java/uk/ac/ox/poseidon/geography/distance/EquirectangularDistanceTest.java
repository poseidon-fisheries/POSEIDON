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

package uk.ac.ox.poseidon.geography.distance;

import com.vividsolutions.jts.geom.Coordinate;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EquirectangularDistanceTest {

    private final EquirectangularDistance equirectangularDistance =
        new EquirectangularDistance(null);

    @Test
    public void testDistanceBetweenDifferentPoints() {
        final Coordinate pointA = new Coordinate(-1.254, 51.751); // Example coordinate (Oxford)
        final Coordinate pointB = new Coordinate(-0.1278, 51.5074); // Example coordinate (London)

        final double expectedDistance =
            82.0; // Approximate distance between Oxford and London in km
        final double actualDistance = equirectangularDistance.distanceBetween(pointA, pointB);

        assertEquals(
            expectedDistance,
            actualDistance,
            5.0
        ); // Allowing some tolerance for approximation
    }

    @Test
    public void testDistanceBetweenAcrossEquator() {
        final Coordinate pointA = new Coordinate(-79.3832, 43.6532); // Toronto
        // Hypothetical point in the Southern Hemisphere
        final Coordinate pointB = new Coordinate(-79.3832, -43.6532);

        final double expectedDistance = 9710.0; // Approximate distance in km
        final double actualDistance = equirectangularDistance.distanceBetween(pointA, pointB);

        assertEquals(expectedDistance, actualDistance, 5); // Allowing some tolerance
    }

    @Test
    public void otherCoordinates() {
        assertEquals(
            427.1,
            equirectangularDistance.distanceBetween(
                new Coordinate(114.407067850409, -6.7958646820027),
                new Coordinate(118.274833029541, -6.7958646820027)
            ),
            .1
        );
    }

    @Property
    void samePointAlwaysZero(
        @ForAll @DoubleRange(min = -180, max = 180) final double lon,
        @ForAll @DoubleRange(min = -90, max = 90) final double lat
    ) {
        final Coordinate point = new Coordinate(lon, lat);
        final double distance = equirectangularDistance.distanceBetween(point, point);
        assertEquals(0.0, distance, 0.0001);
    }

    @Property
    void distanceBetweenSymmetry(
        @ForAll @DoubleRange(min = -180, max = 180) final double lon1,
        @ForAll @DoubleRange(min = -90, max = 90) final double lat1,
        @ForAll @DoubleRange(min = -180, max = 180) final double lon2,
        @ForAll @DoubleRange(min = -90, max = 90) final double lat2
    ) {
        final Coordinate pointA = new Coordinate(lon1, lat1);
        final Coordinate pointB = new Coordinate(lon2, lat2);

        final double distanceAB = equirectangularDistance.distanceBetween(pointA, pointB);
        final double distanceBA = equirectangularDistance.distanceBetween(pointB, pointA);

        assertEquals(distanceAB, distanceBA, 0.0001);
    }
}
