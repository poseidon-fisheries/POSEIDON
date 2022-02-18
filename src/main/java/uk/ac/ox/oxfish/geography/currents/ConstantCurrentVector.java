package uk.ac.ox.oxfish.geography.currents;

import sim.util.Double2D;
import sim.util.Int2D;

/**
 * a very simple, "dummy" current vector that just always returns the same vector at all positions
 */
public class ConstantCurrentVector implements CurrentVectors {



    private final Double2D currentVector;

    /**
     * the boundaries of the map, which should mean the boundaries of the currents field
     */
    private final int gridHeight;

    /**
     * the boundaries of the map, which should mean the boundaries of the currents field
     */
    private final int gridWidth;


    public ConstantCurrentVector(double xCurrent, double yCurrent, int gridHeight, int gridWidth) {
        this.currentVector = new Double2D(xCurrent,yCurrent);
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
    }

    @Override
    public int getGridHeight() {
        return gridHeight;
    }

    @Override
    public int getGridWidth() {
        return gridWidth;
    }

    @Override
    public Double2D getVector(int step, Int2D location) {
        return currentVector;
    }
}
