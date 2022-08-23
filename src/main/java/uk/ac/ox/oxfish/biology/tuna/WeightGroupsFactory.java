package uk.ac.ox.oxfish.biology.tuna;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

public class WeightGroupsFactory implements Supplier<Map<String, WeightGroups>> {

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
    public Map<String, WeightGroups> get() {
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
