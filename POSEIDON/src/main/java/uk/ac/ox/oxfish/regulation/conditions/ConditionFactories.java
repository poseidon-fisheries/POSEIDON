package uk.ac.ox.oxfish.regulation.conditions;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static uk.ac.ox.oxfish.utility.Constructors.putName;

public class ConditionFactories {

    public static final Map<String, Supplier<AlgorithmFactory<? extends Condition>>> CONSTRUCTORS;
    public static final Map<Class<? extends AlgorithmFactory<?>>, String> NAMES = new LinkedHashMap<>();

    static {
        putName(NAMES, ActionCodeIs.class);
        putName(NAMES, AgentHasTag.class);
        putName(NAMES, AllOf.class);
        putName(NAMES, AnyOf.class);
        putName(NAMES, Below.class);
        putName(NAMES, BetweenYearlyDates.class);
        putName(NAMES, InRectangularArea.class);
        putName(NAMES, Not.class);
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private ConditionFactories() {
    }

}
