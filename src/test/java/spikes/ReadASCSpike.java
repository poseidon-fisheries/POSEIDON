package spikes;

import org.junit.Assert;
import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.Grid2D;
import sim.io.geo.ArcInfoASCGridImporter;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

/**
 * a simple spike to open up california1000.asc
 * Created by carrknight on 3/29/15.
 */
public class ReadASCSpike
{


    @org.junit.Test
    public void canRead() throws Exception {


        URL resource = getClass().getClassLoader().getResource("california1000.asc");
        Assert.assertNotNull("can't open the inputStream!", resource);
        FileInputStream inputStream = new FileInputStream(resource.getFile());

        GeomGridField field = new GeomGridField();
        ArcInfoASCGridImporter.read(inputStream, GeomGridField.GridDataType.DOUBLE, field);

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
