package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableSortedMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.HashMap;
import java.util.Map;

public class ActiveFadLimitsFactory implements AlgorithmFactory<ActiveFadLimits> {

    // since ActiveFadsLimit has no mutable internal state, we can cache and reuse instances
    private final HashMap<Map<Integer, Integer>, ActiveFadLimits> cache = new HashMap<>();

    public Map<Integer, Integer> limits = ImmutableSortedMap.of(
        0, 70,
        213, 120,
        426, 300,
        1200, 450
    );

    @SuppressWarnings("unused") public Map<Integer, Integer> getLimits() { return limits; }

    @SuppressWarnings("unused") public void setLimits(Map<Integer, Integer> limits) { this.limits = limits; }

    @Override public ActiveFadLimits apply(FishState fishState) {
        return cache.computeIfAbsent(limits, __ -> new ActiveFadLimits(ImmutableSortedMap.copyOf(limits)));
    }
}
