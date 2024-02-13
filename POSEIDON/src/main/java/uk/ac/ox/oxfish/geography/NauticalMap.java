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
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import ec.util.MersenneTwisterFast;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.Grid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
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
import uk.ac.ox.oxfish.utility.MasonUtils;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;

/**
 * This object stores the map/chart of the sea. It contains all the geometric fields holding locations and boundaries.
 * It ought to be initialized as follows:
 * <ul>
 *     <li> Constructor</li>
 *     <li> initializeBiology()</li>
 * </ul>
 * Created by carrknight on 4/2/15.
 */
public class NauticalMap implements Startable {

    /**
     * when you don't want to use a real geometry you can use this singleton to just cover an mpa.
     */
    // todo this is a placeholder; what I need to do is to kill off the geometry altogether and embrace the grid
    public static final MasonGeometry MPA_SINGLETON = new MasonGeometry();
    private final MapExtent mapExtent;
    /**
     * this holds the bathymetry raster grid
     */
    private final GeomGridField rasterBathymetry;
    /**
     * the proper grid behind raster bathymetry. You can get it by rasterBathymetry.getGrid() and cast but this skips
     * the casting
     */
    private final ObjectGrid2D rasterBackingGrid;
    /**
     * The list of ports
     */
    private final LinkedList<Port> ports;
    /**
     * the grid containing the location of all the ports
     */
    private final SparseGrid2D portMap;
    /**
     * map holding location of all fishers.
     */
    private final SparseGrid2D fishersMap;
    /**
     * a map holding
     */
    private final IntGrid2D dailyTrawlsMap;
    /**
     * holds the MPAs
     */
    private final GeomVectorField mpaVectorField;
    /**
     * computing neighborhoods is actually a very expensive computational process so we store here all the neighborhood
     * we found for each tile and neighborhood size so we compute them but once
     */
    public Table<SeaTile, Integer, Bag> alreadyComputedNeighbors = HashBasedTable.create();
    /**
     * one size lookups are even more common, so store them here
     */
    public Map<SeaTile, Bag> sizeOneNeighborhoods = new WeakHashMap<>();
    /**
     * the distance calculator, maybe useful, maybe not
     */
    private Distance distance;
    /**
     * the object finding a osmoseWFSPath from A to B
     */
    private Pathfinder pathfinder;
    /**
     * proof that you were started
     */
    private Stoppable receipt;
    private List<SeaTile> allTiles = null;
    private LinkedList<SeaTile> waterSeaTiles = null;
    /**
     * keep the precomputed line tiles
     */
    private HashSet<SeaTile> lineTiles = null;

    /**
     * set all the base fields. Calls recomputeTilesMPA() in order to tell tiles if they are covered by an MPA or not
     *
     * @param rasterBathymetry the bathymetry object. It assumes it is backed by a SeaTile grid (ObjectGrid2D)
     * @param mpaVectorField   the vector field with the MPAs polygons
     * @param distance         the distance calculator
     */
    public NauticalMap(
        final GeomGridField rasterBathymetry,
        final GeomVectorField mpaVectorField,
        final Distance distance,
        final Pathfinder pathfinder
    ) {
        this.rasterBathymetry = rasterBathymetry;
        this.pathfinder = pathfinder;
        this.mpaVectorField = mpaVectorField;
        this.setDistance(distance);
        this.rasterBackingGrid = (ObjectGrid2D) rasterBathymetry.getGrid();
        recomputeTilesMPA();

        ports = new LinkedList<>();
        portMap = new SparseGrid2D(getWidth(), getHeight());
        fishersMap = new SparseGrid2D(getWidth(), getHeight());
        dailyTrawlsMap = new IntGrid2D(getWidth(), getHeight());
        this.mapExtent = MapExtent.from(getWidth(), getHeight(), getRasterBathymetry().getMBR());
    }

