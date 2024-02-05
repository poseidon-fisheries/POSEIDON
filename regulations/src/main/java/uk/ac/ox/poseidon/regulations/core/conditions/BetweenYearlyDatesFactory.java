package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.time.Month;
import java.time.MonthDay;

public class BetweenYearlyDatesFactory implements ComponentFactory<Condition> {

    private IntegerParameter beginningMonth;
    private IntegerParameter beginningDay;
    private IntegerParameter endMonth;
    private IntegerParameter endDay;

    public BetweenYearlyDatesFactory() {
    }

    public BetweenYearlyDatesFactory(
        final MonthDay beginning,
        final MonthDay end
    ) {
        this(beginning.getMonth(), beginning.getDayOfMonth(), end.getMonth(), end.getDayOfMonth());
    }

    public BetweenYearlyDatesFactory(
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

    public BetweenYearlyDatesFactory(
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

    public BetweenYearlyDatesFactory(
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
    public Condition apply(final ModelState ignored) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.BetweenYearlyDates(
            MonthDay.of(beginningMonth.getValue(), beginningDay.getValue()),
            MonthDay.of(endMonth.getValue(), endDay.getValue())
        );
    }
}
