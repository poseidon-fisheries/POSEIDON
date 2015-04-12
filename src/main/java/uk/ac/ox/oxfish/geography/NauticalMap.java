package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.utility.GISReaders;

import java.util.function.Function;

/**
 * This object stores the map/chart of the sea. It contains all the geometric fields holding locations and boundaries.
 * It ought to be initialized as follows:
 * <ul>
 *     <li> Constructor</li>
 *     <li> initializeBiology()</li>
 * </ul>
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
     * the distance calculator, maybe useful, maybe not
     */
    private Distance distance;

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
     * set all the base fields. Calls recomputeTilesMPA() in order to tell tiles if they are covered by an MPA or not
     * @param rasterBathymetry the bathymetry object. It assumes it is backed by a SeaTile grid (ObjectGrid2D)
     * @param mpaVectorField the vector field with the MPAs polygons
     * @param distance the distance calculator
     */
    public NauticalMap(GeomGridField rasterBathymetry, GeomVectorField mpaVectorField, Distance distance) {
        this.rasterBathymetry = rasterBathymetry;
        this.mpaVectorField = mpaVectorField;
        this.distance = distance;
        this.rasterBackingGrid = (ObjectGrid2D) rasterBathymetry.getGrid();
        recomputeTilesMPA();
    }

    /**
     * todo move to parameter list
     */
    final static private String[] DEFAULT_MPA_SOURCES = {"cssr_mpa/reprojected/mpa_central.shp",
            "ncssr_mpa/reprojected/mpa_north.shp"};





    public static NauticalMap initializeWithDefaultValues()
    {
       return NauticalMapFactory.fromBathymetryAndShapeFiles(DEFAULT_BATHYMETRY_SOURCE, DEFAULT_MPA_SOURCES);
    }


    /**
     * goes through all seatiles and calls the initialize function to create/assign a LocalBiology to each SeaTile
     * @param initializer the local biology factory
     */
    public void initializeBiology(Function<SeaTile,LocalBiology> initializer)
    {
        for(Object element : rasterBackingGrid.elements())
        {
            SeaTile tile = (SeaTile) element; //cast
            tile.setBiology(initializer.apply(tile)); //put new biology in
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


    /**
     * the distance (in km) between the cell at (startXGrid,startYGrid) and the cell at (endXGrid,endYGrid)
     * @param startXGrid the starting x grid coordinate
     * @param startYGrid the starting y grid coordinate
     * @param endXGrid the ending x grid coordinate
     * @param endYGrid the ending y grid coordinate
     * @return kilometers between the two points
     */
    public double distance(int startXGrid, int startYGrid, int endXGrid, int endYGrid) {
        return distance.distance(startXGrid, startYGrid, endXGrid, endYGrid);
    }

    /**
     * the distance between two sea-tiles
     * @param start starting sea-tile
     * @param end ending sea-tile
     * @return kilometers between the two
     */
    public double distance(SeaTile start, SeaTile end) {
        return distance.distance(start, end);
    }
}
