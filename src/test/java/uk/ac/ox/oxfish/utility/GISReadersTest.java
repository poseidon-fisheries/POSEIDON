package uk.ac.ox.oxfish.utility;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;


/**
 * Test reading the california1000.asc file. Very much like ReadASCSpike
 */
public class GISReadersTest
{


    @Test
    public void testName() throws Exception {

        GeomGridField field = GISReaders.readRaster("test.asc");

        //the dimensions are correct
        Assert.assertEquals(field.getGridHeight(), 36);
        Assert.assertEquals(field.getGridWidth(), 72);

        //randomly check cells, make sure they are correct
        DoubleGrid2D grid = (DoubleGrid2D) field.getGrid();
        //notice two things: (1) get() works exactly like field[][]
        //(2) this being java the first element is at 0,0 while in R it is in 1,1
        // so 10,100 here is 101,11 there
        Assert.assertEquals(grid.field[0][0], 1,.01);
        Assert.assertEquals(grid.get(0,0), 1,.01);
        Assert.assertEquals(grid.field[1][0], 2,.01);
        Assert.assertEquals(grid.get(1,0),2,.01);

    }
}