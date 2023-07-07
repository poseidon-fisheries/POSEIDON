package uk.ac.ox.oxfish.regulation.conditions;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class AgentHasTag implements AlgorithmFactory<Condition> {
    private StringParameter tag;

    public AgentHasTag() {
    }

    public AgentHasTag(final String tag) {
        this(new StringParameter(tag));
    }

    public AgentHasTag(final StringParameter tag) {
        this.tag = tag;
    }

    public StringParameter getTag() {
        return tag;
    }

    public void setTag(final StringParameter tag) {
        this.tag = tag;
    }

    @Override
    public Condition apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.AgentHasTag(tag.getValue());
    }
}
