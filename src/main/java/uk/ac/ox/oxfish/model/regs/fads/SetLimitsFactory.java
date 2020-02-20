package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableSortedMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Map;

public class SetLimitsFactory implements AlgorithmFactory<SetLimits> {

    public Map<Integer, Integer> limits;

    @SuppressWarnings("unused") public SetLimitsFactory() {
        this(ImmutableSortedMap.of(0, 100));
    }

    public SetLimitsFactory(Map<Integer, Integer> limits) {
        this.limits = limits;
    }

    @SuppressWarnings("unused") public Map<Integer, Integer> getLimits() { return limits; }

    @SuppressWarnings("unused") public void setLimits(Map<Integer, Integer> limits) { this.limits = limits; }

    @Override public SetLimits apply(FishState fishState) {
        return new SetLimits(fishState, ImmutableSortedMap.copyOf(limits));
    }
}
