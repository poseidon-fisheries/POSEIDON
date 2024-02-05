package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class AgentHasTagFactory implements ComponentFactory<Condition> {
    private StringParameter tag;

    public AgentHasTagFactory() {
    }

    public AgentHasTagFactory(final String tag) {
        this(new StringParameter(tag));
    }

    public AgentHasTagFactory(final StringParameter tag) {
        this.tag = tag;
    }

    public StringParameter getTag() {
        return tag;
    }

    public void setTag(final StringParameter tag) {
        this.tag = tag;
    }

    @Override
    public Condition apply(final ModelState ignored) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.AgentHasTag(tag.getValue());
    }

}
