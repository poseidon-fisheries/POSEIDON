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
        SCENARIOS.put("Abstract",new PrototypeScenario());
        DESCRIPTIONS.put("Abstract", "The current model, modular and ready to use.");


        SCENARIOS.put("California Map Scenario", new CaliforniaAbundanceScenario());
        DESCRIPTIONS.put("California Map Scenario", "A simple test on how well does the model read and construct a world" +
                "from bathymetry data");


        SCENARIOS.put("Abstract 2 Populations",new TwoPopulationsScenario());
        DESCRIPTIONS.put("Abstract 2 Populations", "The current model, modular and using two populations");


        SCENARIOS.put("OSMOSE WFS",new OsmoseWFSScenario());
        DESCRIPTIONS.put("OSMOSE WFS", "A pre-set OSMOSE scenario to simulate the west florida shelf");


        SCENARIOS.put("Policy California",new SimpleCaliforniaScenario());
        DESCRIPTIONS.put("Policy California","California Scenario for the masses!");

   SCENARIOS.put("Simple California",new DerisoCaliforniaScenario());
        DESCRIPTIONS.put("Simple California","California Scenario with DS biology");


    }
}
