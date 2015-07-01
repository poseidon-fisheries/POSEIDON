package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * A scenario is a set of commands called to initialize the model
 */
public interface Scenario {

    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    public ScenarioEssentials start(FishState model);


    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     * @param model the model
     * @return a list of agents
     */
    public ScenarioPopulation populateModel(FishState model);




}
