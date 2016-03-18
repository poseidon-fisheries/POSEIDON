package uk.ac.ox.oxfish.geography;

/**
 * Common interface for all distance measures over a nautical chart
 * Created by carrknight on 4/10/15.
 */
public interface Distance
{

    /**
     * the distance between two sea-tiles
     * @param start starting sea-tile
     * @param end ending sea-tile
     * @param map
     * @return kilometers between the two
     */
    double distance(SeaTile start, SeaTile end, NauticalMap map);




}
