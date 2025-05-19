/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.geography.ports.Port;

public class FarOffPort {


    private final Port port;

    private final SeaTile landTilePortSitsOn;

    private final SeaTile exitOnMap;

    private final double distanceToExit;


    private final Coordinate coordinate;


    public FarOffPort(
        Port port,
        SeaTile landTilePortSitsOn,
        SeaTile exitOnMap,
        double distanceToExit, Coordinate coordinate
    ) {
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
