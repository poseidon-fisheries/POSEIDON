/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.utility;

import com.google.common.collect.ImmutableMap;
import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.biology.BiomassResetterFactory;
import uk.ac.ox.oxfish.biology.BiomassTotalResetterFactory;
import uk.ac.ox.oxfish.biology.boxcars.*;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.complicated.factory.*;
import uk.ac.ox.oxfish.biology.growers.CommonLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.growers.FadAwareLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.*;
import uk.ac.ox.oxfish.biology.initializer.factory.*;
import uk.ac.ox.oxfish.biology.tuna.BiomassInitializerFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.ConstantWeatherFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.OscillatingWeatherFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.TimeSeriesWeatherFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.*;
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
import uk.ac.ox.oxfish.fisher.purseseiner.planner.EPOPlannedStrategyFlexibleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.GenerateRandomPlansStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerAbundanceFishingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerBiomassFishingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.fisher.selfanalysis.factory.*;
import uk.ac.ox.oxfish.fisher.strategies.departing.AdaptiveProbabilityDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.*;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.*;
import uk.ac.ox.oxfish.fisher.strategies.discarding.*;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.*;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.*;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.IgnoreWeatherFactory;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.WindThresholdFactory;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.discretization.IdentityDiscretizerFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadMakerCSVFactory;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadSetterCSVFactory;
import uk.ac.ox.oxfish.geography.fads.FadDemoFactory;
import uk.ac.ox.oxfish.geography.habitat.AllSandyHabitatFactory;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.geography.habitat.RockyPyramidsFactory;
import uk.ac.ox.oxfish.geography.habitat.rectangles.OneRockyRectangleFactory;
import uk.ac.ox.oxfish.geography.habitat.rectangles.RockyRectanglesHabitatFactory;
import uk.ac.ox.oxfish.geography.mapmakers.*;
import uk.ac.ox.oxfish.geography.ports.*;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.data.Averager;
import uk.ac.ox.oxfish.model.data.collectors.AdditionalFishStateDailyCollectorsFactory;
import uk.ac.ox.oxfish.model.data.collectors.HerfindalndexCollectorFactory;
import uk.ac.ox.oxfish.model.data.collectors.TowLongLoggerFactory;
import uk.ac.ox.oxfish.model.data.factory.ExponentialMovingAverageFactory;
import uk.ac.ox.oxfish.model.data.factory.IterativeAverageFactory;
import uk.ac.ox.oxfish.model.data.factory.MovingAverageFactory;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.YearlyMarketMapFromPriceFileFactory;
import uk.ac.ox.oxfish.model.market.factory.*;
import uk.ac.ox.oxfish.model.market.gas.CsvTimeSeriesGasFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasFactory;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;
import uk.ac.ox.oxfish.model.network.BarabasiAlbertBuilder;
import uk.ac.ox.oxfish.model.network.ClubNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.plugins.*;
import uk.ac.ox.oxfish.model.regs.ConjunctiveRegulationsFactory;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.TaggedRegulationFactory;
import uk.ac.ox.oxfish.model.regs.factory.*;
import uk.ac.ox.oxfish.model.regs.policymakers.*;
import uk.ac.ox.oxfish.model.regs.policymakers.factory.ISlopeToTACControllerFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.factory.ITEControllerFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.factory.ITargetTACFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.SimpleFishSamplerFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.SurplusProductionDepletionFormulaController;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.*;
import uk.ac.ox.oxfish.utility.bandit.factory.BanditSupplier;
import uk.ac.ox.oxfish.utility.bandit.factory.EpsilonGreedyBanditFactory;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * Just a way to link a class to its constructor map Created by carrknight on 5/29/15.
 */
@SuppressWarnings({"unchecked", "RedundantSuppression"})
public class AlgorithmFactories {

    public static final Map<Class<?>, Map<String, ? extends Supplier<?
        extends AlgorithmFactory<?>>>>
        CONSTRUCTOR_MAP = new HashMap<>();

    private static final Map<Class<?>, Map<Class<? extends AlgorithmFactory<?>>, String>>
        NAMES_MAP =
        new HashMap<>();

