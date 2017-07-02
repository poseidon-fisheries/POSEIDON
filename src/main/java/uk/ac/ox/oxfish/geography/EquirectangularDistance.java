package uk.ac.ox.oxfish.geography;

/**
 * Approximative distance between two points. Just grabbed the formula from here:
 * http://www.movable-type.co.uk/scripts/latlong.html
 * Created by carrknight on 4/10/15.
 */
public class EquirectangularDistance extends BaseDistance {

    /**
     * the latitude (x) of the grid at 0,0
     */
    private final double lowerLeftLatitude;

    /**
     * distance between the center of the grid and the other
     */
    private final double gridSize;

    public final static double EARTH_RADIUS = 6373;

    public EquirectangularDistance(double lowerLeftLatitude, double gridSize) {
        this.lowerLeftLatitude = lowerLeftLatitude;
        this.gridSize = gridSize;
    }

    /**
     * the distance (in km) between the cell at (startXGrid,startYGrid) and the cell at (endXGrid,endYGrid)
     * @param startXGrid the starting x grid coordinate
     * @param startYGrid the starting y grid coordinate
     * @param endXGrid the ending x grid coordinate
     * @param endYGrid the ending y grid coordinate
     * @return kilometers between the two points
     */
    @Override
    public double distance(int startXGrid, int startYGrid, int endXGrid, int endYGrid) {
        assert  startXGrid >= 0;
        assert  startYGrid >= 0;
        assert  endXGrid >= 0;
        assert  endYGrid >= 0;

/*
        double startLatitude = startXGrid * gridSize + lowerLeftLatitude;
        double startLongitude = startYGrid * gridSize + lowerLeftLongitude;
        double endLatitude = endXGrid * gridSize + lowerLeftLatitude;
        double endLongitude = startYGrid * gridSize + lowerLeftLongitude;
*/
        double latitudeDistance = (endXGrid-startXGrid) * gridSize;
        double midLatitude = gridSize* (endXGrid-startXGrid)/2.0 + lowerLeftLatitude;
        midLatitude = Math.toRadians(midLatitude); latitudeDistance = Math.toRadians(latitudeDistance);
        double x = latitudeDistance * Math.cos(midLatitude);
        double longitudeDistance = (endYGrid-startYGrid) * gridSize;
        longitudeDistance = Math.toRadians(longitudeDistance);

        return EARTH_RADIUS * Math.sqrt(Math.pow(x,2)+Math.pow(longitudeDistance,2));



    }
}
