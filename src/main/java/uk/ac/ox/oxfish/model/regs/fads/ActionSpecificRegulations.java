package uk.ac.ox.oxfish.model.regs.fads;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ActionSpecificRegulations {

    public static final Map<String, Supplier<AlgorithmFactory<? extends ActionSpecificRegulation>>> CONSTRUCTORS;
    public static final Map<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(ActiveFadLimitsFactory.class, "Active FAD limits");
        NAMES.put(SetLimitsFactory.class, "General Set limits");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    public ActionSpecificRegulations() {}

}
