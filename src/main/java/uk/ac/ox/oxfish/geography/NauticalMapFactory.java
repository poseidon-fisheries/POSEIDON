package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Envelope;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.utility.GISReaders;

/**
 * A bunch of static methods that build maps
 * Created by carrknight on 4/10/15.
 */
public class NauticalMapFactory {


    public static NauticalMap fromBathymetryAndShapeFiles(String bathymetryResource, String... mpaSources)
    {
        //read raster bathymetry
        GeomGridField temporaryField = GISReaders.readRaster(bathymetryResource);
        DoubleGrid2D temporaryGrid = (DoubleGrid2D)temporaryField.getGrid(); //cast cast cast. Welcome to mason
        //now turn it into a grid of sea tiles
        ObjectGrid2D rasterBackingGrid = new ObjectGrid2D(temporaryField.getGridWidth(),temporaryField.getGridHeight());
        for(int i=0;i<rasterBackingGrid.getWidth(); i++)
            for(int j=0; j<rasterBackingGrid.getHeight(); j++)
                rasterBackingGrid.field[i][j]=new SeaTile(i,j,temporaryGrid.field[i][j]);
        //now from this grid create the correct bathymetry object
        GeomGridField rasterBathymetry = new GeomGridField(rasterBackingGrid);
        rasterBathymetry.setPixelHeight(temporaryField.getPixelHeight());
        rasterBathymetry.setPixelWidth(temporaryField.getPixelWidth());
        rasterBathymetry.setMBR(temporaryField.getMBR());



        //read in MPAs
        GeomVectorField mpaVectorField = GISReaders.readShapeAndMergeWithRaster(rasterBathymetry, mpaSources);

        EquirectangularDistance distance = new EquirectangularDistance(temporaryField.toXCoord(0.5),
                temporaryField.getPixelHeight());

        return new NauticalMap(rasterBathymetry,mpaVectorField,distance);



    }




 /*   public GeomVectorField addCities(GeomGridField rasterBathymetry, String cityResources)
    {

        GeomVectorField cities = GISReaders.readShapeAndMergeWithRaster(rasterBathymetry,cityResources);
        //now transform the MasonGeometries into Cities
        Envelope savedMBR = new Envelope(cities.getMBR());
        Bag oldGeometries = new Bag(cities.getGeometries());
        cities.getGeometries().clear();
        for(Object old : oldGeometries)
        {
            MasonGeometry geometry = (MasonGeometry) old;

            cities.addGeometry(new City(geometry.getGeometry(),geometry.getStringAttribute("AREANAME"),
                    geometry.getIntegerAttribute("POP2000")));
        }

        return cities;

    }
    */

}
