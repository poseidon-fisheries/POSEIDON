package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.fisher.strategies.departing.factory.*;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.UnifiedAmateurishDynamicFactory;
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

        CONSTRUCTORS.put("Unified Amateurish Dynamic Programming",
                         UnifiedAmateurishDynamicFactory::getInstance);
        NAMES.put(UnifiedAmateurishDynamicFactory.class,
                  "Unified Amateurish Dynamic Programming");


        CONSTRUCTORS.put("Max Hours Per Year",
                         MaxHoursPerYearDepartingFactory::new);
        NAMES.put(MaxHoursPerYearDepartingFactory.class,
                  "Max Hours Per Year");



        CONSTRUCTORS.put("WFS Longline",
                         LonglineFloridaLogisticDepartingFactory::new);
        NAMES.put(LonglineFloridaLogisticDepartingFactory.class,"WFS Longline");

        CONSTRUCTORS.put("WFS Handline",
                         FloridaLogisticDepartingFactory::new);
        NAMES.put(FloridaLogisticDepartingFactory.class, "WFS Handline");


        CONSTRUCTORS.put("Exit Decorator",
                         ExitDecoratorFactory::new);
        NAMES.put(ExitDecoratorFactory.class,"Exit Decorator");
    }

    private DepartingStrategies() {}






}
