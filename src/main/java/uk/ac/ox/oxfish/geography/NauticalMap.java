package uk.ac.ox.oxfish.geography;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.*;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * This object stores the map/chart of the sea. It contains all the geometric fields holding locations and boundaries.
 * It ought to be initialized as follows:
 * <ul>
 *     <li> Constructor</li>
 *     <li> initializeBiology()</li>
 * </ul>
 * Created by carrknight on 4/2/15.
 */
public class NauticalMap implements Startable
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
     * pre-recorded distance between cell 0,0 and 1,1. Useful to compute grid distances quickly
     */
    private double obliqueDistanceInKm = Double.NaN;
    /**
     * pre-recorded distance between cell 0,0 and 0,1
     */
    private double horizontalVerticalDistanceInKm = Double.NaN;



    /**
     * The list of ports
     */
    private HashSet<Port> ports;

    /**
     * the grid containing the location of all the ports
     */
    private SparseGrid2D portMap;


    /**
     * map holding location of all fishers.
     */
    private SparseGrid2D fishersMap;

    /**
     * a map holding
     */
    private DoubleGrid2D fishedMap;

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
        this.setDistance(distance);
        this.rasterBackingGrid = (ObjectGrid2D) rasterBathymetry.getGrid();
        recomputeTilesMPA();

        ports = new HashSet<>();
        portMap = new SparseGrid2D(getWidth(), getHeight());
        fishersMap = new SparseGrid2D(getWidth(), getHeight());
        fishedMap = new DoubleGrid2D(getWidth(),getHeight());
    }

    public int getHeight() {
        return rasterBathymetry.getGridHeight();
    }

    /**
     * how many cells horizontally
     */
    public int getWidth() {
        return rasterBathymetry.getGridWidth();
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
     * @param random the randomizer
     * @param biology the biology
     */
    public void initializeBiology(BiologyInitializer initializer,
                                  MersenneTwisterFast random, GlobalBiology biology)
    {
        for(Object element : rasterBackingGrid.elements())
        {
            SeaTile tile = (SeaTile) element; //cast
            tile.setBiology(initializer.generate(biology, tile, random, getHeight(), getWidth())); //put new biology in
        }

    }


    public double getTotalBiology(Specie specie)
    {
        return getAllSeaTilesAsList().stream().mapToDouble(value -> value.getBiomass(specie)).sum();
    }



    /**
     * proof that you were started
     */
    private Stoppable receipt;


    @Override
    public void start(FishState model) {
        Preconditions.checkArgument(receipt == null, "already started, love");


        //start all tiles
        for(Object element : rasterBackingGrid.elements())
        {
            SeaTile tile = (SeaTile) element; //cast
            tile.start(model);
        }

    }


    /**
     * start the resetting of fish-hotspots; pointless if you have no gui
     * @param model
     */
    public void guiStart(FishState model)
    {
        Preconditions.checkArgument(receipt==null);
        //reset fished map count
        receipt =
                model.scheduleEveryDay(new Steppable() {
                    @Override
                    public void step(SimState simState) {

                        fishedMap.multiply(.8);
                    }
                }, StepOrder.DATA_RESET);
    }
    public Bag getAllSeaTiles()
    {
        return rasterBackingGrid.elements();
    }

    public List<SeaTile> getAllSeaTilesAsList()
    {
        return new LinkedList<>(rasterBackingGrid.elements());
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if(receipt!=null)
            receipt.stop();

        //turn off all tiles
        //start all tiles
        for(Object element : rasterBackingGrid.elements())
        {
            SeaTile tile = (SeaTile) element; //cast
            tile.turnOff();
        }
    }

    /**
     * this is called at initialization but can be called again if there is a change in MPAs. It basically checks
     * for each tile if they belong to an MPA (strictly speaking if their center belongs to an MPA). If there is an MPA it is set to the tile.Bio
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

    /**
     * returns sea tile at that coordinates. If it isn't there returns null
     * @param gridX the x coordinate of the cell
     * @param gridY the y coordinate of the cell
     * @return the cell or null if there isn't anything
     */
    public SeaTile getSeaTile(int gridX, int gridY) {
        if(rasterBackingGrid.getHeight() <= gridY || rasterBackingGrid.getWidth() <= gridX
                 || gridX < 0 || gridY <0)
            return null;
        return (SeaTile) rasterBackingGrid.get(gridX, gridY);
    }

    public Coordinate coordinate(int gridX, int gridY)
    {
        return rasterBathymetry.toPoint(gridX,gridY).getCoordinate();
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


    /**
     * computing neighborhoods is actually a very expensive computational process so we store here all the
     * neighborhood we found for each tile and neighborhood size so we compute them but once
     */
    public Table<SeaTile,Integer,Bag> alreadyComputedNeighbors = HashBasedTable.create();



    public Bag getMooreNeighbors(SeaTile tile, int neighborhoodSize)
    {
        Bag neighbors;
        neighbors = alreadyComputedNeighbors.get(tile, neighborhoodSize);
        if(neighbors == null) {
            neighbors = new Bag();
            rasterBackingGrid.getMooreNeighbors(tile.getGridX(), tile.getGridY(), neighborhoodSize,
                                                Grid2D.BOUNDED, false, neighbors, null, null);
            alreadyComputedNeighbors.put(tile,neighborhoodSize,neighbors);
        }
        return neighbors;
    }

    /**
     * tell the map some seatile has changed (not in its inner workings but really swapped out with a new seatile object).
     * Forgets all precomputed neighborhoods and recomputes MPAs
     */
    public void reactToSeaTileChange()
    {
        recomputeTilesMPA();
        alreadyComputedNeighbors.clear();
    }

    /**
     * Given a port, sets it on the map but first check that:
     * <ul>
     *     <li> The port is on land</li>
     *     <li> There is at least one patch of sea nearby </li>
     * </ul>
     * @param port the port to add to the map
     */
    public void addPort(Port port)
    {
        //check location
        SeaTile portSite = port.getLocation();
        Preconditions.checkArgument(portSite.getAltitude() >= 0, "port is not on land");
        //check it's coastal
        Bag neighbors = new Bag();
        rasterBackingGrid.getMooreNeighbors(portSite.getGridX(), portSite.getGridY(), 1,
                Grid2D.BOUNDED, false, neighbors,null,null);
        boolean isCoastal = false;
        for(Object tile : neighbors)
        {
            isCoastal = ((SeaTile)tile).getAltitude() < 0;
            if(isCoastal)
                break;
        }
        Preconditions.checkArgument(isCoastal,"port has no neighboring sea tiles");

        //put it in the masterlist
        boolean wasNotIn = ports.add(port);
        Preconditions.checkArgument(wasNotIn, "This port was already registered!");

        portMap.setObjectLocation(port,portSite.getGridX(),portSite.getGridY());

    }

    /**
     * the map of ports. Don't use directly (it is public for gui weirdness)
     * @return the sparsegrid with ports
     */
    public SparseGrid2D getPortMap() {
        return portMap;
    }

    public HashSet<Port> getPorts() {
        return ports;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }


    public boolean recordFisherLocation(Fisher fisher, int x, int y) {
        return fishersMap.setObjectLocation(fisher, x, y);
    }

    /**
     * record the fact that somebody fished somewhere (ignored if there is no gui)
     * @param tile where it has been fished
     */
    public void recordFishing(SeaTile tile)
    {
        if(receipt != null)
            fishedMap.field[tile.getGridX()][tile.getGridY()]++;
    }

    public SparseGrid2D getFisherGrid() {
        return fishersMap;
    }


    public SeaTile getRandomBelowWaterLineSeaTile(MersenneTwisterFast random)
    {
        SeaTile toReturn;
        int tries = 0;
        do{
            toReturn = getSeaTile(random.nextInt(getWidth()),
                                      random.nextInt(getHeight()));

            tries++;
            if(tries > 100000)
                throw new RuntimeException("Tried 100000 time to get a random sea tile and failed. Maybe it's time to turnOff");

        }while (toReturn.getAltitude() > 0); //keep looking if you found something at sea
        return toReturn;
    }

    public DoubleGrid2D getFishedMap() {
        return fishedMap;
    }
}
