package uk.ac.ox.oxfish.regulation.conditions;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;
import uk.ac.ox.poseidon.regulations.core.conditions.Condition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConditionFactories {

    public static final Map<String, Supplier<AlgorithmFactory<? extends Condition>>> CONSTRUCTORS;
    public static final Map<Class<? extends AlgorithmFactory<?>>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(ActionCodeIs.class, "Action code is");
        NAMES.put(AgentHasTag.class, "Agent has tag");
        NAMES.put(AllOf.class, "All of");
        NAMES.put(AnyOf.class, "Any of");
        NAMES.put(BelowLimit.class, "Below limit");
        NAMES.put(Not.class, "Not");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private ConditionFactories() {
    }

}
