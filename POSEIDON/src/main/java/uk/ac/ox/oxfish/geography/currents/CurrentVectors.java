package uk.ac.ox.oxfish.geography.currents;

import sim.util.Double2D;
import sim.util.Int2D;

public interface CurrentVectors {

    int getGridHeight();

    int getGridWidth();

    Double2D getVector(int step, Int2D location);
}
