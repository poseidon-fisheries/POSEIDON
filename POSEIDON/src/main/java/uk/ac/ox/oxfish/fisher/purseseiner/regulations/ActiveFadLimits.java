package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.oxfish.regulations.quantities.NumberOfActiveFads;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.ForbiddenIfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.*;

import java.util.Map;

public class ActiveFadLimits implements ComponentFactory<Regulations> {

    private Map<Integer, ? extends Map<String, Integer>> limitsPerYearAndClass;

    @SuppressWarnings("unused")
    public ActiveFadLimits() {
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ActiveFadLimits(
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
        return new ForbiddenIfFactory(
            new AllOfFactory(
                new ActionCodeIsFactory("DPL"),
                new AnyOfFactory(
                    limitsPerYearAndClass.entrySet().stream().map(yearAndLimits ->
                        new AllOfFactory(
                            new InYearFactory(yearAndLimits.getKey()),
                            new AnyOfFactory(
                                yearAndLimits.getValue().entrySet().stream().map(classAndLimit ->
                                    new AllOfFactory(
                                        new AgentHasTagFactory("class " + classAndLimit.getKey()),
                                        new NotBelowFactory(new NumberOfActiveFads(), classAndLimit.getValue())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).apply(modelState);
    }
}
