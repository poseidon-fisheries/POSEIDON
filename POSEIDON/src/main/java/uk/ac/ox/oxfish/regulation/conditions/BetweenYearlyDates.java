package uk.ac.ox.oxfish.regulation.conditions;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.time.Month;
import java.time.MonthDay;

public class BetweenYearlyDates implements AlgorithmFactory<Condition> {

    private IntegerParameter startMonth;
    private IntegerParameter startDay;
    private IntegerParameter endMonth;
    private IntegerParameter endDay;

    public BetweenYearlyDates() {
    }

    public BetweenYearlyDates(
        final Month startMonth,
        final int startDay,
        final Month endMonth,
        final int endDay
    ) {
        this(
            startMonth.getValue(),
            startDay,
            endMonth.getValue(),
            endDay
        );
    }

    public BetweenYearlyDates(
        final int startMonth,
        final int startDay,
        final int endMonth,
        final int endDay
    ) {
        this.startMonth = new IntegerParameter(startMonth);
        this.startDay = new IntegerParameter(startDay);
        this.endMonth = new IntegerParameter(endMonth);
        this.endDay = new IntegerParameter(endDay);
    }

    public BetweenYearlyDates(
        final IntegerParameter startMonth,
        final IntegerParameter startDay,
        final IntegerParameter endMonth,
        final IntegerParameter endDay
    ) {
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.endMonth = endMonth;
        this.endDay = endDay;
    }

    public IntegerParameter getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(final IntegerParameter startMonth) {
        this.startMonth = startMonth;
    }

    public IntegerParameter getStartDay() {
        return startDay;
    }

    public void setStartDay(final IntegerParameter startDay) {
        this.startDay = startDay;
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
            MonthDay.of(startMonth.getValue(), startDay.getValue()),
            MonthDay.of(endMonth.getValue(), endDay.getValue())
        );
    }
}
