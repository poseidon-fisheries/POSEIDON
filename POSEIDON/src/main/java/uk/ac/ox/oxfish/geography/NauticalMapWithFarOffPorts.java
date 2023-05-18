package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Coordinate;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import uk.ac.ox.oxfish.geography.pathfinding.Pathfinder;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;


/**
 * re-routes all calls of nauticalmap in order to deal with ports outside of it.
 * It will look quite ugly but it's the only way to make sure that distance and pathfinder calls are intercepted (because these
 * can be changed over time, decorators just won't do it)
 */
public class NauticalMapWithFarOffPorts extends NauticalMap {


    public final static int GRID_X_ALLOCATED_TO_FAR_OFF_PORTS = -999;

    /**
     * map of landseatile ---> faroffport (the key tile is not the exit, it is the fake land the port is sitting on)
     * gets initialized when we have the biology (which we need for ports)
     */
    private final Map<SeaTile, FarOffPort> farOffPorts;


    public NauticalMapWithFarOffPorts(
        GeomGridField rasterBathymetry, GeomVectorField mpaVectorField,
        Distance distance, Pathfinder pathfinder,
        Map<SeaTile, FarOffPort> farOffPorts
    ) {
        super(rasterBathymetry, mpaVectorField, distance, pathfinder);
        this.farOffPorts = farOffPorts;
        for (FarOffPort value : farOffPorts.values()) {
            addPort(value.getPort());
        }
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
    public double distance(int startXGrid, int startYGrid, int endXGrid, int endYGrid) {
        return distance(
            getSeaTile(startXGrid, startYGrid),
            getSeaTile(endXGrid, endYGrid)
        );
    }

    /**
     * the distance between two sea-tiles
     *
     * @param start starting sea-tile
     * @param end   ending sea-tile
     * @return kilometers between the two
     */
    public double distance(SeaTile start, SeaTile end) {

        //if start is from far off port...
        final FarOffPort startAsFarOffPort = farOffPorts.get(start);
        if (startAsFarOffPort != null)
            return startAsFarOffPort.getDistanceToExit() + distance(
                startAsFarOffPort.getExitOnMap(),
                end
            );
        final FarOffPort endAsFarOffPort = farOffPorts.get(end);
        if (endAsFarOffPort != null)
            return distance(start, endAsFarOffPort.getExitOnMap()) +
                endAsFarOffPort.getDistanceToExit();

        //normal distance
        return getDistance().distance(start, end, this);
    }

    @Override
    public Coordinate getCoordinates(int gridX, int gridY) {
        if (gridX == GRID_X_ALLOCATED_TO_FAR_OFF_PORTS)
            return getCoordinates(getSeaTile(gridX, gridY));
        else
            return super.getCoordinates(gridX, gridY);
    }

    @Override
    public Coordinate getCoordinates(SeaTile tile) {

        final FarOffPort farOffPort = farOffPorts.get(tile);
        if (farOffPort == null)
            return super.getCoordinates(tile);
        else
            return farOffPort.getCoordinate();

    }

    @Override
    public SeaTile getSeaTile(int gridX, int gridY) {
        if (gridX == GRID_X_ALLOCATED_TO_FAR_OFF_PORTS) //quickly tells you if you are dealing with something off the map
            for (FarOffPort farOffPort : farOffPorts.values()) {
                if (farOffPort.getLandTilePortSitsOn().getGridY() == gridY)
                    return farOffPort.getLandTilePortSitsOn();
            }


        return super.getSeaTile(gridX, gridY);
    }

    @Override
    public SeaTile getSeaTile(Coordinate coordinate) {

        for (FarOffPort farOffPort : farOffPorts.values()) {
            if (farOffPort.getCoordinate().equals(coordinate))
                return farOffPort.getLandTilePortSitsOn();
        }

        return super.getSeaTile(coordinate);
    }


    @Override
    public Deque<SeaTile> getRoute(SeaTile start, SeaTile end) {

        //if start is from far off port...
        final FarOffPort startAsFarOffPort = farOffPorts.get(start);
        if (startAsFarOffPort != null) {
            Deque<SeaTile> path = new LinkedList<>();
            path.add(startAsFarOffPort.getLandTilePortSitsOn());
            path.addAll(
                getRoute(startAsFarOffPort.getExitOnMap(), end)
            );
            return path;
        }
        final FarOffPort endAsFarOffPort = farOffPorts.get(end);
        if (endAsFarOffPort != null) {
            Deque<SeaTile> path = new LinkedList<>();
            path.addAll(
                getRoute(start, endAsFarOffPort.getExitOnMap())
            );
            path.add(endAsFarOffPort.getLandTilePortSitsOn());
            return path;
        }

        return super.getRoute(start, end);
    }


    @Override
    public Bag getMooreNeighbors(SeaTile tile, int neighborhoodSize) {
        if (farOffPorts.containsKey(tile))
            return new Bag();

        return super.getMooreNeighbors(tile, neighborhoodSize);
    }

    @Override
    public boolean isCoastal(SeaTile seaTile) {
        if (farOffPorts.containsKey(seaTile))
            return true;
        else
            return super.isCoastal(seaTile);


    }


    public Collection<FarOffPort> getAllFarOffPorts() {
        return farOffPorts.values();
    }
}
