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

package uk.ac.ox.oxfish.utility;

import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgent;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.complicated.factory.*;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowers;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializers;
import uk.ac.ox.oxfish.biology.initializer.allocator.Allocators;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.WeatherInitializers;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.Gears;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.*;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory.*;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory.AcquisitionFunctions;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.NumericalGeographicalRegressions;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.log.initializers.LogbookInitializer;
import uk.ac.ox.oxfish.fisher.log.initializers.LogbookInitializers;
import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.TimeScalarFunction;
import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.factory.TimeScalarFunctions;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.DiscretizedOwnFadPlanningModule;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.factories.FadPlanningFactories;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFilterFactories;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.fisher.selfanalysis.factory.ObjectiveFunctions;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.DestinationStrategies;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.GearStrategies;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.WeatherStrategies;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizers;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializers;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializers;
import uk.ac.ox.oxfish.geography.ports.PortInitializer;
import uk.ac.ox.oxfish.geography.ports.PortInitializers;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.data.Averager;
import uk.ac.ox.oxfish.model.data.factory.Averages;
import uk.ac.ox.oxfish.model.event.ExogenousCatchFactories;
import uk.ac.ox.oxfish.model.event.ExogenousCatches;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.factory.Markets;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMakers;
import uk.ac.ox.oxfish.model.network.NetworkBuilders;
import uk.ac.ox.oxfish.model.network.NetworkPredicate;
import uk.ac.ox.oxfish.model.network.factory.NetworkPredicates;
import uk.ac.ox.oxfish.model.plugins.AdditionalStartables;
import uk.ac.ox.oxfish.model.plugins.CatchAtLengthFactories;
import uk.ac.ox.oxfish.model.regs.PermitAllocationPolicy;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.PermitAllocationPolicies;
import uk.ac.ox.oxfish.model.regs.factory.Regulations;
import uk.ac.ox.oxfish.regulation.RegulationFactories;
import uk.ac.ox.oxfish.regulation.conditions.ConditionFactories;
import uk.ac.ox.oxfish.regulation.quantities.QuantityFactories;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.Probabilities;
import uk.ac.ox.oxfish.utility.bandit.factory.BanditAlgorithms;
import uk.ac.ox.oxfish.utility.bandit.factory.BanditSupplier;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Just a way to link a class to its constructor map Created by carrknight on 5/29/15.
 */
public class AlgorithmFactories {


    //notice the <? extends AlgorithmFactory>. It's the need for hacks like these that explains why so many engineers
    //join terrorist organizations
    public static final Map<Class<?>, Map<String, ? extends Supplier<? extends AlgorithmFactory<?>>>>
        CONSTRUCTOR_MAP = new HashMap<>();
    private static final Map<Class<?>, Map<Class<? extends AlgorithmFactory<?>>, String>> NAMES_MAP =
        new HashMap<>();

