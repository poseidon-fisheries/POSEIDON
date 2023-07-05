package uk.ac.ox.oxfish.regulations.factories;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.Predicate;

public class AgentHasTag implements AlgorithmFactory<Predicate<Action>> {
    private String tag;

    public AgentHasTag() {
    }

    public AgentHasTag(final String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    @Override
    public Predicate<Action> apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.AgentHasTag(tag);
    }
}
