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
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The collection of all the destination strategies factories.
 * Created by carrknight on 5/27/15.
 */
public class DestinationStrategies {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    public static final Map<String, Supplier<AlgorithmFactory<? extends DestinationStrategy>>> CONSTRUCTORS;
    static {
        NAMES.put(RandomFavoriteDestinationFactory.class, "Random Favorite");
        NAMES.put(FixedFavoriteDestinationFactory.class, "Fixed Favorite");
        NAMES.put(RandomThenBackToPortFactory.class, "Always Random");
        NAMES.put(YearlyIterativeDestinationFactory.class, "Yearly HillClimber");
        NAMES.put(PerTripIterativeDestinationFactory.class, "Per Trip Iterative");
        NAMES.put(PerTripImitativeDestinationFactory.class, "Imitator-Explorator");
        NAMES.put(PerTripImitativeWithHeadStartFactory.class, "Imitator-Explorator with Head Start");
        NAMES.put(PerTripParticleSwarmFactory.class, "PSO");
        NAMES.put(ThresholdEroteticDestinationFactory.class, "Threshold Erotetic");
        NAMES.put(BetterThanAverageEroteticDestinationFactory.class, "Better Than Average Erotetic");
        NAMES.put(SNALSARDestinationFactory.class, "SNALSAR");
        NAMES.put(HeatmapDestinationFactory.class, "Heatmap Based");
        NAMES.put(PlanningHeatmapDestinationFactory.class, "Heatmap Planning");
        NAMES.put(GravitationalSearchDestinationFactory.class, "GSA");
        NAMES.put(UnifiedAmateurishDynamicFactory.class, "Unified Amateurish Dynamic Programming");
        NAMES.put(BanditDestinationFactory.class, "Discretized Bandit");
        NAMES.put(SimpleRUMDestinationFactory.class, "Simple Random Utility Model");
        NAMES.put(FloridaLogitDestinationFactory.class, "Florida Longliner");
        NAMES.put(BarebonesFloridaDestinationFactory.class, "Boolean Bare Bones");
        NAMES.put(BarebonesContinuousDestinationFactory.class, "Continuous Bare Bones");
        NAMES.put(BarebonesContinuousInterceptedDestinationFactory.class, "Continuous Bare Bones With Intercepts");
        NAMES.put(ClampedDestinationFactory.class, "Clamped to Data");
        NAMES.put(LogitRPUEDestinationFactory.class, "Perfect RPUE Logit");
        NAMES.put(PerfectDestinationFactory.class, "Perfect Knowledge");
        NAMES.put(ReplicatorDestinationFactory.class, "Replicator");
        NAMES.put(FadDestinationStrategyFactory.class, "FAD Destination Strategy");
        NAMES.put(FadGravityDestinationFactory.class, "FAD Gravity Destination Strategy");
        NAMES.put(GeneralizedCognitiveStrategyFactory.class, "Generalized Cognitive Strategy");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private DestinationStrategies() {};

}
