package uk.ac.ox.oxfish.geography;

/**
 * Common interface for all distance measures over a nautical chart
 * Created by carrknight on 4/10/15.
 */
public interface Distance
{

    /**
     * the distance (in km) between the cell at (startXGrid,startYGrid) and the cell at (endXGrid,endYGrid)
     * @param startXGrid the starting x grid coordinate
     * @param startYGrid the starting y grid coordinate
     * @param endXGrid the ending x grid coordinate
     * @param endYGrid the ending y grid coordinate
     * @return kilometers between the two points
     */
    double distance(int startXGrid, int startYGrid, int endXGrid, int endYGrid);

    /**
     * the distance between two sea-tiles
     * @param start starting sea-tile
     * @param end ending sea-tile
     * @return kilometers between the two
     */
    double distance(SeaTile start, SeaTile end);




}
