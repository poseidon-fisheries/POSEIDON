package uk.ac.ox.oxfish.geography.currents;

import java.util.List;
import java.util.function.Function;

public class CurrentMaps {

    private final List<VectorGrid2D> vectorGrids;
    private Function<Long, Integer> currentsSchedule; // function from Schedule.getSteps to a vectorGrids index

    public CurrentMaps(
        List<VectorGrid2D> vectorGrids, Function<Long, Integer> currentsSchedule
    ) {
        this.vectorGrids = vectorGrids;
        this.currentsSchedule = currentsSchedule;
    }

    public VectorGrid2D atSteps(long steps) {
        return vectorGrids.get(currentsSchedule.apply(steps));
    }
}
