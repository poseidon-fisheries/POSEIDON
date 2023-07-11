package uk.ac.ox.poseidon.agents.core;

import com.google.common.util.concurrent.AtomicLongMap;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.ActionCounter;
import uk.ac.ox.poseidon.agents.api.Agent;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class AtomicLongMapActionCounter implements ActionCounter {

    private final Map<? super Agent, AtomicLongMap<String>> counts;

    private AtomicLongMapActionCounter(final Map<? super Agent, AtomicLongMap<String>> counts) {
        this.counts = counts;
    }

    public static ActionCounter create() {
        return new AtomicLongMapActionCounter(new HashMap<>());
    }

    @Override
    public void observe(final Action action) {
        countsFor(action.getAgent()).incrementAndGet(action.getCode());
    }

    private AtomicLongMap<String> countsFor(final Agent agent) {
        return counts.computeIfAbsent(agent, __ -> AtomicLongMap.create());
    }

    @Override
    public long getCount(final Agent agent, final String actionCode) {
        return countsFor(agent).get(actionCode);
    }

    @Override
    public ActionCounter copy() {
        return new AtomicLongMapActionCounter(
            counts.entrySet().stream().collect(toMap(
                Map.Entry::getValue,
                entry -> AtomicLongMap.create(entry.getValue().asMap())
            ))
        );
    }
}