    static {
        CONSTRUCTOR_MAP.put(DepartingStrategy.class, DepartingStrategies.CONSTRUCTORS);
        NAMES_MAP.put(DepartingStrategy.class, DepartingStrategies.NAMES);
        CONSTRUCTOR_MAP.put(DestinationStrategy.class, DestinationStrategies.CONSTRUCTORS);
        NAMES_MAP.put(DestinationStrategy.class, DestinationStrategies.NAMES);
        CONSTRUCTOR_MAP.put(FishingStrategy.class, FishingStrategies.CONSTRUCTORS);
        NAMES_MAP.put(FishingStrategy.class, FishingStrategies.NAMES);
        CONSTRUCTOR_MAP.put(Regulation.class, Regulations.CONSTRUCTORS);
        NAMES_MAP.put(Regulation.class, Regulations.NAMES);
        CONSTRUCTOR_MAP.put(BiologyInitializer.class, BiologyInitializers.CONSTRUCTORS);
        NAMES_MAP.put(BiologyInitializer.class, BiologyInitializers.NAMES);
        CONSTRUCTOR_MAP.put(DirectedGraph.class, NetworkBuilders.CONSTRUCTORS);
        NAMES_MAP.put(DirectedGraph.class, NetworkBuilders.NAMES);
        CONSTRUCTOR_MAP.put(Market.class, Markets.CONSTRUCTORS);
        NAMES_MAP.put(Market.class, Markets.NAMES);
        CONSTRUCTOR_MAP.put(AdaptationProbability.class, Probabilities.CONSTRUCTORS);
        NAMES_MAP.put(AdaptationProbability.class, Probabilities.NAMES);
        CONSTRUCTOR_MAP.put(WeatherInitializer.class, WeatherInitializers.CONSTRUCTORS);
        NAMES_MAP.put(WeatherInitializer.class, WeatherInitializers.NAMES);
        CONSTRUCTOR_MAP.put(WeatherEmergencyStrategy.class, WeatherStrategies.CONSTRUCTORS);
        NAMES_MAP.put(WeatherEmergencyStrategy.class, WeatherStrategies.NAMES);
        CONSTRUCTOR_MAP.put(HabitatInitializer.class, HabitatInitializers.CONSTRUCTORS);
        NAMES_MAP.put(HabitatInitializer.class, HabitatInitializers.NAMES);
        CONSTRUCTOR_MAP.put(Gear.class, Gears.CONSTRUCTORS);
        NAMES_MAP.put(Gear.class, Gears.NAMES);
        CONSTRUCTOR_MAP.put(MapInitializer.class, MapInitializers.CONSTRUCTORS);
        NAMES_MAP.put(MapInitializer.class, MapInitializers.NAMES);
        CONSTRUCTOR_MAP.put(ObjectiveFunction.class, ObjectiveFunctions.CONSTRUCTORS);
        NAMES_MAP.put(ObjectiveFunction.class, ObjectiveFunctions.NAMES);
        CONSTRUCTOR_MAP.put(GearStrategy.class, GearStrategies.CONSTRUCTORS);
        NAMES_MAP.put(GearStrategy.class, GearStrategies.NAMES);
        CONSTRUCTOR_MAP.put(
            ProfitThresholdExtractor.class,
            ProfitThresholdsExtractors.CONSTRUCTORS
        );
        NAMES_MAP.put(ProfitThresholdExtractor.class, ProfitThresholdsExtractors.NAMES);
        CONSTRUCTOR_MAP.put(
            SocialAcceptabilityFeatureExtractor.class,
            SocialAcceptabilityFeatureExtractors.CONSTRUCTORS
        );
        NAMES_MAP.put(
            SocialAcceptabilityFeatureExtractor.class,
            SocialAcceptabilityFeatureExtractors.NAMES
        );
        CONSTRUCTOR_MAP.put(SafetyFeatureExtractor.class, SafetyFeatureExtractors.CONSTRUCTORS);
        NAMES_MAP.put(SafetyFeatureExtractor.class, SafetyFeatureExtractors.NAMES);
        CONSTRUCTOR_MAP.put(LegalityFeatureExtractor.class, LegalityFeatureExtractors.CONSTRUCTORS);
        NAMES_MAP.put(LegalityFeatureExtractor.class, LegalityFeatureExtractors.NAMES);
        CONSTRUCTOR_MAP.put(ProfitFeatureExtractor.class, ProfitFeatureExtractors.CONSTRUCTORS);
        NAMES_MAP.put(ProfitFeatureExtractor.class, ProfitFeatureExtractors.NAMES);
        CONSTRUCTOR_MAP.put(
            GeographicalRegression.class,
            NumericalGeographicalRegressions.CONSTRUCTORS
        );
        NAMES_MAP.put(GeographicalRegression.class, NumericalGeographicalRegressions.NAMES);
        CONSTRUCTOR_MAP.put(AcquisitionFunction.class, AcquisitionFunctions.CONSTRUCTORS);
        NAMES_MAP.put(AcquisitionFunction.class, AcquisitionFunctions.NAMES);
        CONSTRUCTOR_MAP.put(BanditSupplier.class, BanditAlgorithms.CONSTRUCTORS);
        NAMES_MAP.put(BanditSupplier.class, BanditAlgorithms.NAMES);
        CONSTRUCTOR_MAP.put(Averager.class, Averages.CONSTRUCTORS);
        NAMES_MAP.put(Averager.class, Averages.NAMES);
        CONSTRUCTOR_MAP.put(PortInitializer.class, PortInitializers.CONSTRUCTORS);
        NAMES_MAP.put(PortInitializer.class, PortInitializers.NAMES);
        CONSTRUCTOR_MAP.put(MapDiscretizer.class, MapDiscretizers.CONSTRUCTORS);
        NAMES_MAP.put(MapDiscretizer.class, MapDiscretizers.NAMES);
        CONSTRUCTOR_MAP.put(LogisticGrowerInitializer.class, LogisticGrowers.CONSTRUCTORS);
        NAMES_MAP.put(LogisticGrowerInitializer.class, LogisticGrowers.NAMES);
        CONSTRUCTOR_MAP.put(LogbookInitializer.class, LogbookInitializers.CONSTRUCTORS);
        NAMES_MAP.put(LogbookInitializer.class, LogbookInitializers.NAMES);
        CONSTRUCTOR_MAP.put(DiscardingStrategy.class, DiscardingStrategies.CONSTRUCTORS);
        NAMES_MAP.put(DiscardingStrategy.class, DiscardingStrategies.NAMES);

        CONSTRUCTOR_MAP.put(Meristics.class, MeristicFactories.CONSTRUCTORS);
        NAMES_MAP.put(Meristics.class, MeristicFactories.NAMES);
        CONSTRUCTOR_MAP.put(AbundanceDiffuser.class, AbundanceDiffusers.CONSTRUCTORS);
        NAMES_MAP.put(AbundanceDiffuser.class, AbundanceDiffusers.NAMES);
        CONSTRUCTOR_MAP.put(AgingProcess.class, Agings.CONSTRUCTORS);
        NAMES_MAP.put(AgingProcess.class, Agings.NAMES);
        CONSTRUCTOR_MAP.put(InitialAbundance.class, InitialAbundances.CONSTRUCTORS);
        NAMES_MAP.put(InitialAbundance.class, InitialAbundances.NAMES);
        CONSTRUCTOR_MAP.put(RecruitmentProcess.class, Recruitments.CONSTRUCTORS);
        NAMES_MAP.put(RecruitmentProcess.class, Recruitments.NAMES);
        CONSTRUCTOR_MAP.put(BiomassAllocator.class, Allocators.CONSTRUCTORS);
        NAMES_MAP.put(BiomassAllocator.class, Allocators.NAMES);
        CONSTRUCTOR_MAP.put(NaturalMortalityProcess.class, Mortalities.CONSTRUCTORS);
        NAMES_MAP.put(NaturalMortalityProcess.class, Mortalities.NAMES);
        CONSTRUCTOR_MAP.put(GasPriceMaker.class, GasPriceMakers.CONSTRUCTORS);
        NAMES_MAP.put(GasPriceMaker.class, GasPriceMakers.NAMES);
        CONSTRUCTOR_MAP.put(ExogenousCatches.class, ExogenousCatchFactories.CONSTRUCTORS);
        NAMES_MAP.put(ExogenousCatches.class, ExogenousCatchFactories.NAMES);

        CONSTRUCTOR_MAP.put(AdditionalStartable.class, AdditionalStartables.CONSTRUCTORS);
        NAMES_MAP.put(AdditionalStartable.class, AdditionalStartables.NAMES);
        CONSTRUCTOR_MAP.put(SPRAgent.class, CatchAtLengthFactories.CONSTRUCTORS);
        NAMES_MAP.put(SPRAgent.class, CatchAtLengthFactories.NAMES);
        CONSTRUCTOR_MAP.put(TimeScalarFunction.class, TimeScalarFunctions.CONSTRUCTORS);
        NAMES_MAP.put(TimeScalarFunction.class, TimeScalarFunctions.NAMES);
        CONSTRUCTOR_MAP.put(PermitAllocationPolicy.class, PermitAllocationPolicies.CONSTRUCTORS);
        NAMES_MAP.put(PermitAllocationPolicy.class, PermitAllocationPolicies.NAMES);

        CONSTRUCTOR_MAP.put(NetworkPredicate.class, NetworkPredicates.CONSTRUCTORS);
        NAMES_MAP.put(NetworkPredicate.class, NetworkPredicates.NAMES);

        CONSTRUCTOR_MAP.put(Condition.class, ConditionFactories.CONSTRUCTORS);
        NAMES_MAP.put(Condition.class, ConditionFactories.NAMES);

        CONSTRUCTOR_MAP.put(uk.ac.ox.poseidon.regulations.api.Regulation.class, RegulationFactories.CONSTRUCTORS);
        NAMES_MAP.put(uk.ac.ox.poseidon.regulations.api.Regulation.class, RegulationFactories.NAMES);

        CONSTRUCTOR_MAP.put(uk.ac.ox.poseidon.regulations.api.Quantity.class, QuantityFactories.CONSTRUCTORS);
        NAMES_MAP.put(uk.ac.ox.poseidon.regulations.api.Quantity.class, QuantityFactories.NAMES);

        CONSTRUCTOR_MAP.put(
            DiscretizedOwnFadPlanningModule.class,
            FadPlanningFactories.CONSTRUCTORS
        );
        NAMES_MAP.put(
            DiscretizedOwnFadPlanningModule.class,
            FadPlanningFactories.NAMES
        );

        CONSTRUCTOR_MAP.put(AbundanceFilterFactories.class, AbundanceFilterFactories.CONSTRUCTORS);
        NAMES_MAP.put(AbundanceFilterFactories.class, AbundanceFilterFactories.NAMES);
    }


