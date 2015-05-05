package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.model.FishState;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

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
    public ScenarioResult start(FishState model);



    //todo move this to the GUI side of things
    public static LinkedHashMap<Class<? extends Scenario>,Supplier<Scenario>> constructors = new LinkedHashMap<>();

    public static LinkedHashMap<Class<? extends Scenario>,String> name = new LinkedHashMap<>();

    public static LinkedHashMap<Class<? extends Scenario>,String> description = new LinkedHashMap<>();

}