    /**
     * this is called at initialization but can be called again if there is a change in MPAs. It basically checks for
     * each tile if they belong to an MPA (strictly speaking if their center belongs to an MPA). If there is an MPA it
     * is set to the tile.Bio
     */
    public void recomputeTilesMPA() {
        waterSeaTiles = null;
        allTiles = null;
        lineTiles = null;
        // todo this works but make a test to be sure
        for (int i = 0; i < rasterBackingGrid.getWidth(); i++)
            for (int j = 0; j < rasterBackingGrid.getHeight(); j++) {
                final Point gridPoint = rasterBathymetry.toPoint(i, j);
                final Bag coveringObjects = mpaVectorField.getCoveringObjects(gridPoint);
                final SeaTile seaTile = getSeaTile(i, j);

                if (!coveringObjects.isEmpty()) {
                    assert coveringObjects.size() ==
                        1 : "got a tile covered by multiple MPAs, is that normal?"; // assume there is no double MPA
                    seaTile.assignMpa((MasonGeometry) coveringObjects.get(0));
                    assert seaTile.isProtected() : "Set a tile to an MPA but it doesn't set the protected state to " +
                        "true";
                } else {

                    if (seaTile.grabMPA() != MPA_SINGLETON)
                        seaTile.assignMpa(null);
                    assert !seaTile.isProtected() : "This tile has no MPA but still is protected";

                }

            }
    }

    /**
     * how many cells horizontally
     */
    public int getWidth() {
        return rasterBathymetry.getGridWidth();
    }

    public int getHeight() {
        return rasterBathymetry.getGridHeight();
    }

    public GeomGridField getRasterBathymetry() {
        return rasterBathymetry;
    }

    /**
     * returns sea tile at that coordinates. If it isn't there returns null
     *
     * @param gridX the x coordinate of the cell
     * @param gridY the y coordinate of the cell
     * @return the cell or null if there isn't anything
     */
    public SeaTile getSeaTile(
        final int gridX,
        final int gridY
    ) {
        if (rasterBackingGrid.getHeight() <= gridY || rasterBackingGrid.getWidth() <= gridX
            || gridX < 0 || gridY < 0)
            return null;
        return (SeaTile) rasterBackingGrid.get(gridX, gridY);
    }

    /**
     * goes through all seatiles and calls the initialize function to create/assign a LocalBiology to each SeaTile
     *
     * @param initializer the local biology factory
     * @param random      the randomizer
     * @param biology     the biology
     */
    public void initializeBiology(
        final BiologyInitializer initializer,
        final MersenneTwisterFast random,
        final GlobalBiology biology
    ) {
        for (final Object element : rasterBackingGrid.elements()) {
            final SeaTile tile = (SeaTile) element; // cast
            tile.setBiology(initializer.generateLocal(biology,
                tile, random, getHeight(),
                getWidth(), this
            )); // put new biology in
        }

    }

    public double getTotalBiomass(final Species species) {
        double biomass = 0;
        for (final SeaTile seaTile : getAllSeaTilesExcludingLandAsList()) {
            biomass += seaTile.getBiomass(species);
        }

        return biomass;
    }

    public List<SeaTile> getAllSeaTilesExcludingLandAsList() {

        if (waterSeaTiles == null) {
            waterSeaTiles = new LinkedList<>(getAllSeaTilesAsList());
            waterSeaTiles.removeIf(SeaTile::isLand);
        }
        return waterSeaTiles;
    }

