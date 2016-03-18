package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Assuming the UTM map is small enough that cartesian distance is applicable to it
 * Created by carrknight on 3/18/16.
 */
public class CartesianUTMDistance implements Distance {


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
        Coordinate coordinate = map.getCoordinates(start);
        double xStart = coordinate.x;
        double yStart = coordinate.y;
        coordinate = map.getCoordinates(end);
        double xEnd = coordinate.x;
        double yEnd = coordinate.y;
        //this should return the distance in kilometers!
        return Math.sqrt( Math.pow(yEnd-yStart,2) +  Math.pow(xEnd-xStart,2) )/1000;

    }
}
