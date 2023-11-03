package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.oxfish.regulations.ForbiddenIf;
import uk.ac.ox.oxfish.regulations.conditions.*;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.time.MonthDay;
import java.util.List;

import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.addDays;
import static uk.ac.ox.oxfish.regulations.conditions.False.FALSE;

public class Closure implements RegulationFactory, YearsActive {
    private List<Integer> yearsActive;
    private StringParameter agentTag;
    private IntegerParameter beginningDay;
    private IntegerParameter beginningMonth;
    private IntegerParameter endDay;
    private IntegerParameter endMonth;
    private IntegerParameter daysToForbidDeploymentsBefore;

    @SuppressWarnings("unused")
    public Closure() {
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public Closure(
        final List<Integer> yearsActive,
        final String agentTag,
        final MonthDay beginning,
        final MonthDay end,
        final int daysToForbidDeploymentsBefore
    ) {
        this(
            yearsActive,
            new StringParameter(agentTag),
            new IntegerParameter(beginning.getDayOfMonth()),
            new IntegerParameter(beginning.getMonthValue()),
            new IntegerParameter(end.getDayOfMonth()),
            new IntegerParameter(end.getMonthValue()),
            new IntegerParameter(daysToForbidDeploymentsBefore)
        );
    }

    @SuppressWarnings("WeakerAccess")
    public Closure(
        final List<Integer> yearsActive,
        final StringParameter agentTag,
        final IntegerParameter beginningDay,
        final IntegerParameter beginningMonth,
        final IntegerParameter endDay,
        final IntegerParameter endMonth,
        final IntegerParameter daysToForbidDeploymentsBefore
    ) {
        this.agentTag = agentTag;
        this.yearsActive = yearsActive;
        this.beginningDay = beginningDay;
        this.beginningMonth = beginningMonth;
        this.endDay = endDay;
        this.endMonth = endMonth;
        this.daysToForbidDeploymentsBefore = daysToForbidDeploymentsBefore;
    }

    @SuppressWarnings("unused")
    public List<Integer> getYearsActive() {
        return yearsActive;
    }

    @SuppressWarnings("unused")
    public void setYearsActive(final List<Integer> yearsActive) {
        this.yearsActive = yearsActive;
    }

    @SuppressWarnings("WeakerAccess")
    public StringParameter getAgentTag() {
        return agentTag;
    }

    @SuppressWarnings("unused")
    public void setAgentTag(final StringParameter agentTag) {
        this.agentTag = agentTag;
    }

    @SuppressWarnings("WeakerAccess")
    public IntegerParameter getDaysToForbidDeploymentsBefore() {
        return daysToForbidDeploymentsBefore;
    }

    @SuppressWarnings("unused")
    public void setDaysToForbidDeploymentsBefore(final IntegerParameter daysToForbidDeploymentsBefore) {
        this.daysToForbidDeploymentsBefore = daysToForbidDeploymentsBefore;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getBeginningDay() {
        return beginningDay;
    }

    @SuppressWarnings("WeakerAccess")
    public void setBeginningDay(final IntegerParameter beginningDay) {
        this.beginningDay = beginningDay;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getBeginningMonth() {
        return beginningMonth;
    }

    @SuppressWarnings("WeakerAccess")
    public void setBeginningMonth(final IntegerParameter beginningMonth) {
        this.beginningMonth = beginningMonth;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getEndDay() {
        return endDay;
    }

    @SuppressWarnings("WeakerAccess")
    public void setEndDay(final IntegerParameter endDay) {
        this.endDay = endDay;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getEndMonth() {
        return endMonth;
    }

    @SuppressWarnings("WeakerAccess")
    public void setEndMonth(final IntegerParameter endMonth) {
        this.endMonth = endMonth;
    }

    @Override
    public AlgorithmFactory<Regulations> get() {
        final MonthDay beginning = getBeginning();
        return new ForbiddenIf(
            new AllOf(
                new AgentHasTag(agentTag.getValue()),
                new AnyOf(yearsActive.stream().map(InYear::new)),
                new AnyOf(
                    daysToForbidDeploymentsBefore.getIntValue() >= 1
                        ? forbidDeploymentsBefore(beginning, daysToForbidDeploymentsBefore.getIntValue())
                        : FALSE,
                    new BetweenYearlyDates(beginning, getEnd())
                )
            )
        );
    }

    public MonthDay getBeginning() {
        return makeMonthDay(beginningMonth, beginningDay);
    }

    static AllOf forbidDeploymentsBefore(final MonthDay beginning, final int numDays) {
        return new AllOf(
            new ActionCodeIs("DPL"),
            new BetweenYearlyDates(
                addDays(beginning, -numDays),
                addDays(beginning, -1)
            )
        );
    }

    public MonthDay getEnd() {
        return makeMonthDay(endMonth, endDay);
    }

    private static MonthDay makeMonthDay(final IntegerParameter month, final IntegerParameter day) {
        return MonthDay.of(month.getIntValue(), day.getIntValue());
    }

    public void setEnd(final MonthDay monthDay) {
        setEndMonth(new IntegerParameter(monthDay.getMonthValue()));
        setEndDay(new IntegerParameter(monthDay.getDayOfMonth()));
    }

    public void setBeginning(final MonthDay monthDay) {
        setBeginningMonth(new IntegerParameter(monthDay.getMonthValue()));
        setBeginningDay(new IntegerParameter(monthDay.getDayOfMonth()));
    }
}
