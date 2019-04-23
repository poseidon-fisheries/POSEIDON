package uk.ac.ox.oxfish.geography.currents;

import java.util.List;
import java.util.function.Function;

public class CurrentMaps {

    private final List<VectorGrid2D> currentsMaps;
    private Function<Long, Integer> currentsSchedule; // function from Schedule.getSteps to a currentsMaps index

    public CurrentMaps(
        List<VectorGrid2D> currentsMaps, Function<Long, Integer> currentsSchedule
    ) {
        this.currentsMaps = currentsMaps;
        this.currentsSchedule = currentsSchedule;
    }

    public VectorGrid2D atSteps(long steps) {
        return currentsMaps.get(currentsSchedule.apply(steps));
    }
}
