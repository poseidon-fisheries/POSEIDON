package uk.ac.ox.poseidon.agents.api;

import java.util.Collection;

public interface YearlyActionCounts {
    default int getCount(final Action action) {
        return getCount(action.getDateTime().getYear(), action.getAgent(), action.getCode());
    }

    int getCount(int year, Agent agent, String actionCode);

    default int sumOfCounts(final int year, final Agent agent, final Collection<String> actionCodes) {
        return actionCodes.stream()
            .mapToInt(code -> getCount(year, agent, code))
            .sum();
    }
}
