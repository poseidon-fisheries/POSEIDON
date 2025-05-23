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

package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.field.geo.GeomGridField;
import sim.field.grid.ObjectGrid2D;
import sim.util.Double2D;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;
import uk.ac.ox.poseidon.common.core.geography.MapExtentFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class MapExtentTest {

    /**
     * Check that MapExtent's mapping of coordinates to grid cell matches that of GeomGridField
     */
    @Test
    public void testCoords() {
        // Note: this would be a good candidate for property based testing
        final Envelope envelope = new Envelope(-5, 5, -5, 5);
        final MapExtent mapExtent = MapExtent.from(10, 10, envelope);
        final GeomGridField geomGridField = new GeomGridField(new ObjectGrid2D(10, 10));
        geomGridField.setMBR(envelope);

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                Assertions.assertEquals(geomGridField.toXCoord(x), mapExtent.toGridX(x));
                Assertions.assertEquals(geomGridField.toYCoord(y), mapExtent.toGridY(y));
            }
        }

    }

    @Test
    public void testCoordToXyAndBack() {
        final MapExtent mapExtent = new MapExtentFactory(
            101, 100, -171, -70, -50, 50
        ).get();
        final Envelope envelope = mapExtent.getEnvelope();
        final List<Coordinate> coordinates =
            range((int) envelope.getMinX(), (int) envelope.getMaxX() - 1)
                .mapToObj(x -> x + 0.5)
                .flatMap(x ->
                    range((int) envelope.getMinY(), (int) envelope.getMaxY() - 1)
                        .mapToObj(y -> y + 0.5)
                        .map(y -> new Coordinate(x, y))
                ).collect(toList());

        coordinates.forEach(coordinate -> {
            final Double2D xy = mapExtent.coordinateToXY(coordinate);
            Assertions.assertEquals(
                coordinate,
                mapExtent.getCoordinates((int) xy.x, (int) xy.y),
                coordinate.toString()
            );
        });
    }
}
