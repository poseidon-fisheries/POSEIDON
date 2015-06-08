package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.utility.StrategyFactory;

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
    public static final Map<String,Supplier<StrategyFactory<? extends DepartingStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends StrategyFactory>,String> NAMES =
            new LinkedHashMap<>();
    static{
        CONSTRUCTORS.put("Fixed Probability",
                         FixedProbabilityDepartingFactory::new);
        NAMES.put(FixedProbabilityDepartingFactory.class,"Fixed Probability");
    }

    private DepartingStrategies() {}


}
