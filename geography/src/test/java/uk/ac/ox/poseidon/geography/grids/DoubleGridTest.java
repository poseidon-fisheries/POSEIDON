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

package uk.ac.ox.poseidon.geography.grids;

import com.vividsolutions.jts.geom.Envelope;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Coordinate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoubleGridTest {

    private final DoubleGrid grid =
        new DoubleGrid(new double[][]{
            {1d, 2d, 3d},
            {4d, 5d, 6d},
            {7d, 8d, 9d},
        });

    @Test
    void getValue() {
        assertEquals(1.0, grid.getValue(new Int2D(0, 0)));
        assertEquals(2.0, grid.getValue(new Int2D(0, 1)));
        assertEquals(4.0, grid.getValue(new Int2D(1, 0)));
        assertEquals(9.0, grid.getValue(new Int2D(2, 2)));
    }

    @Test
    void getMinimumValue() {
        assertEquals(1.0, grid.getMinimumValue());
    }

    @Test
    void getMaximumValue() {
        assertEquals(9.0, grid.getMaximumValue());
    }

    @Test
    void testGridExtent() {
        final GridExtent gridExtent = grid.getGridExtent();
        assertEquals(3, gridExtent.getGridWidth());
        assertEquals(3, gridExtent.getGridHeight());
        assertEquals(
            List.of(
                new Coordinate(0.5, 2.5),
                new Coordinate(0.5, 1.5),
                new Coordinate(0.5, 0.5),
                new Coordinate(1.5, 2.5),
                new Coordinate(1.5, 1.5),
                new Coordinate(1.5, 0.5),
                new Coordinate(2.5, 2.5),
                new Coordinate(2.5, 1.5),
                new Coordinate(2.5, 0.5)
            ),
            gridExtent.getAllCoordinates()
        );
    }

    @Test
    void testEnvelope() {
        final Envelope envelope = grid.getGridExtent().getEnvelope();
        assertEquals(3, envelope.getWidth());
        assertEquals(3, envelope.getHeight());
    }
}
