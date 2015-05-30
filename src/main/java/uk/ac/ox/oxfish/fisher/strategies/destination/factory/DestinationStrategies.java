package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.RandomThenBackToPortDestinationStrategy;
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
    public static final LinkedHashMap<String,StrategyFactory<? extends DestinationStrategy>> CONSTRUCTORS =
            new LinkedHashMap<>();
    static{
        CONSTRUCTORS.put("Random Favorite", FavoriteDestinationStrategy.RANDOM_FAVORITE_DESTINATION_FACTORY);
        CONSTRUCTORS.put("Fixed Favorite", FavoriteDestinationStrategy.FIXED_FAVORITE_DESTINATION_FACTORY);
        CONSTRUCTORS.put("Always Random",
                         RandomThenBackToPortDestinationStrategy.RANDOM_THEN_BACK_TO_PORT_DESTINATION_STRATEGY_FACTORY);
    }

    private DestinationStrategies() {}

}
