package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.biology.boxcars.SPRAgentBuilder;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgentBuilderFixedSample;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgentBuilderSelectiveSampling;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

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
