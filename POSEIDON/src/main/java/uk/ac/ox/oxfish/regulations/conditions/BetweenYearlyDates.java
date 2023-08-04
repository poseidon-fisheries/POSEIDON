package uk.ac.ox.oxfish.regulations.conditions;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.time.Month;
import java.time.MonthDay;

public class BetweenYearlyDates implements AlgorithmFactory<Condition> {

    private IntegerParameter beginningMonth;
    private IntegerParameter beginningDay;
    private IntegerParameter endMonth;
    private IntegerParameter endDay;

    public BetweenYearlyDates() {
    }

    public BetweenYearlyDates(
        final MonthDay beginning,
        final MonthDay end
    ) {
        this(beginning.getMonth(), beginning.getDayOfMonth(), end.getMonth(), end.getDayOfMonth());
    }

    public BetweenYearlyDates(
        final Month beginningMonth,
        final int beginningDay,
        final Month endMonth,
        final int endDay
    ) {
        this(
            beginningMonth.getValue(),
            beginningDay,
            endMonth.getValue(),
            endDay
        );
    }

    public BetweenYearlyDates(
        final int beginningMonth,
        final int beginningDay,
        final int endMonth,
        final int endDay
    ) {
        this.beginningMonth = new IntegerParameter(beginningMonth);
        this.beginningDay = new IntegerParameter(beginningDay);
        this.endMonth = new IntegerParameter(endMonth);
        this.endDay = new IntegerParameter(endDay);
    }

    public BetweenYearlyDates(
        final IntegerParameter beginningMonth,
        final IntegerParameter beginningDay,
        final IntegerParameter endMonth,
        final IntegerParameter endDay
    ) {
        this.beginningMonth = beginningMonth;
        this.beginningDay = beginningDay;
        this.endMonth = endMonth;
        this.endDay = endDay;
    }

    public IntegerParameter getBeginningMonth() {
        return beginningMonth;
    }

    public void setBeginningMonth(final IntegerParameter beginningMonth) {
        this.beginningMonth = beginningMonth;
    }

    public IntegerParameter getBeginningDay() {
        return beginningDay;
    }

    public void setBeginningDay(final IntegerParameter beginningDay) {
        this.beginningDay = beginningDay;
    }

    public IntegerParameter getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(final IntegerParameter endMonth) {
        this.endMonth = endMonth;
    }

    public IntegerParameter getEndDay() {
        return endDay;
    }

    public void setEndDay(final IntegerParameter endDay) {
        this.endDay = endDay;
    }

    @Override
    public Condition apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.BetweenYearlyDates(
            MonthDay.of(beginningMonth.getValue(), beginningDay.getValue()),
            MonthDay.of(endMonth.getValue(), endDay.getValue())
        );
    }
}
