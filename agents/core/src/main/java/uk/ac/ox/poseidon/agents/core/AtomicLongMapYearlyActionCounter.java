package uk.ac.ox.poseidon.agents.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.ActionCounter;
import uk.ac.ox.poseidon.agents.api.Agent;
import uk.ac.ox.poseidon.agents.api.YearlyActionCounter;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class AtomicLongMapYearlyActionCounter implements YearlyActionCounter {

    private final Map<? super Integer, ActionCounter> counts;

    private AtomicLongMapYearlyActionCounter(final Map<? super Integer, ActionCounter> counts) {
        this.counts = counts;
    }

    public static YearlyActionCounter create() {
        return new AtomicLongMapYearlyActionCounter(new HashMap<>());
    }

    @Override
    public long getCount(
        final int year,
        final Agent agent,
        final String actionCode
    ) {
        return getActionCounter(year).getCount(agent, actionCode);
    }

    private ActionCounter getActionCounter(final int year) {
        return counts.computeIfAbsent(year, __ -> AtomicLongMapActionCounter.create());
    }

    @Override
    public void observe(final Action action) {
        action.getDateTime().ifPresent(dateTime ->
            getActionCounter(dateTime.getYear()).observe(action)
        );
    }

    @Override
    public YearlyActionCounter copy() {
        return new AtomicLongMapYearlyActionCounter(
            counts.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().copy()
            ))
        );
    }
}
