package uk.ac.ox.oxfish.regulation.quantities;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static uk.ac.ox.oxfish.utility.Constructors.putName;

public class QuantityFactories {
    public static final Map<String, Supplier<AlgorithmFactory<? extends Quantity>>> CONSTRUCTORS;
    public static final Map<Class<? extends AlgorithmFactory<?>>, String> NAMES = new LinkedHashMap<>();

    static {
        putName(NAMES, SumOf.class);
        putName(NAMES, YearlyActionCount.class);
        NAMES.put(NumberOfActiveFads.class, "Number of active FADs");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }
}