package uk.ac.ox.oxfish.fisher.strategies.destination;

import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.utility.StrategyFactory;

import java.util.LinkedHashMap;

/**
 * The collection of all the destination strategies factories.
 * Created by carrknight on 5/27/15.
 */
public class DestinationStrategies
{



    /**
     * the list of all registered CONSTRUCTORS
     */
    public static LinkedHashMap<String,StrategyFactory<? extends DestinationStrategy>> constructors =
            new LinkedHashMap<>();
    static{
        constructors.put("Random Favorite",FavoriteDestinationStrategy.RANDOM_FAVORITE_DESTINATION_FACTORY );
        constructors.put("Fixed Favorite",FavoriteDestinationStrategy.FIXED_FAVORITE_DESTINATION_FACTORY );
        constructors.put("Always Random", RandomThenBackToPortDestinationStrategy.RANDOM_THEN_BACK_TO_PORT_DESTINATION_STRATEGY_FACTORY);
    }

    private DestinationStrategies() {}

}
