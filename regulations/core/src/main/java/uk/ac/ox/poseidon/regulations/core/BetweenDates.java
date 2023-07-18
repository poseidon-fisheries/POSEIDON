package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BetweenDates implements Condition {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public BetweenDates(final LocalDate startDate, final LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public boolean test(final Action action) {
        return action
            .getDateTime()
            .map(LocalDateTime::toLocalDate)
            .map(this::test)
            .orElse(false);
    }

    boolean test(final LocalDate date) {
        return !(date.isBefore(startDate) || date.isAfter(endDate));
    }
}
