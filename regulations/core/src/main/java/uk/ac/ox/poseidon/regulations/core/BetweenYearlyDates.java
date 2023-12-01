package uk.ac.ox.poseidon.regulations.core;

import java.time.LocalDate;
import java.time.MonthDay;

import static com.google.common.base.Preconditions.checkNotNull;

public class BetweenYearlyDates extends DateCondition {

    private final MonthDay beginning;
    private final MonthDay end;
    private final boolean yearSpanning;

    public BetweenYearlyDates(final MonthDay beginning, final MonthDay end) {
        this.beginning = checkNotNull(beginning);
        this.end = checkNotNull(end);
        this.yearSpanning = end.isBefore(beginning);
    }

    public MonthDay getBeginning() {
        return beginning;
    }

    public MonthDay getEnd() {
        return end;
    }

    public boolean isYearSpanning() {
        return yearSpanning;
    }

    @Override
    boolean test(final LocalDate date) {
        return test(MonthDay.from(date));
    }

    boolean test(final MonthDay monthDay) {
        final boolean outsideRange = yearSpanning
            ? monthDay.isAfter(end) && monthDay.isBefore(beginning)
            : monthDay.isBefore(beginning) || monthDay.isAfter(end);
        return !outsideRange;
    }

    @Override
    public String toString() {
        return "BetweenYearlyDates{" +
            "beginning=" + beginning +
            ", end=" + end +
            ", yearSpanning=" + yearSpanning +
            '}';
    }
}
