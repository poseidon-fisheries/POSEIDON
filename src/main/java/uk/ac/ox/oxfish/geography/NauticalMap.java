package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.utility.GISReaders;

/**
 * This object stores the map/chart of the sea. It contains all the geometric fields holding locations and boundaries
 * Created by carrknight on 4/2/15.
 */
public class NauticalMap
{
    /**
     * this holds the bathymetry raster grid
     */
    private GeomGridField rasterBathymetry;

    /**
     * the proper grid behind raster bathymetry. You can get it by rasterBathymetry.getGrid() and cast but this skips the
     * casting
     */
    private ObjectGrid2D rasterBackingGrid;

    /**
     * todo move to parameter list
     */
    final static private String DEFAULT_BATHYMETRY_SOURCE = "california1000.asc";


    /**
     * holds the MPAs
     */
    private GeomVectorField mpaVectorField;

    /**
     * holds the cities
     */
    private GeomVectorField cities = new GeomVectorField();

    /**
     * todo move to parameter list
     */
    final static private String[] DEFAULT_MPA_SOURCES = {"cssr_mpa/reprojected/mpa_central.shp",
            "ncssr_mpa/reprojected/mpa_north.shp"};



    public void initializeWithDefaultValues()
    {
        initialize( DEFAULT_BATHYMETRY_SOURCE, DEFAULT_MPA_SOURCES);
    }

    public  void initialize(String bathymetryResource, String... mpaSources)
    {
        //read raster bathymetry
        GeomGridField temporaryField = GISReaders.readRaster(bathymetryResource);
        DoubleGrid2D temporaryGrid = (DoubleGrid2D)temporaryField.getGrid(); //cast cast cast. Welcome to mason
        //now turn it into a grid of sea tiles
        rasterBackingGrid = new ObjectGrid2D(temporaryField.getGridWidth(),temporaryField.getGridHeight());
        for(int i=0;i<rasterBackingGrid.getWidth(); i++)
            for(int j=0; j<rasterBackingGrid.getHeight(); j++)
                rasterBackingGrid.field[i][j]=new SeaTile(i,j,temporaryGrid.field[i][j]);
        //now from this grid create the correct bathymetry object
        rasterBathymetry = new GeomGridField(rasterBackingGrid);
        rasterBathymetry.setPixelHeight(temporaryField.getPixelHeight());
        rasterBathymetry.setPixelWidth(temporaryField.getPixelWidth());
        rasterBathymetry.setMBR(temporaryField.getMBR());



        //read in MPAs
        mpaVectorField = GISReaders.readShapeAndMergeWithRaster(rasterBathymetry, mpaSources);

        //now go again through all the grid and set tiles as protected if need be
        //this might take a while but that's why we do it here once and never have to do it again for all the rest
        //of the simulation
        recomputeTilesMPA();

    }


    /**
     * not strictly required and so separate from the initialization proper, but basically a shape file can be used to
     * add cities in
     */
    public void addCities(String cityResources)
    {

        cities = GISReaders.readShapeAndMergeWithRaster(rasterBathymetry,cityResources);
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


    }

    /**
     * this is called at initialization but can be called again if there is a change in MPAs. It basically checks
     * for each tile if they belong to an MPA (strictly speaking if their center belongs to an MPA). If there is an MPA it is set to the tile.
     */
    public void recomputeTilesMPA() {
        //todo this works but make a test to be sure
        for(int i=0;i<rasterBackingGrid.getWidth(); i++)
            for(int j=0; j<rasterBackingGrid.getHeight(); j++)
            {
                Point gridPoint = rasterBathymetry.toPoint(i, j);
                Bag coveringObjects = mpaVectorField.getCoveringObjects(gridPoint);
                SeaTile seaTile = getSeaTile(i, j);

                if(coveringObjects.size() > 0)
                {
                    assert coveringObjects.size() == 1 : "got a tile covered by multiple MPAs, is that normal?"; //assume there is no double MPA
                    seaTile.setMpa((MasonGeometry) coveringObjects.get(0));
                    assert seaTile.isProtected() : "Set a tile to an MPA but it doesn't set the protected state to true";
                }
                else
                {
                    seaTile.setMpa(null);
                    assert !seaTile.isProtected() : "This tile has no MPA but still is protected";

                }

            }
    }

    public SeaTile getSeaTile(int gridX, int gridY) {
        return (SeaTile) rasterBackingGrid.get(gridX, gridY);
    }


    public GeomGridField getRasterBathymetry() {
        return rasterBathymetry;
    }

    public GeomVectorField getMpaVectorField() {
        return mpaVectorField;
    }

    public GeomVectorField getCities() {
        return cities;
    }
}
