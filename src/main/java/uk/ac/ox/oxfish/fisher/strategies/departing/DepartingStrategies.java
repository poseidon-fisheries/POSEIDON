package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.fisher.strategies.departing.factory.DoubleLogisticDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedProbabilityDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedRestTimeDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.MonthlyDepartingFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Here I hold the strategy factory
 * Created by carrknight on 5/19/15.
 */
public class DepartingStrategies {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends DepartingStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();
    static{
        CONSTRUCTORS.put("Fixed Probability Departing",
                         FixedProbabilityDepartingFactory::new);
        NAMES.put(FixedProbabilityDepartingFactory.class,"Fixed Probability Departing");
        CONSTRUCTORS.put("Adaptive Probability Departing",
                         AdaptiveProbabilityDepartingFactory::new);
        NAMES.put(AdaptiveProbabilityDepartingFactory.class,"Adaptive Probability Departing");
        CONSTRUCTORS.put("Fixed Rest",
                         FixedRestTimeDepartingFactory::new);
        NAMES.put(FixedRestTimeDepartingFactory.class,"Fixed Rest");
        CONSTRUCTORS.put("Double Logistic",
                         DoubleLogisticDepartingFactory::new);
        NAMES.put(DoubleLogisticDepartingFactory.class,"Double Logistic");
        CONSTRUCTORS.put("Monthly Departing",
                         MonthlyDepartingFactory::new);
        NAMES.put(MonthlyDepartingFactory.class,"Monthly Departing");
    }

    private DepartingStrategies() {}






}
