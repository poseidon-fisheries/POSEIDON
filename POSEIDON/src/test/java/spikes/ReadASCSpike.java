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

package spikes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;
import sim.io.geo.ArcInfoASCGridImporter;

import java.io.FileInputStream;
import java.net.URL;

/**
 * a simple spike to open up california1000.asc
 * Created by carrknight on 3/29/15.
 */
public class ReadASCSpike {


    @Test
    public void canRead() throws Exception {


        final URL resource = getClass().getClassLoader().getResource("5by5.asc");
        Assertions.assertNotNull(resource, "can't open the inputStream!");
        final FileInputStream inputStream = new FileInputStream(resource.getFile());

        final GeomGridField field = new GeomGridField();
        ArcInfoASCGridImporter.read(inputStream, GeomGridField.GridDataType.DOUBLE, field);

        //the dimensions are correct
        Assertions.assertEquals(field.getGridHeight(), 5);
        Assertions.assertEquals(field.getGridWidth(), 5);

        //randomly check cells, make sure they are correct
        final DoubleGrid2D grid = (DoubleGrid2D) field.getGrid();
        //notice that this being java the first element is at 0,0 while in R it is in 1,1
        // so 4,4 here is 5,5 there
        Assertions.assertEquals(grid.field[0][0], -10, .01);
        Assertions.assertEquals(grid.field[4][4], 10, .01);


    }
}
