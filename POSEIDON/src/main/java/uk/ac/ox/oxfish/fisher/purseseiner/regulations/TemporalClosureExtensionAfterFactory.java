package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.core.conditions.AgentHasTagFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.AllOfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.BetweenYearlyDatesFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.addDays;

public class TemporalClosureExtensionAfterFactory implements ComponentFactory<Condition> {
    private TemporalClosure originalClosure;
    private IntegerParameter numberOfDaysToExtend;

    @SuppressWarnings("unused")
    public TemporalClosureExtensionAfterFactory() {
    }

    public TemporalClosureExtensionAfterFactory(
        final TemporalClosure originalClosure,
        final int numberOfDaysToExtend
    ) {
        this(originalClosure, new IntegerParameter(numberOfDaysToExtend));
    }

    @SuppressWarnings("WeakerAccess")
    public TemporalClosureExtensionAfterFactory(
        final TemporalClosure originalClosure,
        final IntegerParameter numberOfDaysToExtend
    ) {
        checkArgument(numberOfDaysToExtend.getIntValue() >= 1);
        this.originalClosure = originalClosure;
        this.numberOfDaysToExtend = numberOfDaysToExtend;
    }

    @SuppressWarnings("unused")
    public TemporalClosure getOriginalClosure() {
        return originalClosure;
    }

    @SuppressWarnings("unused")
    public void setOriginalClosure(final TemporalClosure originalClosure) {
        this.originalClosure = originalClosure;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getNumberOfDaysToExtend() {
        return numberOfDaysToExtend;
    }

    @SuppressWarnings("unused")
    public void setNumberOfDaysToExtend(final IntegerParameter numberOfDaysToExtend) {
        checkArgument(numberOfDaysToExtend.getIntValue() >= 1);
        this.numberOfDaysToExtend = numberOfDaysToExtend;
    }

    @Override
    public Condition apply(final ModelState modelState) {
        return new AllOfFactory(
            new AgentHasTagFactory(originalClosure.getAgentTag().getValue()),
            new BetweenYearlyDatesFactory(
                addDays(originalClosure.end(), 1),
                addDays(originalClosure.end(), numberOfDaysToExtend.getIntValue())
            )
        ).apply(modelState);
    }
}
