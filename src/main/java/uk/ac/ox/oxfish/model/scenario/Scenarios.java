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

package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

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
    final public static BiMap<String, Supplier<Scenario>> SCENARIOS = HashBiMap.create(4);

    /**
     * A quick description of each scenario available.
     */
    final public static LinkedHashMap<String, String> DESCRIPTIONS = new LinkedHashMap<>();

    static
    {
        SCENARIOS.put("Abstract",PrototypeScenario::new);
        DESCRIPTIONS.put("Abstract", "The current model, modular and ready to use.");

        SCENARIOS.put("Flexible",FlexibleScenario::new);
        DESCRIPTIONS.put("Flexible", "The conceptual model, with multiple populations");


        SCENARIOS.put("California Map Scenario", CaliforniaAbundanceScenario::new);
        DESCRIPTIONS.put("California Map Scenario", "A simple test on how well does the model read and construct a world" +
                "from bathymetry data");


        SCENARIOS.put("Abstract 2 Populations",TwoPopulationsScenario::new);
        DESCRIPTIONS.put("Abstract 2 Populations", "The current model, modular and using two populations");


        SCENARIOS.put("OSMOSE WFS",OsmoseWFSScenario::new);
        DESCRIPTIONS.put("OSMOSE WFS", "A pre-set OSMOSE scenario to simulate the west florida shelf");

//
//        SCENARIOS.put("Policy California",SimpleCaliforniaScenario::new);
//        DESCRIPTIONS.put("Policy California","California Scenario for the masses!");
//
        SCENARIOS.put("Simple California",DerisoCaliforniaScenario::new);
        DESCRIPTIONS.put("Simple California","California Scenario with DS biology");


        SCENARIOS.put("Indonesia",IndonesiaScenario::new);
        DESCRIPTIONS.put("Indonesia","Minimum Working Model of Indonesia");
    }
}
