package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.utility.StrategyFactory;

import java.util.LinkedHashMap;

/**
 * Here I hold the strategy factory
 * Created by carrknight on 5/19/15.
 */
public class DepartingStrategies {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String,StrategyFactory<? extends DepartingStrategy>> CONSTRUCTORS =
            new LinkedHashMap<>();
    static{
        CONSTRUCTORS.put("Fixed Probability", FixedProbabilityDepartingStrategy.factory);
    }

    private DepartingStrategies() {}


}