    static {
        addFactories(new Factories<>(
            AdditionalStartable.class,
            ImmutableMap.ofEntries(
                entry(TowAndAltitudePluginFactory.class, "Tow Heatmapper"),
                entry(BiomassResetterFactory.class, "Biomass Location Resetter"),
                entry(BiomassTotalResetterFactory.class, "Biomass Total Resetter"),
                entry(SnapshotAbundanceResetterFactory.class, "Abundance Snapshot Resetter"),
                entry(SnapshotBiomassResetterFactory.class, "Biomass Snapshot Resetter"),
                entry(AbundanceGathererBuilder.class, "Abundance Gatherers"),
                entry(SPRAgentBuilder.class, "SPR Agent"),
                entry(SPRAgentBuilderSelectiveSampling.class, "SPR Selective Agent"),
                entry(SPRAgentBuilderFixedSample.class, "SPR Fixed Sample Agent"),
                entry(SprOracleBuilder.class, "SPR Oracle"),
                entry(FishingMortalityAgentFactory.class, "Fishing Mortality Agent"),
                entry(FisherEntryByProfitFactory.class, "Fish Entry By Profit"),
                entry(FisherEntryConstantRateFactory.class, "Fish Entry Constant Rate"),
                entry(SpendSaveInvestEntryFactory.class, "Spend Save Invest Entry"),
                entry(
                    FullSeasonalRetiredDataCollectorsFactory.class,
                    "Full-time Seasonal Retired Data Collectors"
                ),
                entry(BiomassDepletionGathererFactory.class, "Biomass Depletion Data Collectors"),
                entry(TowLongLoggerFactory.class, "Tow Long Logger"),
                entry(OnOffSwitchAllocatorFactory.class, "Effort Regulator"),
                entry(
                    AdditionalFishStateDailyCollectorsFactory.class,
                    "Additional Daily Collectors"
                ),
                entry(CatchAtBinFactory.class, "Catch at bin Collectors"),
                entry(HerfindalndexCollectorFactory.class, "Herfindal Index"),
                entry(ISlopeToTACControllerFactory.class, "ISlope-TAC Controller"),
                entry(LastCatchToTACController.class, "Last catch as TAC Controller"),
                entry(PIDControllerIndicatorTarget.class, "PID-TAC Controller"),
                entry(LBSPREffortPolicyFactory.class, "LBSPR Effort Controller"),
                entry(LBSPRffortPolicyAdaptingFactory.class, "LBSPR Effort Adaptive Controller"),
                entry(ITEControllerFactory.class, "ITEControllerFactory"),
                entry(LoptEffortPolicyFactory.class, "Lopt Effort Controller"),
                entry(ITargetTACFactory.class, "Itarget Controller"),
                entry(
                    SurplusProductionDepletionFormulaController.class,
                    "Schaefer Assessment Formula Controller"
                ),
                entry(SimpleFishSamplerFactory.class, "Simple Fisher Sampler"),
                entry(ExogenousFadMakerCSVFactory.class, "Exogenous Fad Maker CSV"),
                entry(FadDemoFactory.class, "Fad Demo"),
                entry(ExogenousFadSetterCSVFactory.class, "Exogenous Fad Setter CSV")
            )
        ));
        addFactories(new Factories<>(
            DepartingStrategy.class,
            ImmutableMap.ofEntries(
                entry(FixedProbabilityDepartingFactory.class, "Fixed Probability Departing"),
                entry(AdaptiveProbabilityDepartingFactory.class, "Adaptive Probability Departing"),
                entry(FixedRestTimeDepartingFactory.class, "Fixed Rest"),
                entry(DoubleLogisticDepartingFactory.class, "Double Logistic"),
                entry(MonthlyDepartingFactory.class, "Monthly Departing"),
                entry(MaxHoursPerYearDepartingFactory.class, "Max Hours Per Year"),
                entry(
                    MaxHoursOutWithRestingTimeDepartingStrategy.class,
                    "Max Hours Per Year Plus Resting Time"
                ),
                entry(LonglineFloridaLogisticDepartingFactory.class, "WFS Longline"),
                entry(FloridaLogisticDepartingFactory.class, "WFS Handline"),
                entry(ExitDecoratorFactory.class, "Exit Decorator"),
                entry(
                    FullSeasonalRetiredDecoratorFactory.class,
                    "Full-time Seasonal Retired Decorator"
                ),
                entry(PurseSeinerDepartingStrategyFactory.class, "Purse Seiner Departing Strategy")
            )
        ));
        addFactories(new Factories<>(
            DestinationStrategy.class,
            ImmutableMap.ofEntries(
                entry(RandomFavoriteDestinationFactory.class, "Random Favorite"),
                entry(FixedFavoriteDestinationFactory.class, "Fixed Favorite"),
                entry(RandomThenBackToPortFactory.class, "Always Random"),
                entry(YearlyIterativeDestinationFactory.class, "Yearly HillClimber"),
                entry(PerTripIterativeDestinationFactory.class, "Per Trip Iterative"),
                entry(PerTripImitativeDestinationFactory.class, "Imitator-Explorator"),
                entry(
                    PerTripImitativeWithHeadStartFactory.class,
                    "Imitator-Explorator with Head Start"
                ),
                entry(PerTripParticleSwarmFactory.class, "PSO"),
                entry(ThresholdEroteticDestinationFactory.class, "Threshold Erotetic"),
                entry(
                    BetterThanAverageEroteticDestinationFactory.class,
                    "Better Than Average Erotetic"
                ),
                entry(SNALSARDestinationFactory.class, "SNALSAR"),
                entry(HeatmapDestinationFactory.class, "Heatmap Based"),
                entry(PlanningHeatmapDestinationFactory.class, "Heatmap Planning"),
                entry(GravitationalSearchDestinationFactory.class, "GSA"),
                entry(BanditDestinationFactory.class, "Discretized Bandit"),
                entry(ClampedDestinationFactory.class, "Clamped to Data"),
                entry(PerfectDestinationFactory.class, "Perfect Knowledge"),
                entry(GeneralizedCognitiveStrategyFactory.class, "Generalized Cognitive Strategy"),
                entry(GravityDestinationStrategyFactory.class, "Gravity Destination Strategy"),
                entry(GenerateRandomPlansStrategyFactory.class, "Random Fishing Plans Strategy"),
                entry(EPOPlannedStrategyFlexibleFactory.class, "EPO planned strategy")
            )
        ));
        addFactories(new Factories<>(
            FishingStrategy.class,
            ImmutableMap.of(
                FishOnceFactory.class,
                "Fish Once",
                TowLimitFactory.class,
                "Tow Limit",
                QuotaLimitDecoratorFactory.class,
                "Quota Bound",
                FishUntilFullFactory.class,
                "Fish Until Full",
                MaximumStepsFactory.class,
                "Until Full With Day Limit",
                FloridaLogitReturnFactory.class,
                "WFS Logit Return",
                MaximumDaysAYearFactory.class,
                "Maximum Days a Year Decorator",
                PurseSeinerBiomassFishingStrategyFactory.class,
                "Purse Seiner Biomass Fishing Strategy",
                DefaultToDestinationStrategyFishingStrategyFactory.class,
                "Default to Destination Strategy",
                PurseSeinerAbundanceFishingStrategyFactory.class,
                "Purse Seiner Abundance Fishing Strategy"
            )
        ));
        addFactories(new Factories<>(
            Regulation.class,
            ImmutableMap.ofEntries(
                entry(AnarchyFactory.class, "Anarchy"),
                entry(FishingSeasonFactory.class, "Fishing Season"),
                entry(ProtectedAreasOnlyFactory.class, "MPA Only"),
                entry(
                    SpecificProtectedAreaFromShapeFileFactory.class,
                    "Specific MPA from Shape File"
                ),
                entry(
                    SpecificProtectedAreaFromCoordinatesFactory.class,
                    "Specific MPA from Coordinates"
                ),
                entry(ProtectedAreaChromosomeFactory.class, "MPA Chromosome"),
                entry(FinedProtectedAreasFactory.class, "MPA with fine"),
                entry(DepthMPAFactory.class, "MPA by depth"),
                entry(TACMonoFactory.class, "Mono-TAC"),
                entry(IQMonoFactory.class, "Mono-IQ"),
                entry(ITQMonoFactory.class, "Mono-ITQ"),
                entry(MultiITQFactory.class, "Multi-ITQ"),
                entry(MultiITQStringFactory.class, "Multi-ITQ by List"),
                entry(ITQSpecificFactory.class, "Partial-ITQ"),
                entry(TACMultiFactory.class, "Multi-TAC"),
                entry(MultiTACStringFactory.class, "Multi-TAC by List"),
                entry(KitchenSinkFactory.class, "Kitchen Sink"),
                entry(MultiQuotaMapFactory.class, "Multi-Quotas from Map"),
                entry(ThresholdSingleSpeciesTaxation.class, "Single Species Threshold Taxation"),
                entry(
                    SingleSpeciesPIDTaxationOnLandingsFactory.class,
                    "Single Species PID Taxation"
                ),
                entry(TemporaryProtectedAreasFactory.class, "Temporary MPA"),
                entry(TemporaryRegulationFactory.class, "Temporary Regulation"),
                entry(TaggedRegulationFactory.class, "Tagged Regulation"),
                entry(MultipleRegulationsFactory.class, "Multiple Regulations"),
                entry(ConjunctiveRegulationsFactory.class, "Conjunctive Regulations"),
                entry(WeakMultiTACStringFactory.class, "Weak Multi-TAC by List"),
                entry(PortBasedWaitTimesFactory.class, "Port Based Wait Times"),
                entry(MaxHoursOutFactory.class, "Max Hours Out"),
                entry(TriggerRegulationFactory.class, "Trigger Regulation"),
                entry(OffSwitchFactory.class, "Off Switch Decorator"),
                entry(NoFishingFactory.class, "No Fishing"),
                entry(ProtectedAreasFromFolderFactory.class, "Protected Areas from Folder")
            )
        ));
        addFactories(new Factories<>(
            BiologyInitializer.class,
            ImmutableMap.ofEntries(
                entry(IndependentLogisticFactory.class, "Independent Logistic"),
                entry(DiffusingLogisticFactory.class, "Diffusing Logistic"),
                entry(RockyLogisticFactory.class, "Habitat-Aware Diffusing Logistic"),
                entry(TwoSpeciesRockyLogisticFactory.class, "Habitat-Aware 2 Species"),
                entry(FromLeftToRightFactory.class, "From Left To Right Fixed"),
                entry(FromLeftToRightLogisticFactory.class, "From Left To Right Logistic"),
                entry(
                    FromLeftToRightLogisticPlusClimateChangeFactory.class,
                    "From Left To Right Logistic with Climate Change"
                ),
                entry(FromLeftToRightMixedFactory.class, "From Left To Right Well-Mixed"),
                entry(RandomConstantBiologyFactory.class, "Random Smoothed and Fixed"),
                entry(HalfBycatchFactory.class, "Half Bycatch"),
                entry(SplitInitializerFactory.class, "Split in Half"),
                entry(WellMixedBiologyFactory.class, "Well-Mixed"),
                entry(TwoSpeciesBoxFactory.class, "Two Species Box"),
                entry(SingleSpeciesBiomassFactory.class, "Single Species Biomass"),
                entry(
                    SingleSpeciesBiomassNormalizedFactory.class,
                    "Single Species Biomass Normalized"
                ),
                entry(
                    SingleSpeciesAbundanceFromDirectoryFactory.class,
                    "Single Species Abundance From Directory"
                ),
                entry(SingleSpeciesAbundanceFactory.class, "Single Species Abundance"),
                entry(MultipleIndependentSpeciesBiomassFactory.class, "Multiple Species Biomass"),
                entry(
                    MultipleIndependentSpeciesAbundanceFactory.class,
                    "Multiple Species Abundance"
                ),
                entry(OneSpeciesSchoolFactory.class, "One Species School"),
                entry(YellowBycatchFactory.class, "Yellow Bycatch Factory"),
                entry(YellowBycatchWithHistoryFactory.class, "Yellow Bycatch Factory with History"),
                entry(LinearGetterBiologyFactory.class, "Linear Getter Biology"),
                entry(SingleSpeciesRegularBoxcarFactory.class, "Boxcar Biology"),
                entry(SingleSpeciesBoxcarFromListFactory.class, "Boxcar Biology from List"),
                entry(SingleSpeciesIrregularBoxcarFactory.class, "Irregular Boxcar Biology"),
                entry(
                    SingleSpeciesBoxcarPulseRecruitmentFactory.class,
                    "Boxcar Biology with pulses"
                ),
                entry(BiomassInitializerFactory.class, "Biomass Initializer Factory")
            ),
            AbundanceInitializerFactory.class
        ));
        addFactories(new Factories<>(
            DirectedGraph.class,
            ImmutableMap.of(
                EmptyNetworkBuilder.class, "No Network",
                BarabasiAlbertBuilder.class, "Barabasi-Albert",
                EquidegreeBuilder.class, "Equal Out Degree",
                ClubNetworkBuilder.class, "Same Size Clubs"
            )
        ));
        addFactories(new Factories<>(
            Market.class,
            ImmutableMap.of(
                FixedPriceMarketFactory.class, "Fixed Price Market",
                AbundanceAwareFixedPriceMarketFactory.class, "Abundance Aware Fixed Price Market",
                ArrayFixedPriceMarket.class, "Fixed Price Market Array",
                CongestedMarketFactory.class, "Congested Market",
                MACongestedMarketFactory.class, "Moving Average Congested Market",
                ThreePricesMarketFactory.class, "Three Prices Market",
                NPricesMarketFactory.class, "Many Prices Market",
                SpeciesMarketMappedFactory.class, "Multiple Three Prices Markets",
                ThreePricesWithPremium.class, "Three Prices Market with premium",
                WeightLimitMarketFactory.class, "Weight Limit Market"
            )
        ));
        addFactories(new Factories<>(
            MarketMap.class,
            YearlyMarketMapFromPriceFileFactory.class
        ));
        addFactories(new Factories<>(
            AdaptationProbability.class,
            ImmutableMap.of(
                FixedProbabilityFactory.class, "Fixed Probability",
                DailyDecreasingProbabilityFactory.class, "Daily Decreasing Probability",
                ExplorationPenaltyProbabilityFactory.class, "Adaptive Probability",
                SocialAnnealingProbabilityFactory.class, "Social Annealing Probability",
                ThresholdProbabilityFactory.class, "Profit Threshold Probability"
            )
        ));
        addFactories(new Factories<>(
            WeatherInitializer.class,
            ImmutableMap.of(
                ConstantWeatherFactory.class, "Constant Weather",
                OscillatingWeatherFactory.class, "Oscillating Weather",
                TimeSeriesWeatherFactory.class, "CSV Fixed Weather"
            )
        ));
        addFactories(new Factories<>(
            WeatherEmergencyStrategy.class,
            ImmutableMap.of(
                IgnoreWeatherFactory.class, "Ignore Weather",
                WindThresholdFactory.class, "Sail up to Threshold"
            )
        ));
        addFactories(new Factories<>(
            HabitatInitializer.class,
            ImmutableMap.of(
                AllSandyHabitatFactory.class, "All Sand",
                RockyRectanglesHabitatFactory.class, "Rocky Rectangles",
                OneRockyRectangleFactory.class, "One Rocky Rectangle",
                RockyPyramidsFactory.class, "Rocky Pyramids"
            )
        ));
        addFactories(new Factories<>(
            Gear.class,
            ImmutableMap.ofEntries(
                entry(FixedProportionGearFactory.class, "Fixed Proportion"),
                entry(OneSpecieGearFactory.class, "One Species Gear"),
                entry(RandomCatchabilityTrawlFactory.class, "Random Catchability"),
                entry(RandomTrawlStringFactory.class, "Random Catchability By List"),
                entry(HabitatAwareGearFactory.class, "Habitat Aware Gear"),
                entry(ThresholdGearFactory.class, "Threshold Gear Factory"),
                entry(LogisticSelectivityGearFactory.class, "Logistic Selectivity Gear"),
                entry(SimpleLogisticGearFactory.class, "Simple Logistic Selectivity Gear"),
                entry(SelectivityFromListGearFactory.class, "Selectivity from List Gear"),
                entry(SimpleDomeShapedGearFactory.class, "Simple Dome Shaped Selectivity Gear"),
                entry(DoubleNormalGearFactory.class, "Double Normal Selectivity Gear"),
                entry(SablefishGearFactory.class, "Sablefish Trawl Selectivity Gear"),
                entry(HeterogeneousGearFactory.class, "Heterogeneous Selectivity Gear"),
                entry(
                    FixedProportionHomogeneousGearFactory.class,
                    "Abundance Fixed Proportion Gear"
                ),
                entry(GarbageGearFactory.class, "Garbage Gear"),
                entry(HoldLimitingDecoratorFactory.class, "Hold Upper Limit"),
                entry(DelayGearDecoratorFactory.class, "Hour Delay Gear"),
                entry(MaxThroughputDecoratorFactory.class, "Max Throughput Limit")
            )
        ));
        addFactories(new Factories<>(
            MapInitializer.class,
            ImmutableMap.of(
                SimpleMapInitializerFactory.class, "Simple Map",
                TwoSidedMapFactory.class, "Two Sided Map",
                MapWithFarOffPortsInitializerFactory.class, "Map with far-off ports",
                FromFileMapInitializerFactory.class, "From File Map",
                FromFileMapInitializerWithOverridesFactory.class, "From File Map With Overrides"
            )
        ));
        addFactories(new Factories<>(
            ObjectiveFunction.class,
            ImmutableMap.of(
                CashFlowObjectiveFactory.class, "Cash Flow Objective",
                HourlyProfitObjectiveFactory.class, "Hourly Profit Objective",
                TargetSpeciesObjectiveFactory.class, "Target Species Hourly Profit",
                KnifeEdgePerTripFactory.class, "Hourly Knife-Edge Objective",
                CutoffPerTripObjectiveFactory.class, "Hourly Cutoff Objective",
                KnifeEdgeCashflowFactory.class, "Cash Flow Knife-Edge Objective",
                SimulatedProfitCPUEObjectiveFactory.class, "Simulated Profit Objective"
            )
        ));
        addFactories(new Factories<>(
            GearStrategy.class,
            ImmutableMap.of(
                FixedGearStrategyFactory.class, "Never Change Gear",
                PeriodicUpdateFromListFactory.class, "Periodic Gear Update from List",
                PeriodicUpdateMileageFactory.class, "Periodic Gear Update Mileage",
                PeriodicUpdateCatchabilityFactory.class, "Periodic Gear Update Catchability",
                PeriodicUpdateSelectivityFactory.class, "Periodic Gear Update Selectivity",
                FadRefillGearStrategyFactory.class, "FAD Refill"
            )
        ));
        addFactories(new Factories<>(
            ProfitThresholdExtractor.class,
            ImmutableMap.of(
                FixedProfitThresholdFactory.class, "Fixed Threshold",
                AverageProfitsThresholdFactory.class, "Average Profits Threshold"
            )
        ));
        addFactories(new Factories<>(
            SocialAcceptabilityFeatureExtractor.class,
            ImmutableMap.of(
                EverywhereTrueExtractorFactory.class, "Socially Acceptable Everywhere",
                NoFriendsHereExtractorFactory.class, "No Friends Fish Here",
                NobodyFishesHereFactory.class, "Nobody Fishes Here"
            )
        ));
        addFactories(new Factories<>(
            SafetyFeatureExtractor.class,
            ImmutableMap.of(
                EverywhereTrueExtractorFactory.class,
                "Safe Everywhere",
                LessThanXFishersHereExtractorFactory.class,
                "Less Than X Fishers Currently Here Is Safe"
            )
        ));
        addFactories(new Factories<>(
            LegalityFeatureExtractor.class,
            ImmutableMap.of(
                EverywhereTrueExtractorFactory.class, "Ignore Rules",
                FollowRulesExtractorFactory.class, "Follow the Rules"
            )
        ));
        addFactories(new Factories<>(
            ProfitFeatureExtractor.class,
            ImmutableMap.of(
                RememberedProfitsExtractorFactory.class, "Remembered Profits"
            )
        ));
        addFactories(new Factories<>(
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
        ));
        addFactories(new Factories<>(
            AcquisitionFunction.class,
            ImmutableMap.of(
                ExhaustiveAcquisitionFunctionFactory.class, "Exhaustive Search",
                HillClimberAcquisitionFunctionFactory.class, "Hill-Climber Acquisition"
            )
        ));
        addFactories(new Factories<>(
            BanditSupplier.class,
            ImmutableMap.of(
                EpsilonGreedyBanditFactory.class, "Epsilon Greedy Bandit"
            )
        ));
        addFactories(new Factories<>(
            Averager.class,
            ImmutableMap.of(
                IterativeAverageFactory.class, "Average",
                MovingAverageFactory.class, "Moving Average",
                ExponentialMovingAverageFactory.class, "Exponential Moving Average"
            )
        ));
        addFactories(new Factories<>(
            PortInitializer.class,
            ImmutableMap.of(
                RandomPortFactory.class, "Random Ports",
                OnePortFactory.class, "One Port",
                TwoPortsFactory.class, "Two Ports",
                PortListFactory.class, "List of Ports"
            ),
            PortInitializerFromFileFactory.class
        ));
        addFactories(new Factories<>(
            MapDiscretizer.class,
            ImmutableMap.of(
                SquaresMapDiscretizerFactory.class, "Squared Discretization",
                CentroidMapFileFactory.class, "Centroid File Discretization",
                IdentityDiscretizerFactory.class, "Identity Discretization"
            )
        ));
        addFactories(new Factories<>(
            LogisticGrowerInitializer.class,
            ImmutableMap.of(
                SimpleLogisticGrowerFactory.class, "Independent Logistic Grower",
                CommonLogisticGrowerFactory.class, "Common Logistic Grower",
                FadAwareLogisticGrowerFactory.class, "FAD-Aware Logistic Grower"
            )
        ));
        addFactories(new Factories<>(
            LogbookInitializer.class,
            ImmutableMap.of(
                NoLogbookFactory.class, "No Logbook",
                TowAndAltitudeFactory.class, "Tows and Altitude"
            )
        ));
        addFactories(new Factories<>(
            DiscardingStrategy.class,
            ImmutableMap.of(
                NoDiscardingFactory.class, "No Discarding",
                DiscardingAllUnsellableFactory.class, "Discarding All Unsellable",
                AlwaysDiscardTheseSpeciesFactory.class, "Specific Discarding",
                DiscardUnderagedFactory.class, "Age Discarding"
            )
        ));
        addFactories(new Factories<>(
            Meristics.class,
            ImmutableMap.of(
                ListMeristicFactory.class, "Weight List Meristics",
                SimpleListMeristicFactory.class, "Length-Weight No Sex Meristics",
                MeristicsFileFactory.class, "Stock Assessment Meristics From File",
                EquallySpacedBertalanffyFactory.class, "Equally Spaced Von bertalanffy"
            )
        ));
        addFactories(new Factories<>(
            AbundanceDiffuser.class,
            ImmutableMap.of(
                NoDiffuserFactory.class, "No Diffusion",
                ConstantRateDiffuserFactory.class, "Constant Rate Diffusion",
                AgeLimitedConstantRateDiffuserFactory.class, "Bin-Restricted Diffusion"
            )
        ));
        addFactories(new Factories<>(
            AgingProcess.class,
            ImmutableMap.of(
                StandardAgingFactory.class, "Yearly Aging",
                ProportionalAgingFactory.class, "Proportional Aging",
                FixedBoxcarBertalannfyAging.class, "Fixed Boxcar VB Aging",
                SullivanAgingFactory.class, "Sullivan Matrix Aging"
            )
        ));
        addFactories(new Factories<>(
            InitialAbundance.class,
            ImmutableMap.of(
                InitialAbundanceFromStringFactory.class, "Abundance From String",
                InitialAbundanceFromListFactory.class, "Abundance From List",
                OneBinAbundanceFactory.class, "Abundance in one bin"
            )
        ));
        addFactories(new Factories<>(
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
        ));
        addFactories(new Factories<>(
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
        ));
        addFactories(new Factories<>(
            NaturalMortalityProcess.class,
            ImmutableMap.of(
                ProportionalMortalityFactory.class, "Proportional Mortality",
                ExponentialMortalityFactory.class, "Exponential Mortality"
            )
        ));
        addFactories(new Factories<>(
            GasPriceMaker.class,
            ImmutableMap.of(
                FixedGasFactory.class, "Fixed Gas Price",
                CsvTimeSeriesGasFactory.class, "Gas Price from File"
            )
        ));
    }

    private static void addFactories(final Factories<?> factories) {
        CONSTRUCTOR_MAP.put(factories.getClassObject(), factories.getConstructors());
        NAMES_MAP.put(factories.getClassObject(), factories.getNames());
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
    public static Stream<FactorySupplier> getAlgorithmFactoriesAsSuppliers() {
        return NAMES_MAP
            .entrySet()
            .stream()
            .flatMap(entry -> entry.getValue().entrySet().stream())
            .map(entry -> new BasicFactorySupplier<>(entry.getKey(), entry.getValue()));
    }

    public static <T> Map<String, Supplier<AlgorithmFactory<? extends T>>> getConstructors(final Class<T> classObject) {
        return (Map<String, Supplier<AlgorithmFactory<? extends T>>>) CONSTRUCTOR_MAP.get(
            classObject);
    }
}
