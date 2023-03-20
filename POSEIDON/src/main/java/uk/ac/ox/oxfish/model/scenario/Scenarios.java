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

    static {

        add(
            "Abstract",
            "The current model, modular and ready to use.",
            PrototypeScenario::new
        );
        add(
            "Flexible",
            "The conceptual model, with multiple populations",
            FlexibleScenario::new
        );
        add(
            "Generalized",
            "Extension of the Abstract Scenario, implementing communal restrictions",
            GeneralizedScenario::new
        );
        add(
            "California Map Scenario",
            "A simple test on how well does the model read and construct a world from bathymetry data",
            CaliforniaAbundanceScenario::new
        );
        add(
            "Abstract 2 Populations",
            "The current model, modular and using two populations",
            TwoPopulationsScenario::new
        );
        add(
            "Simple California",
            "California Scenario with DS biology",
            DerisoCaliforniaScenario::new
        );
        add(
            "Indonesia",
            "Minimum Working Model of Indonesia",
            IndonesiaScenario::new
        );
        add(
            "EPO Biomass",
            "A biomass-based scenario for purse-seine fishing in the Eastern Pacific Ocean.",
            EpoBiomassScenario::new
        );
        add(
            "EPO Abundance",
            "An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.",
            EpoAbundanceScenario::new
        );
        add(
            "FAD only Abundance",
            "A vessel-less scenario for FAD parameter calibration purposes.",
            FadsOnlyEpoAbundanceScenario::new
        );
        add(
            "EPO Abundance Pathfinding",
            "Like EPO Abundance but using an alternative decision process",
            EpoScenarioPathfinding::new
        );
        add(
            "EPO Abundance Biology Only",
            "A biology-only scenario for testing purposes",
            EpoAbundanceScenarioBioOnly::new
        );
    }

    private static void add(
        final String name,
        final String description,
        final Supplier<Scenario> scenarioSupplier
    ) {
        SCENARIOS.put(name, scenarioSupplier);
        DESCRIPTIONS.put(name, description);
    }
}
