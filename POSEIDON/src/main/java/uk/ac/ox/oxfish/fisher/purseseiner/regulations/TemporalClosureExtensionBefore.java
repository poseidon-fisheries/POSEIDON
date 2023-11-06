package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.oxfish.regulations.conditions.AgentHasTag;
import uk.ac.ox.oxfish.regulations.conditions.AllOf;
import uk.ac.ox.oxfish.regulations.conditions.AnyOf;
import uk.ac.ox.oxfish.regulations.conditions.BetweenYearlyDates;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.time.MonthDay;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.addDays;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.TemporalClosure.forbidDeploymentsBefore;
import static uk.ac.ox.oxfish.regulations.conditions.False.FALSE;

public class TemporalClosureExtensionBefore implements ConditionFactory {
    private TemporalClosure originalClosure;
    private IntegerParameter numberOfDaysToExtend;

    @SuppressWarnings("unused")
    public TemporalClosureExtensionBefore() {
    }

    @SuppressWarnings("WeakerAccess")
    public TemporalClosureExtensionBefore(
        final TemporalClosure originalClosure,
        final int numberOfDaysToExtend
    ) {
        this(originalClosure, new IntegerParameter(numberOfDaysToExtend));
    }

    @SuppressWarnings("WeakerAccess")
    public TemporalClosureExtensionBefore(
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
    public AlgorithmFactory<Condition> get() {
        final MonthDay newBeginning = addDays(originalClosure.beginning(), -numberOfDaysToExtend.getIntValue());
        final int daysOfForbiddenDeployments = originalClosure.getDaysToForbidDeploymentsBefore().getIntValue();
        return new AllOf(
            new AgentHasTag(originalClosure.getAgentTag().getValue()),
            new AnyOf(
                daysOfForbiddenDeployments >= 1
                    ? forbidDeploymentsBefore(newBeginning, daysOfForbiddenDeployments)
                    : FALSE,
                new BetweenYearlyDates(
                    newBeginning,
                    addDays(originalClosure.beginning(), -1)
                )
            )
        );
    }
}
