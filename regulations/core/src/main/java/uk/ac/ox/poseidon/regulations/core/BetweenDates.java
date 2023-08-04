package uk.ac.ox.poseidon.regulations.core;

import java.time.LocalDate;

public class BetweenDates extends DateCondition {

    private final LocalDate beginningDate;
    private final LocalDate endDate;

    public BetweenDates(final LocalDate beginningDate, final LocalDate endDate) {
        this.beginningDate = beginningDate;
        this.endDate = endDate;
    }

    @Override
    boolean test(final LocalDate date) {
        return !(date.isBefore(beginningDate) || date.isAfter(endDate));
    }
}
