package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class ActiveFadLimitsFactory implements ComponentFactory<Regulations> {

    // The map keys need to be strings to be accessible as Java beans, even though they
    // represent years. They will be converted to integers at the time of generating the
    // component. It's the user's responsibility to provide convertible values.
    private Map<String, ? extends Map<String, Integer>> limitsPerYearAndClass;

    @SuppressWarnings("unused")
    public ActiveFadLimitsFactory() {
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ActiveFadLimitsFactory(
        final Map<String, ? extends Map<String, Integer>> limitsPerYearAndClass
    ) {
        this.limitsPerYearAndClass = limitsPerYearAndClass;
    }

    @SuppressWarnings("unused")
    public Map<String, ? extends Map<String, Integer>> getLimitsPerYearAndClass() {
        return limitsPerYearAndClass;
    }

    @SuppressWarnings("unused")
    public void setLimitsPerYearAndClass(final Map<String, ? extends Map<String, Integer>> limitsPerYearAndClass) {
        this.limitsPerYearAndClass = limitsPerYearAndClass;
    }

    @Override
    public Regulations apply(final ModelState modelState) {
        return new ActiveFadLimits(
            limitsPerYearAndClass.entrySet().stream().collect(toImmutableMap(
                entry -> Integer.valueOf(entry.getKey()),
                Map.Entry::getValue
            )),
            modelState
        );
    }
}
