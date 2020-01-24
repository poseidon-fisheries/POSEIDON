package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableSortedMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Map;

public class GeneralSetLimitsFactory implements AlgorithmFactory<GeneralSetLimits> {

    public Map<Integer, Integer> limits = ImmutableSortedMap.of(0, 100);

    @SuppressWarnings("unused") public Map<Integer, Integer> getLimits() { return limits; }

    @SuppressWarnings("unused") public void setLimits(Map<Integer, Integer> limits) { this.limits = limits; }

    @Override public GeneralSetLimits apply(FishState fishState) {
        final GeneralSetLimits generalSetLimits = new GeneralSetLimits(ImmutableSortedMap.copyOf(limits));
        fishState.registerStartable(generalSetLimits);
        return generalSetLimits;
    }
}
