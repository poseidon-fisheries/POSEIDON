package uk.ac.ox.oxfish.geography;

/**
 * Created by carrknight on 8/3/16.
 */
public class ManhattanDistance implements Distance {
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
        return  distance(start,end);
    }


    public double distance(SeaTile start, SeaTile end) {
        return  Math.abs(start.getGridX()-end.getGridX()) +  Math.abs(start.getGridY()-end.getGridY());
    }
}
