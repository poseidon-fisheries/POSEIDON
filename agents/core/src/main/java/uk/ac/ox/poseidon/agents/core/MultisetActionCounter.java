package uk.ac.ox.poseidon.agents.core;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.ActionCounter;
import uk.ac.ox.poseidon.agents.api.Agent;

import java.util.HashMap;
import java.util.Map;

public class MultisetActionCounter implements ActionCounter {

    private final Map<? super Agent, Multiset<Class<? extends Action>>> counts;

    private MultisetActionCounter(final Map<? super Agent, Multiset<Class<? extends Action>>> counts) {
        this.counts = counts;
    }

    public static ActionCounter create() {
        return new MultisetActionCounter(new HashMap<>());
    }

    @Override
    public void observe(final Action action) {
        counts
            .computeIfAbsent(action.getAgent(), __ -> HashMultiset.create())
            .add(action.getClass());
    }

    @Override
    public int getCount(final Agent agent, final Class<? extends Action> action) {
        return counts
            .computeIfAbsent(agent, __ -> HashMultiset.create())
            .count(action);
    }
}