    /**
     * get all the tiles of the map as a list
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<SeaTile> getAllSeaTilesAsList() {
        if (allTiles == null) {
            allTiles = new LinkedList<>(rasterBackingGrid.elements());
            allTiles.sort((o1, o2) -> {
                final int xComparison = Integer.compare(o1.getGridX(), o2.getGridX());
                if (xComparison == 0)
                    return Integer.compare(o1.getGridY(), o2.getGridY());
                else
                    return xComparison;
            });
        }
        return allTiles;
    }

    @Override
    public void start(final FishState model) {
        Preconditions.checkArgument(receipt == null, "already started, love");

        // start all tiles
        for (final Object element : rasterBackingGrid.elements()) {
            final Startable tile = (SeaTile) element; // cast
            tile.start(model);
        }

        Preconditions.checkArgument(receipt == null);
        // reset fished map count
        receipt =
            model.scheduleEveryDay((Steppable) simState -> dailyTrawlsMap.setTo(0), StepOrder.DAWN);

    }

    public Bag getAllSeaTiles() {
        return rasterBackingGrid.elements();
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if (receipt != null)
            receipt.stop();

        // turn off all tiles
        // start all tiles
        for (final Object element : rasterBackingGrid.elements()) {
            final Startable tile = (SeaTile) element; // cast
            tile.turnOff();
        }

        alreadyComputedNeighbors.clear();
        sizeOneNeighborhoods.clear();
    }

    public SeaTile getSeaTile(final Int2D gridLocation) {
        return getSeaTile(gridLocation.x, gridLocation.y);
    }

    public Coordinate getCoordinates(final SeaTile tile) {
        return getCoordinates(tile.getGridX(), tile.getGridY());
    }

    public Coordinate getCoordinates(
        final int gridX,
        final int gridY
    ) {
        return mapExtent.getCoordinates(gridX, gridY);
    }

    public GeomVectorField getMpaVectorField() {
        return mpaVectorField;
    }

    /**
     * the distance (in km) between the cell at (startXGrid,startYGrid) and the cell at (endXGrid,endYGrid)
     *
     * @param startXGrid the starting x grid coordinate
     * @param startYGrid the starting y grid coordinate
     * @param endXGrid   the ending x grid coordinate
     * @param endYGrid   the ending y grid coordinate
     * @return kilometers between the two points
     */
    public double distance(
        final int startXGrid,
        final int startYGrid,
        final int endXGrid,
        final int endYGrid
    ) {
        return distance.distance(getSeaTile(startXGrid, startYGrid), getSeaTile(endXGrid, endYGrid), this);
    }

    /**
     * the distance between two sea-tiles
     *
     * @param start starting sea-tile
     * @param end   ending sea-tile
     * @return kilometers between the two
     */
    public double distance(
        final SeaTile start,
        final SeaTile end
    ) {
        return distance.distance(start, end, this);
    }

    /**
     * tell the map some seatile has changed (not in its inner workings but really swapped out with a new seatile
     * object). Forgets all precomputed neighborhoods and recomputes MPAs
     */
    public void reactToSeaTileChange() {
        recomputeTilesMPA();
    }

    /**
     * Given a port, sets it on the map but first check that:
     * <ul>
     *     <li> The port is on land</li>
     *     <li> There is at least one patch of sea nearby </li>
     * </ul>
     *
     * @param port the port to add to the map
     */
    public void addPort(final Port port) {

        // check location
        final SeaTile portSite = port.getLocation();
        Preconditions.checkArgument(portSite.isLand(), "port is not on land");
        // check it's coastal
        final boolean isCoastal = isCoastal(portSite);
        Preconditions.checkArgument(isCoastal, "port has no neighboring sea tiles");

        // put it in the masterlist
        Preconditions.checkArgument(!ports.contains(port), "This port was already registered!");
        ports.add(port);

        portMap.setObjectLocation(port, portSite.getGridX(), portSite.getGridY());

    }

    public boolean isCoastal(final SeaTile seaTile) {
        final Bag neighbors = new Bag();
        rasterBackingGrid.getMooreNeighbors(seaTile.getGridX(), seaTile.getGridY(), 1,
            Grid2D.BOUNDED, false, neighbors, null, null
        );
        return MasonUtils.<SeaTile>bagToStream(neighbors).anyMatch(SeaTile::isWater);
    }

    /**
     * the map of ports. Don't use directly (it is public for gui weirdness)
     *
     * @return the sparsegrid with ports
     */
    public SparseGrid2D getPortMap() {
        return portMap;
    }

    public LinkedList<Port> getPorts() {
        return ports;
    }

    public List<Entry<SeaTile, Double>> cumulativeTravelTimeAlongRouteInHours(
        final Deque<SeaTile> route,
        final double speedInKph
    ) {
        return distance.cumulativeTravelTimeAlongRouteInHours(route, this, speedInKph);
    }

    public boolean recordFisherLocation(
        final Fisher fisher,
        final int x,
        final int y
    ) {
        return fishersMap.setObjectLocation(fisher, x, y);
    }

    /**
     * record the fact that somebody fished somewhere (ignored if there is no gui)
     *
     * @param tile where it has been fished
     */
    public void recordFishing(final SeaTile tile) {

        dailyTrawlsMap.field[tile.getGridX()][tile.getGridY()]++;
    }

    public SparseGrid2D getFisherGrid() {
        return fishersMap;
    }

