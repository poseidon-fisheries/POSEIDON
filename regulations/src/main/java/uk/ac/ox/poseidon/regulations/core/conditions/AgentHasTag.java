package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class AgentHasTag implements Condition {
    private final String tag;

    public AgentHasTag(final String tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(final Action action) {
        return action.getAgent().getTags().contains(tag);
    }

    @Override
    public String toString() {
        return "AgentHasTag{" +
            "tag='" + tag + '\'' +
            '}';
    }
}
