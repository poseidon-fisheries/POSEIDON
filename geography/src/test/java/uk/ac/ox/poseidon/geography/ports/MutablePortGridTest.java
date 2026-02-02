/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.geography.ports;

import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.DefaultBathymetricGrid;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class MutablePortGridTest {

    @Test
    void copyOfKeepsLocations() {
        final DefaultBathymetricGrid bathymetricGrid =
            new DefaultBathymetricGrid(new double[][]{
                {+1d, -1d, -1d},
                {+1d, +1d, -1d},
                {-1d, -1d, -1d},
            });
        final Port portA = new Port("PA", "Port A", new Int2D(0, 0));
        final Port portB = new Port("PB", "Port B", new Int2D(1, 1));
        final MutablePortGrid source = new MutablePortGrid(
            bathymetricGrid,
            Map.of(
                portA, new Coordinate(0.5, 2.5),
                portB, new Coordinate(1.5, 1.5)
            )
        );

        final MutablePortGrid copy = MutablePortGrid.copyOf(source);

        assertNotSame(source, copy);
        assertEquals(
            source.getPorts().collect(Collectors.toSet()),
            copy.getPorts().collect(Collectors.toSet())
        );
        Set.of(portA, portB).forEach(port ->
            assertEquals(source.getLocation(port), copy.getLocation(port))
        );
    }
}
