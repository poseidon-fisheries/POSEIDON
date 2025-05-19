/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

/**
 * Approximative distance between two points. Just grabbed the formula from here:
 * http://www.movable-type.co.uk/scripts/latlong.html
 * Created by carrknight on 4/10/15.
 */
public class EquirectangularDistance extends BaseDistance {

    public final static double EARTH_RADIUS = 6373;
    /**
     * the latitude (x) of the grid at 0,0
     */
    private final double lowerLeftLatitude;
    /**
     * distance between the center of the grid and the other
     */
    private final double gridSize;

    public EquirectangularDistance(double lowerLeftLatitude, double gridSize) {
        this.lowerLeftLatitude = lowerLeftLatitude;
        this.gridSize = gridSize;
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
    @Override
    public double distance(int startXGrid, int startYGrid, int endXGrid, int endYGrid) {
        assert startXGrid >= 0;
        assert startYGrid >= 0;
        assert endXGrid >= 0;
        assert endYGrid >= 0;

/*
        double startLatitude = startXGrid * gridSize + lowerLeftLatitude;
        double startLongitude = startYGrid * gridSize + lowerLeftLongitude;
        double endLatitude = endXGrid * gridSize + lowerLeftLatitude;
        double endLongitude = startYGrid * gridSize + lowerLeftLongitude;
*/
        double latitudeDistance = (endXGrid - startXGrid) * gridSize;
        double midLatitude = gridSize * (endXGrid - startXGrid) / 2.0 + lowerLeftLatitude;
        midLatitude = Math.toRadians(midLatitude);
        latitudeDistance = Math.toRadians(latitudeDistance);
        double x = latitudeDistance * Math.cos(midLatitude);
        double longitudeDistance = (endYGrid - startYGrid) * gridSize;
        longitudeDistance = Math.toRadians(longitudeDistance);

        return EARTH_RADIUS * Math.sqrt(Math.pow(x, 2) + Math.pow(longitudeDistance, 2));


    }
}
