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

import com.google.common.collect.ImmutableMap;
import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.biology.boxcars.*;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.complicated.factory.*;
import uk.ac.ox.oxfish.biology.growers.CommonLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.growers.FadAwareLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializers;
import uk.ac.ox.oxfish.biology.initializer.allocator.*;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.WeatherInitializers;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.Gears;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.*;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory.*;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory.ExhaustiveAcquisitionFunctionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory.HillClimberAcquisitionFunctionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.*;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.log.initializers.LogbookInitializer;
import uk.ac.ox.oxfish.fisher.log.initializers.NoLogbookFactory;
import uk.ac.ox.oxfish.fisher.log.initializers.TowAndAltitudeFactory;
import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.TimeScalarFunction;
import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.factory.ExponentialTimeScalarFactory;
import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.factory.InverseTimeScalarFactory;
import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.factory.SigmoidalTimeScalarFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.PlanningModule;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.factories.*;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFilters;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFromFileFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.fisher.selfanalysis.factory.ObjectiveFunctions;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.DestinationStrategies;
import uk.ac.ox.oxfish.fisher.strategies.discarding.*;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.GearStrategies;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.WeatherStrategies;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.discretization.IdentityDiscretizerFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializers;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializers;
import uk.ac.ox.oxfish.geography.ports.*;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.data.Averager;
import uk.ac.ox.oxfish.model.data.factory.ExponentialMovingAverageFactory;
import uk.ac.ox.oxfish.model.data.factory.IterativeAverageFactory;
import uk.ac.ox.oxfish.model.data.factory.MovingAverageFactory;
import uk.ac.ox.oxfish.model.event.AbundanceDrivenGearExogenousCatchesFactory;
import uk.ac.ox.oxfish.model.event.ExogenousCatches;
import uk.ac.ox.oxfish.model.event.ExogenousInstantaneousMortalityCatchesFactory;
import uk.ac.ox.oxfish.model.event.SimpleExogenousCatchesFactory;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.factory.Markets;
import uk.ac.ox.oxfish.model.market.gas.CsvTimeSeriesGasFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasFactory;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;
import uk.ac.ox.oxfish.model.network.NetworkBuilders;
import uk.ac.ox.oxfish.model.network.NetworkPredicate;
import uk.ac.ox.oxfish.model.network.factory.MustShareTag;
import uk.ac.ox.oxfish.model.network.factory.SamePortEdgesOnly;
import uk.ac.ox.oxfish.model.plugins.AdditionalStartables;
import uk.ac.ox.oxfish.model.regs.ExogenousPercentagePermitFactory;
import uk.ac.ox.oxfish.model.regs.PermitAllocationPolicy;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AllowAllAllocationPolicyFactory;
import uk.ac.ox.oxfish.model.regs.factory.MaxHoldSizeRandomAllocationPolicyFactory;
import uk.ac.ox.oxfish.model.regs.factory.Regulations;
import uk.ac.ox.oxfish.regulation.EverythingPermitted;
import uk.ac.ox.oxfish.regulation.ForbiddenIf;
import uk.ac.ox.oxfish.regulation.NamedRegulations;
import uk.ac.ox.oxfish.regulation.conditions.*;
import uk.ac.ox.oxfish.regulation.quantities.NumberOfActiveFads;
import uk.ac.ox.oxfish.regulation.quantities.SumOf;
import uk.ac.ox.oxfish.regulation.quantities.YearlyActionCount;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.Probabilities;
import uk.ac.ox.oxfish.utility.bandit.factory.BanditSupplier;
import uk.ac.ox.oxfish.utility.bandit.factory.EpsilonGreedyBanditFactory;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * Just a way to link a class to its constructor map Created by carrknight on 5/29/15.
 */
public class AlgorithmFactories {

    public static final Map<Class<?>, Map<String, ? extends Supplier<? extends AlgorithmFactory<?>>>>
        CONSTRUCTOR_MAP = new HashMap<>();
    private static final Map<Class<?>, Map<Class<? extends AlgorithmFactory<?>>, String>> NAMES_MAP =
        new HashMap<>();

