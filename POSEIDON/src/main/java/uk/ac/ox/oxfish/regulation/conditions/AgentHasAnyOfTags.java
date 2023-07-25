package uk.ac.ox.oxfish.regulation.conditions;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class AgentHasAnyOfTags implements AlgorithmFactory<Condition> {

    private List<StringParameter> tags;

    public AgentHasAnyOfTags() {
    }

    public AgentHasAnyOfTags(final Collection<String> tags) {
        this.tags = tags.stream().map(StringParameter::new).collect(toImmutableList());
    }

    public List<StringParameter> getTags() {
        return tags;
    }

    public void setTags(final List<StringParameter> tags) {
        this.tags = tags;
    }

    @Override
    public Condition apply(final FishState fishState) {
        final List<AgentHasTag> agentHasTagFactories =
            tags.stream()
                .map(AgentHasTag::new)
                .collect(toImmutableList());
        return new AnyOf(agentHasTagFactories).apply(fishState);
    }
}
