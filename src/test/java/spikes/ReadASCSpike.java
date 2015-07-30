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
public class ReadASCSpike
{


    @org.junit.Test
    public void canRead() throws Exception {


        URL resource = getClass().getClassLoader().getResource("5by5.asc");
        Assert.assertNotNull("can't open the inputStream!", resource);
        FileInputStream inputStream = new FileInputStream(resource.getFile());

        GeomGridField field = new GeomGridField();
        ArcInfoASCGridImporter.read(inputStream, GeomGridField.GridDataType.DOUBLE, field);

        //the dimensions are correct
        Assert.assertEquals(field.getGridHeight(), 5);
        Assert.assertEquals(field.getGridWidth(), 5);

        //randomly check cells, make sure they are correct
        DoubleGrid2D grid = (DoubleGrid2D) field.getGrid();
        //notice that this being java the first element is at 0,0 while in R it is in 1,1
        // so 4,4 here is 5,5 there
        Assert.assertEquals(grid.field[0][0], -10,.01);
        Assert.assertEquals(grid.field[4][4],10,.01);


    }
}
