package uk.ac.ox.oxfish.fisher.purseseiner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.scenario.ScenarioPopulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class EmptyFleetFactory implements AlgorithmFactory<ScenarioPopulation> {
    @Override
    public ScenarioPopulation apply(final FishState fishState) {
        return new ScenarioPopulation(
            ImmutableList.of(),
            new SocialNetwork(new EmptyNetworkBuilder()),
            ImmutableMap.of() // no entry in the fishery so no need to pass factory here
        );
    }
}
