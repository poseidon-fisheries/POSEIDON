/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package spikes;

import org.junit.Assert;
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


    @org.junit.Test
    public void canRead() throws Exception {


        final URL resource = getClass().getClassLoader().getResource("5by5.asc");
        Assert.assertNotNull("can't open the inputStream!", resource);
        final FileInputStream inputStream = new FileInputStream(resource.getFile());

        final GeomGridField field = new GeomGridField();
        ArcInfoASCGridImporter.read(inputStream, GeomGridField.GridDataType.DOUBLE, field);

        //the dimensions are correct
        Assert.assertEquals(field.getGridHeight(), 5);
        Assert.assertEquals(field.getGridWidth(), 5);

        //randomly check cells, make sure they are correct
        final DoubleGrid2D grid = (DoubleGrid2D) field.getGrid();
        //notice that this being java the first element is at 0,0 while in R it is in 1,1
        // so 4,4 here is 5,5 there
        Assert.assertEquals(grid.field[0][0], -10, .01);
        Assert.assertEquals(grid.field[4][4], 10, .01);


    }
}
