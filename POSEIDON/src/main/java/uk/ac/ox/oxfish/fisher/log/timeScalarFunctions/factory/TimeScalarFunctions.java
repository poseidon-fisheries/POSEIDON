package uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.factory;

import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.TimeScalarFunction;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TimeScalarFunctions {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String, Supplier<AlgorithmFactory<? extends TimeScalarFunction>>> CONSTRUCTORS =
        new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory<?>>, String> NAMES = new LinkedHashMap<>();


    static {
        CONSTRUCTORS.put(
            "Inverse",
            InverseTimeScalarFactory::new
        );
        NAMES.put(
            InverseTimeScalarFactory.class,
            "Inverse"
        );

        CONSTRUCTORS.put(
            "Exponential",
            ExponentialTimeScalarFactory::new
        );
        NAMES.put(
            ExponentialTimeScalarFactory.class,
            "Exponential"
        );


        CONSTRUCTORS.put(
            "Sigmoidal",
            SigmoidalTimeScalarFactory::new
        );
        NAMES.put(
            SigmoidalTimeScalarFactory.class,
            "Sigmoidal"
        );
    }

    private TimeScalarFunctions() {

    }
}
