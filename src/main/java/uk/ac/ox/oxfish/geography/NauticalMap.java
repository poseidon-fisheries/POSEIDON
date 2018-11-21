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

package uk.ac.ox.oxfish.geography;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
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
import sim.field.grid.Grid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.pathfinding.Pathfinder;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import javax.annotation.Nullable;
import java.util.*;

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
     * the object finding a osmoseWFSPath from A to B
     */
    private Pathfinder pathfinder;


    /**
     * The list of ports
     */
    private LinkedList<Port> ports;

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
    private IntGrid2D dailyTrawlsMap;


    /**
     * holds the MPAs
     */
    private GeomVectorField mpaVectorField;


    /**
     * set all the base fields. Calls recomputeTilesMPA() in order to tell tiles if they are covered by an MPA or not
     * @param rasterBathymetry the bathymetry object. It assumes it is backed by a SeaTile grid (ObjectGrid2D)
     * @param mpaVectorField the vector field with the MPAs polygons
     * @param distance the distance calculator
     */
    public NauticalMap(GeomGridField rasterBathymetry, GeomVectorField mpaVectorField,
                       Distance distance, Pathfinder pathfinder) {
        this.rasterBathymetry = rasterBathymetry;
        this.pathfinder = pathfinder;
        this.mpaVectorField = mpaVectorField;
        this.setDistance(distance);
        this.rasterBackingGrid = (ObjectGrid2D) rasterBathymetry.getGrid();
        recomputeTilesMPA();

        ports = new LinkedList<>();
        portMap = new SparseGrid2D(getWidth(), getHeight());
        fishersMap = new SparseGrid2D(getWidth(), getHeight());
        dailyTrawlsMap = new IntGrid2D(getWidth(),getHeight());
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
            tile.setBiology(initializer.generateLocal(biology, tile, random, getHeight(), getWidth(),this )); //put new biology in
        }

    }


    public double getTotalBiology(Species species)
    {
        double biomass = 0;
        for (SeaTile seaTile : getAllSeaTilesExcludingLandAsList()) {
            biomass+= seaTile.getBiomass(species);
        }

        return biomass;
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

        Preconditions.checkArgument(receipt==null);
        //reset fished map count
        receipt =
                model.scheduleEveryDay(new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        dailyTrawlsMap.setTo(0);
                    }
                },StepOrder.DAWN);

    }



    public Bag getAllSeaTiles()
    {
        return rasterBackingGrid.elements();
    }


    @SuppressWarnings("unchecked") //bags are annoying this way
    /**
     * get all the tiles of the map as a list
     */
    public List<SeaTile> getAllSeaTilesAsList()
    {

        if(allTiles == null) {
            allTiles = new LinkedList<>(rasterBackingGrid.elements());
            Collections.sort(allTiles, (o1, o2) -> {
                int xComparison = Integer.compare(o1.getGridX(), o2.getGridX());
                if(xComparison == 0)
                    return Integer.compare(o1.getGridY(),o2.getGridY());
                else
                    return xComparison;
            });
        }
        return allTiles;
    }
    private List<SeaTile> allTiles = null;

    private LinkedList<SeaTile> waterSeaTiles = null;

    public List<SeaTile> getAllSeaTilesExcludingLandAsList()
    {

        if(waterSeaTiles == null) {
            waterSeaTiles = new LinkedList<>(getAllSeaTilesAsList());
            waterSeaTiles.removeIf(seaTile -> seaTile.getAltitude() >=0);
        }
        return waterSeaTiles;
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

        alreadyComputedNeighbors.clear();
        coordinateCache.clear();
        sizeOneNeighborhoods.clear();
    }

    /**
     * this is called at initialization but can be called again if there is a change in MPAs. It basically checks
     * for each tile if they belong to an MPA (strictly speaking if their center belongs to an MPA). If there is an MPA it is set to the tile.Bio
     */
    public void recomputeTilesMPA() {
        waterSeaTiles = null;
        allTiles = null;
        coordinateCache.clear();
        lineTiles = null;
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
                    seaTile.assignMpa((MasonGeometry) coveringObjects.get(0));
                    assert seaTile.isProtected() : "Set a tile to an MPA but it doesn't set the protected state to true";
                }
                else
                {
                    seaTile.assignMpa(null);
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




    public Coordinate getCoordinates(int gridX, int gridY)
    {
        return rasterBathymetry.toPoint(gridX,gridY).getCoordinate();
    }

    /**
     * basically getting coordinates is an expensive call; so we store previous calls here
     */
    private final WeakHashMap<SeaTile,Coordinate> coordinateCache = new WeakHashMap<>();

    public Coordinate getCoordinates(SeaTile tile)
    {
        return coordinateCache.computeIfAbsent(tile, new Function<SeaTile, Coordinate>() {
            @Nullable
            @Override
            public Coordinate apply(@Nullable SeaTile input) {
                return rasterBathymetry.toPoint(input.getGridX(),input.getGridY()).getCoordinate();
            }
        });
    }

    public SeaTile getSeaTile(Coordinate coordinate)
    {
        return getSeaTile(rasterBathymetry.toXCoord(coordinate.x),
                rasterBathymetry.toYCoord(coordinate.y));
    }

    public GeomGridField getRasterBathymetry() {
        return rasterBathymetry;
    }

    public GeomVectorField getMpaVectorField() {
        return mpaVectorField;
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
        return distance.distance(getSeaTile(startXGrid,startYGrid),getSeaTile(endXGrid,endYGrid),this);
    }

    /**
     * the distance between two sea-tiles
     * @param start starting sea-tile
     * @param end ending sea-tile
     * @return kilometers between the two
     */
    public double distance(SeaTile start, SeaTile end) {
        return distance.distance(start, end, this);
    }


    /**
     * computing neighborhoods is actually a very expensive computational process so we store here all the
     * neighborhood we found for each tile and neighborhood size so we compute them but once
     */
    public Table<SeaTile,Integer,Bag> alreadyComputedNeighbors = HashBasedTable.create();

    /**
     * one size lookups are even more common, so store them here
     */
    public Map<SeaTile,Bag> sizeOneNeighborhoods = new WeakHashMap<>();


    public Bag getMooreNeighbors(SeaTile tile, int neighborhoodSize)
    {
        Bag neighbors;
        neighbors =  neighborhoodSize == 1 ? sizeOneNeighborhoods.get(tile) :
                alreadyComputedNeighbors.get(tile, neighborhoodSize);
        if(neighbors == null) {
            neighbors = new Bag();
            rasterBackingGrid.getMooreNeighbors(tile.getGridX(), tile.getGridY(), neighborhoodSize,
                    Grid2D.BOUNDED, false, neighbors, null, null);
            if(neighborhoodSize==1)
                sizeOneNeighborhoods.put(tile,neighbors);
            else
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
        Preconditions.checkArgument(!ports.contains(port), "This port was already registered!");
        ports.add(port);

        portMap.setObjectLocation(port,portSite.getGridX(),portSite.getGridY());

    }

    /**
     * the map of ports. Don't use directly (it is public for gui weirdness)
     * @return the sparsegrid with ports
     */
    public SparseGrid2D getPortMap() {
        return portMap;
    }

    public LinkedList<Port> getPorts() {
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

        dailyTrawlsMap.field[tile.getGridX()][tile.getGridY()]++;
    }

    public SparseGrid2D getFisherGrid() {
        return fishersMap;
    }


    public SeaTile getRandomBelowWaterLineSeaTile(MersenneTwisterFast random)
    {


        List<SeaTile> waterTiles = getAllSeaTilesExcludingLandAsList();

        return waterTiles.get(random.nextInt(waterTiles.size()));
    }

    public IntGrid2D getDailyTrawlsMap() {
        return dailyTrawlsMap;
    }

    @VisibleForTesting
    public Stoppable getReceipt() {
        return receipt;
    }

    public Pathfinder getPathfinder() {
        return pathfinder;
    }

    public void setPathfinder(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    /**
     * return the full osmoseWFSPath that brings us from start to end
     * @param start the starting tile
     * @param end the ending tile
     * @return a queue of steps from start to end or null if it isn't possible to go from start to end
     */
    public Deque<SeaTile> getRoute(SeaTile start, SeaTile end) {
        return pathfinder.getRoute(this, start, end);
    }


    public Bag getFishersAtLocation(int x, int y) {
        return fishersMap.getObjectsAtLocation(x, y);
    }

    public Bag getFishersAtLocation(SeaTile tile) {
        return getFishersAtLocation(tile.getGridX(),
                tile.getGridY());
    }


    /**
     * keep the precomputed line tiles
     */
    private HashSet<SeaTile> lineTiles = null;
    /**
     * get all tiles that are unprotected but share a border with at least one protected line!
     * @return
     */

    public HashSet<SeaTile> getTilesOnTheMPALine()
    {
        if(lineTiles==null)
        {
            lineTiles = new HashSet<>();
            for(SeaTile tile : getAllSeaTilesExcludingLandAsList())
            {
                if(!tile.isProtected() && getMooreNeighbors(tile,1).stream().anyMatch(
                        o -> ((SeaTile) o).isProtected()))
                {
                    lineTiles.add(tile);
                }
            }
        }

            return lineTiles;


    }
}
