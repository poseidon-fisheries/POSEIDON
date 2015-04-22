package spikes;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import org.junit.Assert;
import org.junit.Test;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.io.geo.ShapeFileImporter;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.utility.GISReaders;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple test to show that I can read shape files
 * Created by carrknight on 3/30/15.
 */
public class ReadShpSpike
{

    @Test
    public void readsInCorrectly() throws Exception {
        GeomVectorField vectorField = readIn();


        //should read all the features!
        Assert.assertEquals(1, vectorField.getGeometries().size());

    }

    private GeomVectorField readIn() throws FileNotFoundException {
        //grab the file
        URL resource = getClass().getClassLoader().getResource("fakempa.shp");
        if(resource == null)
            throw new NullPointerException("Resource is null");

        GeomVectorField vectorField = new GeomVectorField();

        //read it in!
        ShapeFileImporter.read(resource, vectorField);
        return vectorField;
    }


/*
    private static final ArrayList<String> landReserves = new ArrayList<>();
    static {
        landReserves.add("Elkhorn Slough"); //natural park
        landReserves.add("Morro Bay"); //wetlands
        landReserves.add("Moro Cojo Slough"); //slough
        landReserves.add("Natural Bridges SMR "); //centroid is in land even though this is marine
    }
    */
    @Test
    public  void correctDepth() throws FileNotFoundException {

        //read in the mpas
        GeomVectorField vectorField = readIn();
        //read in the grid
        GeomGridField grid = GISReaders.readRaster("test.asc");
        //synchronize MBRs
        Envelope globalMBR = vectorField.getMBR();
        globalMBR.expandToInclude(grid.getMBR());
        grid.setMBR(globalMBR);
        vectorField.setMBR(globalMBR);


        for(Object geo : vectorField.getGeometries())
        {
            MasonGeometry mpa = (MasonGeometry) geo; //need to cast it


            Point centroid = mpa.getGeometry().getCentroid();
            int x = grid.toXCoord(centroid.getX());
            int y = grid.toYCoord(centroid.getY());
            double depth = ((DoubleGrid2D)grid.getGrid()).get(x,y);
            System.out.println(depth);
            Assert.assertEquals(1333,depth,.01);
        }





    }
}
