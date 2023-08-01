package uk.ac.ox.poseidon.regulations.core;

import java.time.LocalDate;

public class BetweenDates extends DateCondition {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public BetweenDates(final LocalDate startDate, final LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    boolean test(final LocalDate date) {
        return !(date.isBefore(startDate) || date.isAfter(endDate));
    }
}
