package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class AgentHasAnyOfTagsFactory implements ComponentFactory<Condition> {

    private List<StringParameter> tags;

    public AgentHasAnyOfTagsFactory() {
    }

    public AgentHasAnyOfTagsFactory(final Collection<String> tags) {
        this.tags = tags.stream().map(StringParameter::new).collect(toImmutableList());
    }

    public List<StringParameter> getTags() {
        return tags;
    }

    public void setTags(final List<StringParameter> tags) {
        this.tags = tags;
    }

    @Override
    public Condition apply(final ModelState modelState) {
        final List<AgentHasTagFactory> agentHasTagFactoryFactories =
            tags.stream()
                .map(AgentHasTagFactory::new)
                .collect(toImmutableList());
        return new AnyOfFactory(agentHasTagFactoryFactories).apply(modelState);
    }
}
