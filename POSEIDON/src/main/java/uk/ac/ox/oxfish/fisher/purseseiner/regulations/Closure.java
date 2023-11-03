package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.oxfish.regulations.ForbiddenIf;
import uk.ac.ox.oxfish.regulations.conditions.*;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.time.MonthDay;

import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.addDays;
import static uk.ac.ox.oxfish.regulations.conditions.False.FALSE;

public class Closure implements RegulationFactory {
    private IntegerParameter beginningDay;
    private IntegerParameter beginningMonth;
    private IntegerParameter endDay;
    private IntegerParameter endMonth;
    private IntegerParameter daysToForbidDeploymentsBefore;

    public Closure() {
    }

    public Closure(
        final MonthDay beginning,
        final MonthDay end,
        final int daysToForbidDeploymentsBefore
    ) {
        this(
            new IntegerParameter(beginning.getDayOfMonth()),
            new IntegerParameter(beginning.getMonthValue()),
            new IntegerParameter(end.getDayOfMonth()),
            new IntegerParameter(end.getMonthValue()),
            new IntegerParameter(daysToForbidDeploymentsBefore)
        );
    }

    public Closure(
        final IntegerParameter beginningDay,
        final IntegerParameter beginningMonth,
        final IntegerParameter endDay,
        final IntegerParameter endMonth,
        final IntegerParameter daysToForbidDeploymentsBefore
    ) {
        this.beginningDay = beginningDay;
        this.beginningMonth = beginningMonth;
        this.endDay = endDay;
        this.endMonth = endMonth;
        this.daysToForbidDeploymentsBefore = daysToForbidDeploymentsBefore;
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void setBeginningDay(final IntegerParameter beginningDay) {
        this.beginningDay = beginningDay;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getBeginningMonth() {
        return beginningMonth;
    }

    @SuppressWarnings("unused")
    public void setBeginningMonth(final IntegerParameter beginningMonth) {
        this.beginningMonth = beginningMonth;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getEndDay() {
        return endDay;
    }

    @SuppressWarnings("unused")
    public void setEndDay(final IntegerParameter endDay) {
        this.endDay = endDay;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getEndMonth() {
        return endMonth;
    }

    @SuppressWarnings("unused")
    public void setEndMonth(final IntegerParameter endMonth) {
        this.endMonth = endMonth;
    }

    @Override
    public AlgorithmFactory<Regulations> get() {
        final MonthDay beginning = getBeginning();
        return new ForbiddenIf(
            new AnyOf(
                daysToForbidDeploymentsBefore.getIntValue() >= 1
                    ? forbidDeploymentsBefore(beginning, daysToForbidDeploymentsBefore.getIntValue())
                    : FALSE,
                new AllOf(
                    new AgentHasTag("closure A"),
                    new BetweenYearlyDates(
                        beginning,
                        getEnd()
                    )
                )
            )
        );
    }

    public MonthDay getBeginning() {
        return makeMonthDay(beginningMonth, beginningDay);
    }

    private AllOf forbidDeploymentsBefore(final MonthDay beginning, final int numDays) {
        return new AllOf(
            new AgentHasTag("closure A"),
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
}