    static {

        CONSTRUCTOR_MAP.put(AdditionalStartable.class, AdditionalStartables.CONSTRUCTORS);
        NAMES_MAP.put(AdditionalStartable.class, AdditionalStartables.NAMES);


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
        
        Stream.of(
            new Factories<>(
                ProfitThresholdExtractor.class,
                ImmutableMap.of(
                    FixedProfitThresholdFactory.class, "Fixed Threshold",
                    AverageProfitsThresholdFactory.class, "Average Profits Threshold"
                )
            ),
            new Factories<>(
                SocialAcceptabilityFeatureExtractor.class,
                ImmutableMap.of(
                    EverywhereTrueExtractorFactory.class, "Socially Acceptable Everywhere",
                    NoFriendsHereExtractorFactory.class, "No Friends Fish Here",
                    NobodyFishesHereFactory.class, "Nobody Fishes Here"
                )
            ),
            new Factories<>(
                SafetyFeatureExtractor.class,
                ImmutableMap.of(
                    EverywhereTrueExtractorFactory.class, "Safe Everywhere",
                    LessThanXFishersHereExtractorFactory.class, "Less Than X Fishers Currently Here Is Safe"
                )
            ),
            new Factories<>(
                LegalityFeatureExtractor.class,
                ImmutableMap.of(
                    EverywhereTrueExtractorFactory.class, "Ignore Rules",
                    FollowRulesExtractorFactory.class, "Follow the Rules"
                )
            ),
            new Factories<>(
                ProfitFeatureExtractor.class,
                ImmutableMap.of(
                    RememberedProfitsExtractorFactory.class, "Remembered Profits"
                )
            ),
            new Factories<>(
                GeographicalRegression.class,
                ImmutableMap.ofEntries(
                    entry(NearestNeighborRegressionFactory.class, "Nearest Neighbor"),
                    entry(CompleteNearestNeighborRegressionFactory.class, "Complete Nearest Neighbor"),
                    entry(NearestNeighborTransductionFactory.class, "Nearest Neighbor Transduction"),
                    entry(KernelTransductionFactory.class, "Kernel Transduction"),
                    entry(DefaultRBFKernelTransductionFactory.class, "RBF Kernel Transduction"),
                    entry(ParticleFilterRegressionFactory.class, "Particle Filter Regression"),
                    entry(SimpleKalmanRegressionFactory.class, "Simple Kalman"),
                    entry(GeographicallyWeightedRegressionFactory.class, "GWR"),
                    entry(GoodBadRegressionFactory.class, "Good-Bad"),
                    entry(DefaultKernelRegressionFactory.class, "Kernel Regression"),
                    entry(RBFNetworkFactory.class, "RBF Network"),
                    entry(SocialTuningRegressionFactory.class, "Social Tuning"),
                    entry(PersonalTuningRegressionFactory.class, "Personal Tuning")
                )
            ),
            new Factories<>(
                AcquisitionFunction.class,
                ImmutableMap.of(
                    ExhaustiveAcquisitionFunctionFactory.class, "Exhaustive Search",
                    HillClimberAcquisitionFunctionFactory.class, "Hill-Climber Acquisition"
                )
            ),
            new Factories<>(
                BanditSupplier.class,
                ImmutableMap.of(
                    EpsilonGreedyBanditFactory.class, "Epsilon Greedy Bandit"
                )
            ),
            new Factories<>(
                Averager.class,
                ImmutableMap.of(
                    IterativeAverageFactory.class, "Average",
                    MovingAverageFactory.class, "Moving Average",
                    ExponentialMovingAverageFactory.class, "Exponential Moving Average"
                )
            ),
            new Factories<>(
                PortInitializer.class,
                ImmutableMap.of(
                    RandomPortFactory.class, "Random Ports",
                    OnePortFactory.class, "One Port",
                    TwoPortsFactory.class, "Two Ports",
                    PortListFactory.class, "List of Ports"
                )
            ),
            new Factories<>(
                MapDiscretizer.class,
                ImmutableMap.of(
                    SquaresMapDiscretizerFactory.class, "Squared Discretization",
                    CentroidMapFileFactory.class, "Centroid File Discretization",
                    IdentityDiscretizerFactory.class, "Identity Discretization"
                )
            ),
            new Factories<>(
                LogisticGrowerInitializer.class,
                ImmutableMap.of(
                    SimpleLogisticGrowerFactory.class, "Independent Logistic Grower",
                    CommonLogisticGrowerFactory.class, "Common Logistic Grower",
                    FadAwareLogisticGrowerFactory.class, "FAD-Aware Logistic Grower"
                )
            ),
            new Factories<>(
                LogbookInitializer.class,
                ImmutableMap.of(
                    NoLogbookFactory.class, "No Logbook",
                    TowAndAltitudeFactory.class, "Tows and Altitude"
                )
            ),
            new Factories<>(
                DiscardingStrategy.class,
                ImmutableMap.of(
                    NoDiscardingFactory.class, "No Discarding",
                    DiscardingAllUnsellableFactory.class, "Discarding All Unsellable",
                    AlwaysDiscardTheseSpeciesFactory.class, "Specific Discarding",
                    DiscardUnderagedFactory.class, "Age Discarding"
                )
            ),
            new Factories<>(
                Meristics.class,
                ImmutableMap.of(
                    ListMeristicFactory.class, "Weight List Meristics",
                    SimpleListMeristicFactory.class, "Length-Weight No Sex Meristics",
                    MeristicsFileFactory.class, "Stock Assessment Meristics From File",
                    EquallySpacedBertalanffyFactory.class, "Equally Spaced Von bertalanffy"
                )
            ),
            new Factories<>(
                AbundanceDiffuser.class,
                ImmutableMap.of(
                    NoDiffuserFactory.class, "No Diffusion",
                    ConstantRateDiffuserFactory.class, "Constant Rate Diffusion",
                    AgeLimitedConstantRateDiffuserFactory.class, "Bin-Restricted Diffusion"
                )
            ),
            new Factories<>(
                AgingProcess.class,
                ImmutableMap.of(
                    StandardAgingFactory.class, "Yearly Aging",
                    ProportionalAgingFactory.class, "Proportional Aging",
                    FixedBoxcarBertalannfyAging.class, "Fixed Boxcar VB Aging",
                    SullivanAgingFactory.class, "Sullivan Matrix Aging"
                )
            ),
            new Factories<>(
                InitialAbundance.class,
                ImmutableMap.of(
                    InitialAbundanceFromFileFactory.class, "Abundance From File",
                    InitialAbundanceFromStringFactory.class, "Abundance From String",
                    InitialAbundanceFromListFactory.class, "Abundance From List",
                    OneBinAbundanceFactory.class, "Abundance in one bin"
                )
            ),
            new Factories<>(
                RecruitmentProcess.class,
                ImmutableMap.of(
                    LogisticRecruitmentFactory.class, "Logistic Recruitment",
                    RecruitmentBySpawningFactory.class, "Beverton-Holt",
                    SimplifiedBevertonHoltRecruitmentFactory.class, "Beverton-Holt Simplified",
                    RecruitmentBySpawningJackKnifeMaturity.class, "Beverton-Holt Knife-Edge Maturity",
                    FixedRecruitmentFactory.class, "Fixed Recruitment",
                    LinearSSBRatioSpawningFactory.class, "Linear SSB Recruitment",
                    HockeyStickRecruitmentFactory.class, "Hockey Stick Recruitment"
                )
            ),
            new Factories<>(
                BiomassAllocator.class,
                ImmutableMap.ofEntries(
                    entry(ConstantAllocatorFactory.class, "Equal Allocation"),
                    entry(BoundedAllocatorFactory.class, "Bounded Allocation"),
                    entry(FromLeftToRightAllocatorFactory.class, "From Left to Right Allocation"),
                    entry(DepthAllocatorFactory.class, "Depth Allocator"),
                    entry(RandomAllocatorFactory.class, "Random Allocator"),
                    entry(RandomSmoothedFactory.class, "Random Smoothed Allocator"),
                    entry(KernelizedRandomFactory.class, "Random Kernel Allocator"),
                    entry(SimplexFactory.class, "Simplex Allocator"),
                    entry(PyramidsAllocatorFactory.class, "Pyramids Allocator"),
                    entry(SinglePeakAllocatorFactory.class, "Single Peak Pyramid Allocator"),
                    entry(MirroredPyramidsAllocatorFactory.class, "Mirrored Peak Pyramid Allocator"),
                    entry(CoordinateFileAllocatorFactory.class, "From File Allocator"),
                    entry(SmootherFileAllocatorFactory.class, "From File Smoothed Allocator"),
                    entry(PolygonAllocatorFactory.class, "Shape File Allocator")
                )
            ),
            new Factories<>(
                NaturalMortalityProcess.class,
                ImmutableMap.of(
                    ProportionalMortalityFactory.class, "Proportional Mortality",
                    ExponentialMortalityFactory.class, "Exponential Mortality"
                )
            ),
            new Factories<>(
                GasPriceMaker.class,
                ImmutableMap.of(
                    FixedGasFactory.class, "Fixed Gas Price",
                    CsvTimeSeriesGasFactory.class, "Gas Price from File"
                )
            ),
            new Factories<>(
                ExogenousCatches.class,
                ImmutableMap.of(
                    SimpleExogenousCatchesFactory.class, "Simple Exogenous Catches",
                    AbundanceDrivenGearExogenousCatchesFactory.class, "Abundance Gear Exogenous Catches",
                    ExogenousInstantaneousMortalityCatchesFactory.class, "Instantaneous Mortality Exogenous Catches"
                )
            ),
            new Factories<>(
                Condition.class,
                ActionCodeIs.class,
                AgentHasTag.class,
                AllOf.class,
                AnyOf.class,
                Below.class,
                BetweenYearlyDates.class,
                InRectangularArea.class,
                Not.class
            ),
            new Factories<>(
                uk.ac.ox.poseidon.regulations.api.Regulation.class,
                NamedRegulations.class,
                EverythingPermitted.class,
                ForbiddenIf.class
            ),
            new Factories<>(
                Quantity.class,
                ImmutableMap.of(
                    NumberOfActiveFads.class, "Number of active FADs"
                ),
                SumOf.class,
                YearlyActionCount.class
            ),
            new Factories<>(
                AbundanceFilters.class,
                AbundanceFiltersFromFileFactory.class
            ),
            new Factories<>(
                NetworkPredicate.class,
                ImmutableMap.of(
                    MustShareTag.class, "Must share a tag",
                    SamePortEdgesOnly.class, "Must share port"
                )
            ),
            new Factories<>(
                PlanningModule.class,
                ImmutableMap.of(
                    DiscretizedOwnFadPlanningFactory.class, "Centroid FAD Planning",
                    GreedyInsertionFadPlanningFactory.class, "Greedy FAD Module",
                    MarginalValueFadPlanningModuleFactory.class, "MVT FAD Module",
                    ValuePerSetPlanningModuleFactory.class, "VPS FAD Module",
                    WhereFadsAreFadModuleFactory.class, "Where Fads Are FAD Module",
                    WhereMoneyIsPlanningFactory.class, "Where Money Is FAD Module"
                )
            ),
            new Factories<>(
                PermitAllocationPolicy.class,
                ImmutableMap.of(
                    AllowAllAllocationPolicyFactory.class, "No effort limit",
                    MaxHoldSizeRandomAllocationPolicyFactory.class, "Max hold size limit",
                    ExogenousPercentagePermitFactory.class, "Yearly percentage of boats"
                )
            ),
            new Factories<>(
                TimeScalarFunction.class,
                ImmutableMap.of(
                    InverseTimeScalarFactory.class, "Inverse",
                    ExponentialTimeScalarFactory.class, "Exponential",
                    SigmoidalTimeScalarFactory.class, "Sigmoidal"
                )
            ),
            new Factories<>(
                SPRAgent.class,
                ImmutableMap.of(
                    SPRAgentBuilder.class, "SPR Agent",
                    SPRAgentBuilderSelectiveSampling.class, "SPR Selective Agent",
                    SPRAgentBuilderFixedSample.class, "SPR Fixed Sample Agent"
                )
            )
        ).forEach(factories -> {
            CONSTRUCTOR_MAP.put(factories.getClassObject(), factories.getConstructors());
            NAMES_MAP.put(factories.getClassObject(), factories.getNames());
        });
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
