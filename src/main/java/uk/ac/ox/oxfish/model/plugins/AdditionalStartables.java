package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.biology.BiomassResetterFactory;
import uk.ac.ox.oxfish.biology.BiomassTotalResetterFactory;
import uk.ac.ox.oxfish.biology.boxcars.*;
import uk.ac.ox.oxfish.biology.complicated.factory.SnapshotAbundanceResetterFactory;
import uk.ac.ox.oxfish.biology.complicated.factory.SnapshotBiomassResetterFactory;
import uk.ac.ox.oxfish.biology.tuna.ScheduledBiomassProcessesFactory;
import uk.ac.ox.oxfish.biology.tuna.BiomassRestorerFactory;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadMakerCSVFactory;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadSetterCSVFactory;
import uk.ac.ox.oxfish.geography.fads.FadDemoFactory;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.regs.policymakers.factory.ISlopeToTACControllerFactory;
import uk.ac.ox.oxfish.model.data.collectors.AdditionalFishStateDailyCollectorsFactory;
import uk.ac.ox.oxfish.model.data.collectors.HerfindalndexCollectorFactory;
import uk.ac.ox.oxfish.model.data.collectors.TowLongLoggerFactory;
import uk.ac.ox.oxfish.model.data.webviz.JsonOutputManagerFactory;
import uk.ac.ox.oxfish.model.regs.factory.OnOffSwitchAllocatorFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.*;
import uk.ac.ox.oxfish.model.regs.policymakers.factory.ITEControllerFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.factory.ITargetTACFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.SimpleFishSamplerFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.SurplusProductionDepletionFormulaController;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class AdditionalStartables {

    public static final LinkedHashMap<String, Supplier<AlgorithmFactory<? extends AdditionalStartable>>> CONSTRUCTORS;
    public static final LinkedHashMap<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(TowAndAltitudePluginFactory.class, "Tow Heatmapper");
        NAMES.put(BiomassResetterFactory.class, "Biomass Location Resetter");
        NAMES.put(BiomassTotalResetterFactory.class, "Biomass Total Resetter");
        NAMES.put(SnapshotAbundanceResetterFactory.class, "Abundance Snapshot Resetter");
        NAMES.put(SnapshotBiomassResetterFactory.class, "Biomass Snapshot Resetter");
        NAMES.put(AbundanceGathererBuilder.class, "Abundance Gatherers");
        //these three probably need a linkedhashmap of their own::
        NAMES.put(SPRAgentBuilder.class, "SPR Agent");
        NAMES.put(SPRAgentBuilderSelectiveSampling.class, "SPR Selective Agent");
        NAMES.put(SPRAgentBuilderFixedSample.class, "SPR Fixed Sample Agent");
        /////////////////////////////////////////////////////
        NAMES.put(SprOracleBuilder.class, "SPR Oracle");
        NAMES.put(FishingMortalityAgentFactory.class, "Fishing Mortality Agent");
        NAMES.put(FisherEntryByProfitFactory.class, "Fish Entry By Profit");
        NAMES.put(FisherEntryConstantRateFactory.class, "Fish Entry Constant Rate");
        NAMES.put(SpendSaveInvestEntryFactory.class, "Spend Save Invest Entry");
        NAMES.put(FullSeasonalRetiredDataCollectorsFactory.class, "Full-time Seasonal Retired Data Collectors");
        NAMES.put(BiomassDepletionGathererFactory.class, "Biomass Depletion Data Collectors");
        NAMES.put(TowLongLoggerFactory.class, "Tow Long Logger");
        NAMES.put(JsonOutputManagerFactory.class, "Json Output Manager");
        NAMES.put(OnOffSwitchAllocatorFactory.class, "Effort Regulator");
        NAMES.put(AdditionalFishStateDailyCollectorsFactory.class, "Additional Daily Collectors");
        NAMES.put(CatchAtBinFactory.class, "Catch at bin Collectors");
        NAMES.put(HerfindalndexCollectorFactory.class, "Herfindal Index");


        NAMES.put(ISlopeToTACControllerFactory.class, "ISlope-TAC Controller");
        NAMES.put(LastCatchToTACController.class, "Last catch as TAC Controller");
        NAMES.put(PIDControllerIndicatorTarget.class, "PID-TAC Controller");
        NAMES.put(LBSPREffortPolicyFactory.class, "LBSPR Effort Controller");
        NAMES.put(LBSPRffortPolicyAdaptingFactory.class, "LBSPR Effort Adaptive Controller");
        NAMES.put(ITEControllerFactory.class, "ITEControllerFactory");
        NAMES.put(LoptEffortPolicyFactory.class, "Lopt Effort Controller");
        NAMES.put(ITargetTACFactory.class, "Itarget Controller");
        NAMES.put(SurplusProductionDepletionFormulaController.class, "Schaefer Assessment Formula Controller");
        NAMES.put(SimpleFishSamplerFactory.class, "Simple Fisher Sampler");

        NAMES.put(ScheduledBiomassProcessesFactory.class, "Scheduled Biomass Processes");
        NAMES.put(BiomassRestorerFactory.class, "Biomass Restorer");

        NAMES.put(ExogenousFadMakerCSVFactory.class, "Exogenous Fad Maker CSV");
        NAMES.put(FadDemoFactory.class, "Fad Demo");
        NAMES.put(ExogenousFadSetterCSVFactory.class, "Exogenous Fad Setter CSV");
        NAMES.put(IattcClosurePeriodRandomizerFactory.class, "IATTC Closure Period Randomizer");

        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private AdditionalStartables() {}

}
