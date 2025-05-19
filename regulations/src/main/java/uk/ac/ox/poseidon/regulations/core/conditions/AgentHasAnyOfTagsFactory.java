/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
