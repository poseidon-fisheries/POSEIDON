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

import org.junit.jupiter.api.Test;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;

import java.util.List;
import java.util.Set;

import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ModelGridTest {

    private final ModelGrid modelGrid =
        ModelGrid.withAllCellsActive(
            10,
            10,
            new Envelope(-5, 5, -5, 5)
        );

    @Test
    void getActiveNeighbours() {
        assertEquals(
            Set.of(
                new Int2D(4, 4),
                new Int2D(4, 5),
                new Int2D(4, 6),
                new Int2D(5, 4),
                new Int2D(6, 6),
                new Int2D(5, 6),
                new Int2D(6, 4),
                new Int2D(6, 5)
            ),
            Set.copyOf(modelGrid.getActiveNeighbours(new Int2D(5, 5)))
        );
        assertEquals(
            Set.of(
                new Int2D(0, 1),
                new Int2D(1, 0),
                new Int2D(1, 1)
            ),
            Set.copyOf(modelGrid.getActiveNeighbours(new Int2D(0, 0)))
        );
    }

    @Test
    void cellToCoordinate() {
        assertEquals(
            new Coordinate(-4.5, 4.5),
            modelGrid.toCoordinate(new Int2D(0, 0))
        );
        assertEquals(
            new Coordinate(4.5, -4.5),
            modelGrid.toCoordinate(new Int2D(9, 9))
        );
    }

    @Test
    void pointToCoordinate() {
        assertEquals(
            new Coordinate(-5, 5),
            modelGrid.toCoordinate(new Double2D(0, 0))
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> modelGrid.toCoordinate(new Double2D(10, 10)),
            "Double2D[10.0,10.0] outside of grid"
        );
    }

    @Test
    void coordinateToPoint() {
        assertEquals(
            new Double2D(0, 0),
            modelGrid.toPoint(new Coordinate(-5, 5))
        );
        assertEquals(
            new Double2D(10, 10),
            modelGrid.toPoint(new Coordinate(5, -5))
        );
    }

    @Test
    void coordinateToCell() {
        assertEquals(
            new Int2D(0, 0),
            modelGrid.toCell(new Coordinate(-5, 5))
        );
        assertEquals(
            new Int2D(0, 0),
            modelGrid.toCell(new Coordinate(-4.5, 4.5))
        );
        assertEquals(
            new Int2D(9, 9),
            modelGrid.toCell(new Coordinate(4.5, -4.5))
        );
        assertEquals(
            new Int2D(10, 10),
            modelGrid.toCell(new Coordinate(5, -5))
        );
    }

    @Test
    void testCoordToXyAndBack() {
        final Simulation simulation = mock(Simulation.class);
        final ModelGrid modelGrid =
            new ModelGridFactory(
                1.0, -171, -70, -50, 50
            ).get(simulation);
        final Envelope envelope = modelGrid.getEnvelope();
        final List<Coordinate> coordinates =
            range((int) envelope.getMinX(), (int) envelope.getMaxX() - 1)
                .mapToObj(x -> x + 0.5)
                .flatMap(x ->
                    range((int) envelope.getMinY(), (int) envelope.getMaxY() - 1)
                        .mapToObj(y -> y + 0.5)
                        .map(y -> new Coordinate(x, y))
                ).toList();

        coordinates.forEach(coordinate -> {
            final Double2D xy = modelGrid.toPoint(coordinate);
            assertEquals(
                coordinate,
                modelGrid.toCoordinate(new Int2D((int) xy.x, (int) xy.y)),
                coordinate.toString()
            );
        });
    }

}
