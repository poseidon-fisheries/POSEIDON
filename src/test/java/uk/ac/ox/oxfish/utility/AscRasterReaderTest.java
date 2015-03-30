package uk.ac.ox.oxfish.utility;

import junit.framework.TestCase;
import org.junit.Assert;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;


/**
 * Test reading the california1000.asc file. Very much like ReadASCSpike
 */
public class AscRasterReaderTest extends TestCase
{


    public void testName() throws Exception {

        GeomGridField field = AscRasterReader.read("california1000.asc");

        //the dimensions are correct
        Assert.assertEquals(field.getGridHeight(), 1887);
        Assert.assertEquals(field.getGridWidth(), 1000);

        //randomly check cells, make sure they are correct
        DoubleGrid2D grid = (DoubleGrid2D) field.getGrid();
        //notice two things: (1) columns come first here (so the opposite of cartesian)
        //(2) this being java the first element is at 0,0 while in R it is in 1,1
        // so 10,100 here is 101,11 there
        Assert.assertEquals(grid.field[10][100], -1046.217,.01);
        Assert.assertEquals(grid.field[100][10], -19.68794,.01);
    }
}