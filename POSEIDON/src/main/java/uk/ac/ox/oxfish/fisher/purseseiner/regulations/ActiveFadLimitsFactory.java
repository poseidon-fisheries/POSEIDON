package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Map;

public class ActiveFadLimitsFactory implements ComponentFactory<Regulations> {

    private Map<Integer, ? extends Map<String, Integer>> limitsPerYearAndClass;

    @SuppressWarnings("unused")
    public ActiveFadLimitsFactory() {
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ActiveFadLimitsFactory(
        final Map<Integer, ? extends Map<String, Integer>> limitsPerYearAndClass
    ) {
        this.limitsPerYearAndClass = limitsPerYearAndClass;
    }

    @SuppressWarnings("unused")
    public Map<Integer, ? extends Map<String, Integer>> getLimitsPerYearAndClass() {
        return limitsPerYearAndClass;
    }

    @SuppressWarnings("unused")
    public void setLimitsPerYearAndClass(final Map<Integer, ? extends Map<String, Integer>> limitsPerYearAndClass) {
        this.limitsPerYearAndClass = limitsPerYearAndClass;
    }

    @Override
    public Regulations apply(final ModelState modelState) {
        return new ActiveFadLimits(limitsPerYearAndClass, modelState);
    }
}
