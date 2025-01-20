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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.measure.Quantity;

import static javax.measure.MetricPrefix.KILO;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.METRE;

class EquirectangularDistanceTest {

    private final EquirectangularDistance equirectangularDistance =
        new EquirectangularDistance(null);

    @Test
    void testDistanceBetweenDifferentPoints() {
        final Coordinate oxford = new Coordinate(-1.254, 51.751);
        final Coordinate london = new Coordinate(-0.1278, 51.5074);
        assertEquals(
            getQuantity(82, KILO(METRE)),
            equirectangularDistance.distanceBetween(oxford, london),
            getQuantity(350, METRE)
        );
    }

    @Test
    void testDistanceBetweenAcrossEquator() {
        final Coordinate pointA = new Coordinate(-79.3832, 43.6532); // Toronto
        // Hypothetical point in the Southern Hemisphere
        final Coordinate pointB = new Coordinate(-79.3832, -43.6532);

        assertEquals(
            getQuantity(9711, KILO(METRE)),
            equirectangularDistance.distanceBetween(pointA, pointB),
            getQuantity(1, KILO(METRE))
        );
    }

    private <Q extends Quantity<Q>> void assertEquals(
        final Quantity<Q> expected,
        final Quantity<Q> actual,
        final Quantity<Q> delta
    ) {
        Assertions.assertEquals(
            expected.toSystemUnit().getValue().doubleValue(),
            actual.toSystemUnit().getValue().doubleValue(),
            delta.toSystemUnit().getValue().doubleValue(),
            () -> actual + " is not within " + delta + " of " + expected
        );
    }

    @Test
    void otherCoordinates() {
        assertEquals(
            getQuantity(427.2, KILO(METRE)),
            equirectangularDistance.distanceBetween(
                new Coordinate(114.407067850409, -6.7958646820027),
                new Coordinate(118.274833029541, -6.7958646820027)
            ),
            getQuantity(15, METRE)
        );
    }

    @Property
    void samePointAlwaysZero(
        @ForAll @DoubleRange(min = -180, max = 180) final double lon,
        @ForAll @DoubleRange(min = -90, max = 90) final double lat
    ) {
        final Coordinate point = new Coordinate(lon, lat);
        assertTrue(
            equirectangularDistance
                .distanceBetween(point, point)
                .isEquivalentTo(getQuantity(0, METRE))
        );
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
        assertTrue(
            equirectangularDistance
                .distanceBetween(pointA, pointB)
                .isEquivalentTo(equirectangularDistance.distanceBetween(pointB, pointA))
        );
    }
}
