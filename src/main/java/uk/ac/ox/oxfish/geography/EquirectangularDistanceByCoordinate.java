package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Assumes the map has coordinates in degrees.
 * http://www.movable-type.co.uk/scripts/latlong.html
 * Created by carrknight on 7/2/17.
 */
public class EquirectangularDistanceByCoordinate implements Distance {



    /**
     * the distance between two sea-tiles
     *
     * @param start starting sea-tile
     * @param end   ending sea-tile
     * @param map
     * @return kilometers between the two
     */
    @Override
    public double distance(SeaTile start, SeaTile end, NauticalMap map) {

        Coordinate startCoordinates = map.getCoordinates(start);
        double x1 = Math.toRadians(startCoordinates.x);
        double y1 = Math.toRadians(startCoordinates.y);
        Coordinate endCoordinates = map.getCoordinates(end);
        double x2 = Math.toRadians(endCoordinates.x);
        double y2 = Math.toRadians(endCoordinates.y);
        double radius = EquirectangularDistance.EARTH_RADIUS;

        double x = (x2-x1) *
                Math.cos(
                        (y1+y2)/2d
                );
        double y = y2-y1;


        return Math.sqrt(x*x+y*y) * radius;
    }
}
