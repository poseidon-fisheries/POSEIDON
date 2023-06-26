package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.YearlyActionCounts;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Map;

public final class YearlyActionCountLimit<C extends YearlyActionCounts>
    implements Regulations<C> {

    private final Map<Class<? extends Action>, Integer> limits;

    public YearlyActionCountLimit(final Map<Class<? extends Action>, Integer> limits) {
        this.limits = ImmutableMap.copyOf(limits);
    }

    public Map<Class<? extends Action>, Integer> getLimits() {
        return limits;
    }

    @Override
    public Mode mode(final Action action, final C context) {
        return limits.entrySet().stream()
            .filter(entry -> entry.getKey().isAssignableFrom(action.getClass()))
            .mapToInt(Map.Entry::getValue)
            .filter(limit -> {
                final int count = context.getCount(
                    action.getDateTime().getYear(),
                    action.getAgent(),
                    action.getClass()
                );
                // forbid the action if the count is greater than the limit
                return count > limit;
            })
            .mapToObj(__ -> Mode.FORBIDDEN)
            .findFirst()
            .orElse(Mode.PERMITTED);
    }
}
