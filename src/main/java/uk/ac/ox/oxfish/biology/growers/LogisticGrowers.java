package uk.ac.ox.oxfish.biology.growers;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 1/31/17.
 */
public class LogisticGrowers {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String, Supplier<AlgorithmFactory<? extends LogisticGrowerInitializer>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>, String> NAMES =
            new LinkedHashMap<>();

    static {
        CONSTRUCTORS.put("Independent Logistic Grower",
                         SimpleLogisticGrowerFactory::new);
        NAMES.put(SimpleLogisticGrowerFactory.class,
                  "Independent Logistic Grower");

    }
}
