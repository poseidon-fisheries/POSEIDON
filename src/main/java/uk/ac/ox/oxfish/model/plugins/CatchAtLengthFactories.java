package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.biology.BiomassResetterFactory;
import uk.ac.ox.oxfish.biology.BiomassTotalResetterFactory;
import uk.ac.ox.oxfish.biology.boxcars.*;
import uk.ac.ox.oxfish.biology.complicated.factory.SnapshotAbundanceResetterFactory;
import uk.ac.ox.oxfish.biology.complicated.factory.SnapshotBiomassResetterFactory;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.ISlopeTACController;
import uk.ac.ox.oxfish.model.data.collectors.AdditionalFishStateDailyCollectorsFactory;
import uk.ac.ox.oxfish.model.data.collectors.HerfindalndexCollectorFactory;
import uk.ac.ox.oxfish.model.data.collectors.TowLongLoggerFactory;
import uk.ac.ox.oxfish.model.data.webviz.JsonOutputManagerFactory;
import uk.ac.ox.oxfish.model.regs.factory.OnOffSwitchAllocatorFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.*;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.SimpleFishSamplerFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.SurplusProductionDepletionFormulaController;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class CatchAtLengthFactories {


    public static final LinkedHashMap<String, Supplier<CatchAtLengthFactory>> CONSTRUCTORS = new LinkedHashMap<>();
    public static final LinkedHashMap<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();
    static {
        //these three probably need a linkedhashmap of their own::
        NAMES.put(SPRAgentBuilder.class, "SPR Agent");
        CONSTRUCTORS.put("SPR Agent", SPRAgentBuilder::new);
        NAMES.put(SPRAgentBuilderSelectiveSampling.class, "SPR Selective Agent");
        CONSTRUCTORS.put("SPR Selective Agent", SPRAgentBuilderSelectiveSampling::new);
        NAMES.put(SPRAgentBuilderFixedSample.class, "SPR Fixed Sample Agent");
        CONSTRUCTORS.put("SPR Fixed Sample Agent", SPRAgentBuilderFixedSample::new);

    }


    private CatchAtLengthFactories() {
    }
}
