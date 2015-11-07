package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.LinkedHashMap;

/**
 * contains the scenario masterlist
 * Created by carrknight on 6/7/15.
 */
public class Scenarios {


    /**
     * list of all the scenarios. Useful for instantiating them
     */
    final public static BiMap<String, Scenario> SCENARIOS = HashBiMap.create(4);

    /**
     * A quick description of each scenario available.
     */
    final public static LinkedHashMap<String, String> DESCRIPTIONS = new LinkedHashMap<>();

    static
    {
        SCENARIOS.put("Prototype",new PrototypeScenario());
        DESCRIPTIONS.put("Prototype", "The current model, modular and ready to use.");



        SCENARIOS.put("Genetic Algorithm Prototype", new GeneticLocationScenario());
        DESCRIPTIONS.put("Genetic Algorithm Prototype",
                         "An abandoned prototype I used to test how applicable genetic algorithms are to simulate decisions");
        SCENARIOS.put("California Map Scenario", new CaliforniaBathymetryScenario(1));
        DESCRIPTIONS.put("California Map Scenario", "A simple test on how well does the model read and construct a world" +
                "from bathymetry data");
    }
}
