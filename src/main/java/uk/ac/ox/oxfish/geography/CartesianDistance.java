package uk.ac.ox.oxfish.geography;

/**
 * Simple grid distance. Pythagorean
 */
public class CartesianDistance extends BaseDistance
{


    final private double gridCellSizeInKm;

    public CartesianDistance(double gridCellSizeInKm) {
        this.gridCellSizeInKm = gridCellSizeInKm;
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
        return gridCellSizeInKm * Math.sqrt(Math.pow(endXGrid-startXGrid,2) + Math.pow(endYGrid-startYGrid,2));
    }
}
