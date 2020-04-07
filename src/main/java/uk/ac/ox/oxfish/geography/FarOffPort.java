package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.geography.ports.Port;

public class FarOffPort {


    private final Port port;

    private final SeaTile landTilePortSitsOn;

    private final SeaTile exitOnMap;

    private final double distanceToExit;


    private final Coordinate coordinate;



    public FarOffPort(Port port,
                      SeaTile landTilePortSitsOn,
                      SeaTile exitOnMap,
                      double distanceToExit, Coordinate coordinate) {
        this.port = port;
        this.landTilePortSitsOn = landTilePortSitsOn;
        this.exitOnMap = exitOnMap;
        this.distanceToExit = distanceToExit;

        this.coordinate = coordinate;
    }



    public Port getPort() {
        return port;
    }

    public SeaTile getLandTilePortSitsOn() {
        return landTilePortSitsOn;
    }

    public SeaTile getExitOnMap() {
        return exitOnMap;
    }

    public double getDistanceToExit() {
        return distanceToExit;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
