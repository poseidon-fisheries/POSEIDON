/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static uk.ac.ox.oxfish.model.regs.MultipleRegulations.TAG_FOR_ALL;

/**
 * Factory to generate multiple algorithms map
 * Created by carrknight on 4/4/17.
 */
public class MultipleRegulationsFactory implements AlgorithmFactory<MultipleRegulations> {

    //maps of weird maps are really hard to read for SnakeYAML so I am going to use to separate list and
    //generate the map on the second list

    private LinkedList<String> tags;
    private LinkedList<AlgorithmFactory<? extends Regulation>> factories;

    public MultipleRegulationsFactory(
        LinkedList<AlgorithmFactory<? extends Regulation>> factories,
        LinkedList<String> tags
    ) {
        this.factories = factories;
        this.tags = tags;
    }

    /**
     * Allows constructing a MultipleRegulationsFactory directly with a map from regulation factories to tag,
     * even if those will be converted to lists and then back to a map again when the MultipleRegulations object
     * gets constructed. We take an ImmutableMap instead of just a Map because it gives us guaranteed stable
     * iteration order of both keys and values.
     */
    public MultipleRegulationsFactory(ImmutableMap<AlgorithmFactory<? extends Regulation>, String> regulations) {
        this(new LinkedList<>(regulations.keySet()), new LinkedList<>(regulations.values()));
    }

    /**
     * default factories provided
     */
    public MultipleRegulationsFactory() {
        this(ImmutableMap.of(
            new ITQMonoFactory(2500), TAG_FOR_ALL,
            new ProtectedAreasOnlyFactory(), TAG_FOR_ALL
        ));
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MultipleRegulations apply(FishState fishState) {

        Preconditions.checkArgument(factories.size()>0);
        Preconditions.checkArgument(factories.size()==tags.size());
        Map<AlgorithmFactory<? extends Regulation>,String> regulations = new HashMap<>();

        for(int i=0; i<tags.size(); i++)
            regulations.put(factories.get(i),tags.get(i));

        return new MultipleRegulations(regulations);
    }

    /**
     * Getter for property 'tags'.
     *
     * @return Value for property 'tags'.
     */
    public LinkedList<String> getTags() {
        return tags;
    }

    /**
     * Setter for property 'tags'.
     *
     * @param tags Value to set for property 'tags'.
     */
    public void setTags(LinkedList<String> tags) {
        this.tags = tags;
    }

    /**
     * Getter for property 'factories'.
     *
     * @return Value for property 'factories'.
     */
    public LinkedList<AlgorithmFactory<? extends Regulation>> getFactories() {
        return factories;
    }

    /**
     * Setter for property 'factories'.
     *
     * @param factories Value to set for property 'factories'.
     */
    public void setFactories(
            LinkedList<AlgorithmFactory<? extends Regulation>> factories) {
        this.factories = factories;
    }
}
