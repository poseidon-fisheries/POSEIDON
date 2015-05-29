package uk.ac.ox.oxfish.fisher.strategies.fishing;

import uk.ac.ox.oxfish.utility.StrategyFactory;

import java.util.LinkedHashMap;

/**
 * A map of string to constructors, good for gui
 * Created by carrknight on 5/28/15.
 */
public class FishingStrategies {

    public static final LinkedHashMap<String,StrategyFactory<? extends FishingStrategy>> CONSTRUCTORS =
            new LinkedHashMap<>();

    static {


        CONSTRUCTORS.put("Fish Once",FishOnceStrategy.FISH_ONCE_FACTORY);
        CONSTRUCTORS.put("Fish Until Full",FishUntilFullStrategy.FISH_UNTIL_FULL_FACTORY);
    }

}
