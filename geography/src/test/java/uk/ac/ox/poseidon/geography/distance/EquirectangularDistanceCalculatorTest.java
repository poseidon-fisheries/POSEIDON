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

package uk.ac.ox.poseidon.geography.distance;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.Coordinate;

import javax.measure.Quantity;
import java.util.logging.Filter;
import java.util.logging.LogManager;

import static javax.measure.MetricPrefix.KILO;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.METRE;

class EquirectangularDistanceCalculatorTest {

    private final EquirectangularDistanceCalculator equirectangularDistance =
        new EquirectangularDistanceCalculator(null);

    @Test
    void testDistanceDifferentPoints() {
        final Coordinate oxford = new Coordinate(-1.254, 51.751);
        final Coordinate london = new Coordinate(-0.1278, 51.5074);
        assertEquals(
            getQuantity(82, KILO(METRE)),
            equirectangularDistance.distance(oxford, london),
            getQuantity(350, METRE)
        );
    }

    @Test
    void testDistanceAcrossEquator() {
        final Coordinate pointA = new Coordinate(0, 1);
        final Coordinate pointB = new Coordinate(0, -1);

        assertEquals(
            getQuantity(222, KILO(METRE)),
            equirectangularDistance.distance(pointA, pointB),
            getQuantity(500, METRE)
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
            getQuantity(106, KILO(METRE)),
            equirectangularDistance.distance(
                new Coordinate(-1.256, 51.7522), // Oxford
                new Coordinate(0.1167, 52.2) // Cambridge
            ),
            getQuantity(500, METRE)
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
                .distance(point, point)
                .isEquivalentTo(getQuantity(0, METRE))
        );
    }

    @Property
    void distanceSymmetry(
        @ForAll @DoubleRange(min = -180, max = 180) final double lon1,
        @ForAll @DoubleRange(min = -90, max = 90) final double lat1,
        @ForAll @DoubleRange(min = -180, max = 180) final double lon2,
        @ForAll @DoubleRange(min = -90, max = 90) final double lat2
    ) {
        final Coordinate pointA = new Coordinate(lon1, lat1);
        final Coordinate pointB = new Coordinate(lon2, lat2);

        // Retrieve the underlying java.util.logging.Logger
        final java.util.logging.Logger julLogger = LogManager
            .getLogManager()
            .getLogger(EquirectangularDistanceCalculator.class.getName());

        // Save the current log filter and apply a new one that suppresses warnings
        final Filter originalFilter = julLogger.getFilter();
        julLogger.setFilter(record ->
            record.getLevel().intValue() > java.util.logging.Level.WARNING.intValue()
        );
        
        assertTrue(
            equirectangularDistance
                .distance(pointA, pointB)
                .isEquivalentTo(equirectangularDistance.distance(pointB, pointA))
        );
        // Restore the original filter after the test
        julLogger.setFilter(originalFilter);
    }
}
