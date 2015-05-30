package uk.ac.ox.oxfish.utility;

import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.DestinationStrategies;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Just a way to link a class to its constructor map
 * Created by carrknight on 5/29/15.
 */
public class StrategyFactories {


    //notice the <? extends StrategyFactory>. It's the need for hacks like these that explains why so many engineers
    //join terrorist organizations.
    public static final Map<Class,Map<String,? extends StrategyFactory>> CONSTRUCTOR_MAP = new HashMap<>();
    static
    {
        CONSTRUCTOR_MAP.put(DepartingStrategy.class, DepartingStrategies.CONSTRUCTORS);
        CONSTRUCTOR_MAP.put(DestinationStrategy.class, DestinationStrategies.CONSTRUCTORS);
        CONSTRUCTOR_MAP.put(FishingStrategy.class, FishingStrategies.CONSTRUCTORS);


    }

}
