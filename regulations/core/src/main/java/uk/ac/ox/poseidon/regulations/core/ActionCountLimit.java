package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.ActionCounts;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.Map;

public final class ActionCountLimit<A extends Action, C extends ActionCounts>
    implements Regulation<A, C> {

    private final Map<Class<? extends A>, Integer> limits;

    public ActionCountLimit(final Map<Class<? extends A>, Integer> limits) {
        this.limits = ImmutableMap.copyOf(limits);
    }

    public Map<Class<? extends A>, Integer> getLimits() {
        return limits;
    }

    @Override
    public Mode mode(final A action, final C context) {
        return limits.entrySet().stream()
            .filter(entry -> entry.getKey().isAssignableFrom(action.getClass()))
            .mapToInt(Map.Entry::getValue)
            .filter(limit -> context.getCount(action.getAgent(), action.getClass()) > limit)
            .mapToObj(__ -> Mode.FORBIDDEN)
            .findFirst()
            .orElse(Mode.PERMITTED);
    }
}
