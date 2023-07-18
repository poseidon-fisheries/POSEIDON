package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AbundanceFilterFactories {

    public static final Map<String, Supplier<AlgorithmFactory<? extends AbundanceFilters>>>
        CONSTRUCTORS;

    public static final Map<Class<? extends AlgorithmFactory<?>>, String> NAMES =
        new LinkedHashMap<>();

    static {
        // TODO: our custom YAML loader breaks when assigning an anchor to a
        // a named algorithm factory, so we disable this for abundance filters,
        // but this will need to be fixed at some point.
        //putName(NAMES, AbundanceFiltersFromFileFactory.class);
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

}
