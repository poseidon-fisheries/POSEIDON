/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.utility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;


/**
 * Test reading the california1000.asc file. Very much like ReadASCSpike
 */
public class GISReadersTest {


    @Test
    public void testName() throws Exception {

        final GeomGridField field = GISReaders.readRaster("test.asc");

        //the dimensions are correct
        Assertions.assertEquals(field.getGridHeight(), 36);
        Assertions.assertEquals(field.getGridWidth(), 72);

        //randomly check cells, make sure they are correct
        final DoubleGrid2D grid = (DoubleGrid2D) field.getGrid();
        //notice two things: (1) get() works exactly like field[][]
        //(2) this being java the first element is at 0,0 while in R it is in 1,1
        // so 10,100 here is 101,11 there
        Assertions.assertEquals(grid.field[0][0], 1, .01);
        Assertions.assertEquals(grid.get(0, 0), 1, .01);
        Assertions.assertEquals(grid.field[1][0], 2, .01);
        Assertions.assertEquals(grid.get(1, 0), 2, .01);

    }
}
