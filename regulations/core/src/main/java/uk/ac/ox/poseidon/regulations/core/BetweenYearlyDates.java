package uk.ac.ox.poseidon.regulations.core;

import java.time.LocalDate;
import java.time.MonthDay;

import static com.google.common.base.Preconditions.checkNotNull;

public class BetweenYearlyDates extends DateCondition {

    private final MonthDay start;
    private final MonthDay end;
    private final boolean yearSpanning;

    public BetweenYearlyDates(final MonthDay start, final MonthDay end) {
        this.start = checkNotNull(start);
        this.end = checkNotNull(end);
        this.yearSpanning = end.isBefore(start);
    }

    public MonthDay getStart() {
        return start;
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
            ? monthDay.isAfter(end) && monthDay.isBefore(start)
            : monthDay.isBefore(start) || monthDay.isAfter(end);
        return !outsideRange;
    }
}
