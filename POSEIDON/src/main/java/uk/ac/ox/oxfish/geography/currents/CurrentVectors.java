package uk.ac.ox.oxfish.geography.currents;

import com.google.common.cache.Cache;
import sim.util.Double2D;
import sim.util.Int2D;

import java.util.Map;
import java.util.Optional;

public interface CurrentVectors {

    int getGridHeight();

    int getGridWidth();

    Double2D getVector(int step, Int2D location);
}
