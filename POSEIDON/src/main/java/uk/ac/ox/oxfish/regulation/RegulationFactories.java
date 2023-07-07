package uk.ac.ox.oxfish.regulation;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RegulationFactories {

    public static final Map<String, Supplier<AlgorithmFactory<? extends Regulation>>> CONSTRUCTORS;
    public static final Map<Class<? extends AlgorithmFactory<?>>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(ConjunctiveRegulation.class, "Conjunctive regulation");
        NAMES.put(EverythingPermitted.class, "Everything permitted");
        NAMES.put(ForbiddenIf.class, "Forbidden if");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private RegulationFactories() {
    }
}
