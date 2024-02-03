package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.core.conditions.AgentHasTagFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.AllOfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.BetweenYearlyDatesFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.FalseFactory;

import java.time.MonthDay;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.addDays;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.TemporalClosure.forbidDeploymentsBefore;

public class TemporalClosureExtensionBeforeFactory implements ComponentFactory<Condition> {
    private TemporalClosure originalClosure;
    private IntegerParameter numberOfDaysToExtend;

    @SuppressWarnings("unused")
    public TemporalClosureExtensionBeforeFactory() {
    }

    @SuppressWarnings("WeakerAccess")
    public TemporalClosureExtensionBeforeFactory(
        final TemporalClosure originalClosure,
        final int numberOfDaysToExtend
    ) {
        this(originalClosure, new IntegerParameter(numberOfDaysToExtend));
    }

    @SuppressWarnings("WeakerAccess")
    public TemporalClosureExtensionBeforeFactory(
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
        final MonthDay newBeginning = addDays(originalClosure.beginning(), -numberOfDaysToExtend.getIntValue());
        final int daysOfForbiddenDeployments = originalClosure.getDaysToForbidDeploymentsBefore().getIntValue();
        return new AllOfFactory(
            new AgentHasTagFactory(originalClosure.getAgentTag().getValue()),
            new AllOfFactory(
                daysOfForbiddenDeployments >= 1
                    ? forbidDeploymentsBefore(newBeginning, daysOfForbiddenDeployments)
                    : new FalseFactory(),
                new BetweenYearlyDatesFactory(
                    newBeginning,
                    addDays(originalClosure.beginning(), -1)
                )
            )
        ).apply(modelState);
    }

}
