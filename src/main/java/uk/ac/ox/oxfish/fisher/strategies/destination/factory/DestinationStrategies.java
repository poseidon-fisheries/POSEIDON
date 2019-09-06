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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.ReplicatorDrivenDestinationStrategy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * The collection of all the destination strategies factories.
 * Created by carrknight on 5/27/15.
 */
public class DestinationStrategies
{



    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String,Supplier<AlgorithmFactory<? extends DestinationStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();

    static{
        CONSTRUCTORS.put("Random Favorite",
                         RandomFavoriteDestinationFactory::new
                         );
        NAMES.put(RandomFavoriteDestinationFactory.class,"Random Favorite");
        CONSTRUCTORS.put("Fixed Favorite",
                         FixedFavoriteDestinationFactory::new);
        NAMES.put(FixedFavoriteDestinationFactory.class,"Fixed Favorite");
        CONSTRUCTORS.put("Always Random",
                         RandomThenBackToPortFactory::new);
        NAMES.put(RandomThenBackToPortFactory.class,"Always Random");
        CONSTRUCTORS.put("Yearly HillClimber",
                         YearlyIterativeDestinationFactory::new);
        NAMES.put(YearlyIterativeDestinationFactory.class,"Yearly HillClimber");
        CONSTRUCTORS.put("Per Trip Iterative",
                         PerTripIterativeDestinationFactory::new);
        NAMES.put(PerTripIterativeDestinationFactory.class,"Per Trip Iterative");

        CONSTRUCTORS.put("Imitator-Explorator",
                         PerTripImitativeDestinationFactory::new);
        NAMES.put(PerTripImitativeDestinationFactory.class,"Imitator-Explorator");

        CONSTRUCTORS.put("Imitator-Explorator with Head Start",
                         PerTripImitativeWithHeadStartFactory::new);
        NAMES.put(PerTripImitativeWithHeadStartFactory.class,"Imitator-Explorator with Head Start");

        CONSTRUCTORS.put("PSO",
                         PerTripParticleSwarmFactory::new);
        NAMES.put(PerTripParticleSwarmFactory.class,"PSO");

        CONSTRUCTORS.put("Threshold Erotetic",
                         ThresholdEroteticDestinationFactory::new);
        NAMES.put(ThresholdEroteticDestinationFactory.class,
                  "Threshold Erotetic");

        CONSTRUCTORS.put("Better Than Average Erotetic",
                         BetterThanAverageEroteticDestinationFactory::new);
        NAMES.put(BetterThanAverageEroteticDestinationFactory.class,
                  "Better Than Average Erotetic");

        CONSTRUCTORS.put("SNALSAR",
                         SNALSARDestinationFactory::new);
        NAMES.put(SNALSARDestinationFactory.class,
                  "SNALSAR");

        CONSTRUCTORS.put("Heatmap Based",
                         HeatmapDestinationFactory::new);
        NAMES.put(HeatmapDestinationFactory.class,
                  "Heatmap Based");

        CONSTRUCTORS.put("Heatmap Planning",
                         PlanningHeatmapDestinationFactory::new);
        NAMES.put(PlanningHeatmapDestinationFactory.class,
                  "Heatmap Planning");

        CONSTRUCTORS.put("GSA",
                         GravitationalSearchDestinationFactory::new);
        NAMES.put(GravitationalSearchDestinationFactory.class,
                  "GSA");

        CONSTRUCTORS.put("Unified Amateurish Dynamic Programming",
                         UnifiedAmateurishDynamicFactory::getInstance);
        NAMES.put(UnifiedAmateurishDynamicFactory.class,
                  "Unified Amateurish Dynamic Programming");

        CONSTRUCTORS.put("Discretized Bandit",
                         BanditDestinationFactory::new);
        NAMES.put(BanditDestinationFactory.class,
                  "Discretized Bandit");

        CONSTRUCTORS.put("Florida Longliner",
                         FloridaLogitDestinationFactory::new);
        NAMES.put(FloridaLogitDestinationFactory.class,
                  "Florida Longliner");


        CONSTRUCTORS.put("Boolean Bare Bones",
                         BarebonesFloridaDestinationFactory::new);
        NAMES.put(BarebonesFloridaDestinationFactory.class,
                  "Boolean Bare Bones");

        CONSTRUCTORS.put("Continuous Bare Bones",
                         BarebonesContinuousDestinationFactory::new);
        NAMES.put(BarebonesContinuousDestinationFactory.class,
                  "Continuous Bare Bones");

        CONSTRUCTORS.put("Continuous Bare Bones With Intercepts",
                         BarebonesContinuousInterceptedDestinationFactory::new);
        NAMES.put(BarebonesContinuousInterceptedDestinationFactory.class,
                  "Continuous Bare Bones With Intercepts");


        CONSTRUCTORS.put("Clamped to Data",
                         ClampedDestinationFactory::new);
        NAMES.put(ClampedDestinationFactory.class,
                  "Clamped to Data");


        CONSTRUCTORS.put("Perfect RPUE Logit",
                         LogitRPUEDestinationFactory::new);
        NAMES.put(LogitRPUEDestinationFactory.class,
                  "Perfect RPUE Logit");

        CONSTRUCTORS.put("Replicator",
                         ReplicatorDestinationFactory::new);
        NAMES.put(ReplicatorDestinationFactory.class,
                  "Replicator");

        CONSTRUCTORS.put("Generalized Cognitive Strategy",
        				GeneralizedCognitiveStrategyFactory::new);
        NAMES.put(GeneralizedCognitiveStrategyFactory.class,
        		"Generalized Cognitive Strategy");
    }

    private DestinationStrategies() {}

}
