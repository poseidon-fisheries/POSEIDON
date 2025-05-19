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

package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

public class WeightGroupsFactory implements AlgorithmFactory<Map<String, WeightGroups>> {

    private Map<String, List<String>> weightGroupNamesPerSpecies;
    private Map<String, List<Double>> weightGroupCutoffsPerSpecies;


    /**
     * Empty constructor for YAML initialisation
     */
    @SuppressWarnings("unused")
    public WeightGroupsFactory() {
    }

    public WeightGroupsFactory(
        final Collection<String> speciesNames,
        final List<String> weightGroupNames,
        final List<Double> weightGroupCutoffs
    ) {
        this(
            speciesNames.stream().collect(toMap(identity(), __ -> weightGroupNames)),
            speciesNames.stream().collect(toMap(identity(), __ -> weightGroupCutoffs))
        );
    }

    public WeightGroupsFactory(
        final Map<String, List<String>> weightGroupNamesPerSpecies,
        final Map<String, List<Double>> weightGroupCutoffsPerSpecies
    ) {
        checkArgument(weightGroupNamesPerSpecies.keySet().equals(weightGroupCutoffsPerSpecies.keySet()));
        this.weightGroupNamesPerSpecies = weightGroupNamesPerSpecies;
        this.weightGroupCutoffsPerSpecies = weightGroupCutoffsPerSpecies;
    }

    public Map<String, List<String>> getWeightGroupNamesPerSpecies() {
        return weightGroupNamesPerSpecies;
    }

    public void setWeightGroupNamesPerSpecies(Map<String, List<String>> weightGroupNamesPerSpecies) {
        this.weightGroupNamesPerSpecies = weightGroupNamesPerSpecies;
    }

    public Map<String, List<Double>> getWeightGroupCutoffsPerSpecies() {
        return weightGroupCutoffsPerSpecies;
    }

    public void setWeightGroupCutoffsPerSpecies(Map<String, List<Double>> weightGroupCutoffsPerSpecies) {
        this.weightGroupCutoffsPerSpecies = weightGroupCutoffsPerSpecies;
    }

    @Override
    public Map<String, WeightGroups> apply(FishState fishState) {
        checkState(weightGroupNamesPerSpecies.keySet().equals(weightGroupCutoffsPerSpecies.keySet()));
        return weightGroupNamesPerSpecies.keySet().stream().collect(toImmutableMap(
            identity(),
            speciesName -> new WeightGroups(
                weightGroupNamesPerSpecies.get(speciesName),
                weightGroupCutoffsPerSpecies.get(speciesName)
            )
        ));
    }
}