    /**
     * look up for any algorithm factory with a specific name, returning the first it finds
     *
     * @param name the name
     * @return the factory; or throws an exception if there isn't any!
     */
    public static AlgorithmFactory<?> constructorLookup(final String name) {
        return getFirstValueForKey(CONSTRUCTOR_MAP, name)
            .orElseThrow(() ->
                new RuntimeException("failed to find constructor named: " + name)
            )
            .get();
    }

    private static <V> Optional<? extends V> getFirstValueForKey(
        final Map<?, ? extends Map<?, ? extends V>> mapOfMaps,
        final Object key
    ) {
        return mapOfMaps
            .values()
            .stream()
            .filter(map -> map.containsKey(key))
            .map(map -> map.get(key))
            .findFirst();
    }

    /**
     * look up the name of the algorithm factory that has this class
     *
     * @param factoryClass the class for which to find the name
     * @return the factory or throws an exception if there isn't any!
     */
    public static String nameLookup(final Class<?> factoryClass) {
        return getFirstValueForKey(NAMES_MAP, factoryClass)
            .orElseThrow(() ->
                new RuntimeException("failed to find factory name for class " + factoryClass)
            );
    }

    /**
     * returns a stream with all the factories available in the constructor Maps
     */
    public static Stream<Class<? extends AlgorithmFactory<?>>> getAllAlgorithmFactories() {
        return NAMES_MAP
            .entrySet()
            .stream()
            .flatMap(entry -> entry.getValue().keySet().stream());
    }
}
