package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.oxfish.regulations.ForbiddenIf;
import uk.ac.ox.oxfish.regulations.conditions.*;
import uk.ac.ox.oxfish.regulations.quantities.NumberOfActiveFads;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Map;

public class ActiveFadLimits implements RegulationFactory {

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
    public AlgorithmFactory<Regulations> get() {
        return new ForbiddenIf(
            new AllOf(
                new ActionCodeIs("DPL"),
                new AnyOf(
                    limitsPerYearAndClass.entrySet().stream().map(yearAndLimits ->
                        new AllOf(
                            new InYear(yearAndLimits.getKey()),
                            new AnyOf(
                                yearAndLimits.getValue().entrySet().stream().map(classAndLimit ->
                                    new AllOf(
                                        new AgentHasTag("class " + classAndLimit.getKey()),
                                        new NotBelow(new NumberOfActiveFads(), classAndLimit.getValue())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
    }
}
