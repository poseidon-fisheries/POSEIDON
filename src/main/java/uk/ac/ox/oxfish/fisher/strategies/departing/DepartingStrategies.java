package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.utility.StrategyFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Here I hold the strategy factory
 * Created by carrknight on 5/19/15.
 */
public class DepartingStrategies {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,StrategyFactory<? extends DepartingStrategy>> CONSTRUCTORS =
            new LinkedHashMap<>();
    static{
        CONSTRUCTORS.put("Fixed Probability", FixedProbabilityDepartingStrategy.factory);
    }

    private DepartingStrategies() {}


}
