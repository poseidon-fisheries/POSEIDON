package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.biology.BiomassResetterFactory;
import uk.ac.ox.oxfish.biology.BiomassTotalResetterFactory;
import uk.ac.ox.oxfish.biology.boxcars.AbundanceGathererBuilder;
import uk.ac.ox.oxfish.biology.boxcars.FishingMortalityAgentFactory;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgentBuilder;
import uk.ac.ox.oxfish.biology.boxcars.SprOracleBuilder;
import uk.ac.ox.oxfish.biology.complicated.factory.SnapshotAbundanceResetterFactory;
import uk.ac.ox.oxfish.biology.complicated.factory.SnapshotBiomassResetterFactory;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.data.collectors.AdditionalFishStateDailyCollectorsFactory;
import uk.ac.ox.oxfish.model.data.collectors.TowLongLoggerFactory;
import uk.ac.ox.oxfish.model.data.webviz.JsonOutputManagerFactory;
import uk.ac.ox.oxfish.model.regs.factory.OnOffSwitchAllocatorFactory;
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
        NAMES.put(SPRAgentBuilder.class, "SPR Agent");
        NAMES.put(SprOracleBuilder.class, "SPR Oracle");
        NAMES.put(FishingMortalityAgentFactory.class, "Fishing Mortality Agent");
        NAMES.put(FisherEntryByProfitFactory.class, "Fish Entry By Profit");
        NAMES.put(FisherEntryConstantRateFactory.class, "Fish Entry Constant Rate");
        NAMES.put(FullSeasonalRetiredDataCollectorsFactory.class, "Full-time Seasonal Retired Data Collectors");
        NAMES.put(BiomassDepletionGathererFactory.class, "Biomass Depletion Data Collectors");
        NAMES.put(TowLongLoggerFactory.class, "Tow Long Logger");
        NAMES.put(JsonOutputManagerFactory.class, "Json Output Manager");
        NAMES.put(OnOffSwitchAllocatorFactory.class, "Effort Regulator");
        NAMES.put(AdditionalFishStateDailyCollectorsFactory.class, "Additional Daily Collectors");
        NAMES.put(CatchAtBinFactory.class, "Catch at bin Collectors");

        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private AdditionalStartables() {}

}
