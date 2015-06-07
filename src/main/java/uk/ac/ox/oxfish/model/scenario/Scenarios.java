package uk.ac.ox.oxfish.model.scenario;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * contains the scenario masterlist
 * Created by carrknight on 6/7/15.
 */
public class Scenarios {


    /**
     * list of all the scenarios. Useful for instantiating them
     */
    final public static LinkedHashMap<String, Scenario> SCENARIOS = new LinkedHashMap<>();

    /**
     * A quick description of each scenario available.
     */
    final public static LinkedHashMap<String, String> DESCRIPTIONS = new LinkedHashMap<>();

    static
    {
        SCENARIOS.put("Prototype",new PrototypeScenario());
        DESCRIPTIONS.put("Prototype", "A randomly generated west-coast with exponentially more fish the deeper the sea");
        SCENARIOS.put("Genetic Algorithm Prototype", new GeneticLocationScenario());
        DESCRIPTIONS.put("Genetic Algorithm Prototype",
                         "The Prototype scenario where agents choose the location they fish through a genetic algorithm");
        SCENARIOS.put("California Map Scenario", new CaliforniaBathymetryScenario(1));
        DESCRIPTIONS.put("California Map Scenario", "An agent-less scenario that simulates the coast of California from bathymetry data");
    }
}
