package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.Predicate;

public class AgentHasTag implements Predicate<Action> {
    private final String tag;

    public AgentHasTag(final String tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(final Action action) {
        return action.getAgent().getTags().contains(tag);
    }
}
