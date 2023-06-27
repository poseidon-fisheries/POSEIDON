package uk.ac.ox.poseidon.agents.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.ActionCounter;
import uk.ac.ox.poseidon.agents.api.Agent;
import uk.ac.ox.poseidon.agents.api.YearlyActionCounter;

import java.util.HashMap;
import java.util.Map;

public class MultisetYearlyActionCounter implements YearlyActionCounter {

    private final Map<? super Integer, ActionCounter> counts;

    private MultisetYearlyActionCounter(final Map<? super Integer, ActionCounter> counts) {
        this.counts = counts;
    }

    public static YearlyActionCounter create() {
        return new MultisetYearlyActionCounter(new HashMap<>());
    }

    @Override
    public int getCount(
        final int year,
        final Agent agent,
        final String actionCode
    ) {
        return counts
            .computeIfAbsent(year, __ -> MultisetActionCounter.create())
            .getCount(agent, actionCode);
    }

    @Override
    public void observe(final Action action) {
        counts
            .computeIfAbsent(
                action.getDateTime().getYear(),
                __ -> MultisetActionCounter.create()
            )
            .observe(action);
    }
}
