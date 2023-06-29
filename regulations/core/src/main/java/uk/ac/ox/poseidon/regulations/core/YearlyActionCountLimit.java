package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.Agent;
import uk.ac.ox.poseidon.agents.api.YearlyActionCounts;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public final class YearlyActionCountLimit extends Interdiction<YearlyActionCounts> {

    private final Map<Set<String>, Integer> limits;

    public YearlyActionCountLimit(final Map<Set<String>, Integer> limits) {
        this.limits = limits
            .entrySet()
            .stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    entry -> entry.getKey().stream().collect(toImmutableSet()),
                    Entry::getValue
                )
            );
    }

    public Map<Set<String>, Integer> getLimits() {
        return limits;
    }

    @Override
    public boolean test(final Action action, final YearlyActionCounts yearlyActionCounts) {
        return getRemainingActions(action, yearlyActionCounts) <= 0;
    }

    @SuppressWarnings("WeakerAccess")
    public int getRemainingActions(
        final Action action,
        final YearlyActionCounts yearlyActionCounts
    ) {
        return getRemainingActions(
            action.getDateTime().getYear(),
            action.getAgent(),
            action.getCode(),
            yearlyActionCounts
        );
    }

    public int getRemainingActions(
        final int year,
        final Agent agent,
        final String actionCode,
        final YearlyActionCounts yearlyActionCounts
    ) {
        return getApplicableLimits(actionCode)
            .mapToInt(entry ->
                entry.getValue() - yearlyActionCounts.sumOfCounts(year, agent, entry.getKey())
            )
            .min()
            .orElse(Integer.MAX_VALUE);
    }

    public Stream<Entry<Set<String>, Integer>> getApplicableLimits(final String actionCode) {
        return limits
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().contains(actionCode));
    }
}
