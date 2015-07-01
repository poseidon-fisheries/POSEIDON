package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.network.SocialNetwork;

import java.util.List;

/**
 * The result of calling populateModel from the scenario
 * Created by carrknight on 7/1/15.
 */
public class ScenarioPopulation
{


    /**
     * the initial list of fishers
     */
    private final List<Fisher> population;

    /**
     * the social network describing how the fishers are connected: should NOT be populated
     */
    private final SocialNetwork network;

    /**
     * The list of agents and a network ready to be populated!
     * @param population list of agents
     * @param network   network NOT populated
     */
    public ScenarioPopulation(List<Fisher> population, SocialNetwork network) {
        this.population = population;
        this.network = network;
    }


    public List<Fisher> getPopulation() {
        return population;
    }

    public SocialNetwork getNetwork() {
        return network;
    }
}