    public SeaTile getRandomBelowWaterLineSeaTile(final MersenneTwisterFast random) {

        final List<SeaTile> waterTiles = getAllSeaTilesExcludingLandAsList();

        return waterTiles.get(random.nextInt(waterTiles.size()));
    }

    public IntGrid2D getDailyTrawlsMap() {
        return dailyTrawlsMap;
    }

    @VisibleForTesting
    public Stoppable getReceipt() {
        return receipt;
    }

    /**
     * return the full osmoseWFSPath that brings us from start to end
     *
     * @param start the starting tile
     * @param end   the ending tile
     * @return a queue of steps from start to end or null if it isn't possible to go from start to end
     */
    public Deque<SeaTile> getRoute(
        final SeaTile start,
        final SeaTile end
    ) {
        return pathfinder.getRoute(this, start, end);
    }

    public Bag getFishersAtLocation(final SeaTile tile) {
        return getFishersAtLocation(
            tile.getGridX(),
            tile.getGridY()
        );
    }

    public Bag getFishersAtLocation(
        final int x,
        final int y
    ) {
        return fishersMap.getObjectsAtLocation(x, y);
    }

    /**
     * get all tiles that are unprotected but share a border with at least one protected line!
     */
    public HashSet<SeaTile> getTilesOnTheMPALine() {
        if (lineTiles == null) {
            lineTiles = new HashSet<>();
            for (final SeaTile tile : getAllSeaTilesExcludingLandAsList()) {
                if (!tile.isProtected() &&
                    bagToStream(getMooreNeighbors(tile, 1))
                        .anyMatch(o -> ((SeaTile) o).isProtected())) {
                    lineTiles.add(tile);
                }
            }
        }

        return lineTiles;

    }

    public Bag getMooreNeighbors(
        final SeaTile tile,
        final int neighborhoodSize
    ) {
        Bag neighbors;
        neighbors = neighborhoodSize == 1 ? sizeOneNeighborhoods.get(tile) :
            alreadyComputedNeighbors.get(tile, neighborhoodSize);
        if (neighbors == null) {
            neighbors = new Bag();
            rasterBackingGrid.getMooreNeighbors(tile.getGridX(), tile.getGridY(), neighborhoodSize,
                Grid2D.BOUNDED, false, neighbors, null, null
            );
            if (neighborhoodSize == 1)
                sizeOneNeighborhoods.put(tile, neighbors);
            else
                alreadyComputedNeighbors.put(tile, neighborhoodSize, neighbors);
        }
        return neighbors;
    }

    public Stream<SeaTile> getMooreNeighborsStream(
        final SeaTile tile,
        final int neighborhoodSize
    ) {
        return bagToStream(getMooreNeighbors(tile, neighborhoodSize));
    }

    public Pathfinder getPathfinder() {
        return pathfinder;
    }

    public void setPathfinder(final Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    public String asASCII() {
        return range(0, getRasterBathymetry().getGridHeight())
            .mapToObj(y ->
                range(0, getRasterBathymetry().getGridWidth())
                    .mapToObj(x -> getSeaTile(x, y))
                    .map(tile -> tile.isWater() ? "~" : (tile.isPortHere() ? "@" : "*"))
                    .collect(joining())
            )
            .collect(joining("\n"));
    }

    public Int2D getGridXY(final Coordinate coordinate) {
        return getSeaTile(coordinate).getGridLocation();
    }

    public SeaTile getSeaTile(final Coordinate coordinate) {
        return getSeaTile(
            rasterBathymetry.toXCoord(coordinate.x),
            rasterBathymetry.toYCoord(coordinate.y)
        );
    }

    public double distance(
        final Int2D here,
        final Int2D there
    ) {
        final SeaTile tileHere = getSeaTile(here.x, here.y);
        final SeaTile tileThere = getSeaTile(there.x, there.y);
        return getDistance().distance(tileHere, tileThere, this);
    }

    /**
     * do not call directly, if not for decorating. Use delegate functions
     */
    public Distance getDistance() {
        return distance;
    }

    public void setDistance(final Distance distance) {
        this.distance = distance;
    }

    public MapExtent getMapExtent() {
        return mapExtent;
    }

}
