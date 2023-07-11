package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.time.MonthDay;

import static com.google.common.base.Preconditions.checkNotNull;

public class BetweenYearlyDates implements Condition {

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
    public boolean test(final Action action) {
        return action.getDateTime()
            .map(MonthDay::from)
            .map(this::test)
            .orElse(false);
    }

    boolean test(final MonthDay monthDay) {
        final boolean outsideRange = yearSpanning
            ? monthDay.isAfter(end) && monthDay.isBefore(start)
            : monthDay.isBefore(start) || monthDay.isAfter(end);
        return !outsideRange;
    }
}
