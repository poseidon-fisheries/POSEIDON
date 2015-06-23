package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishOnceStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishUntilFullStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.utility.StrategyFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * A map of string to constructors, good for gui
 * Created by carrknight on 5/28/15.
 */
public class FishingStrategies {

    public static final LinkedHashMap<String,
            Supplier<StrategyFactory<? extends FishingStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends StrategyFactory>,String> NAMES = new LinkedHashMap<>();

    static {


        CONSTRUCTORS.put("Fish Once", FishOnceFactory::new);
        NAMES.put(FishOnceFactory.class,"Fish Once");
        CONSTRUCTORS.put("Fish Until Full", FishUntilFullFactory::new);
        NAMES.put(FishUntilFullFactory.class,"Fish Until Full");
        CONSTRUCTORS.put("Until Full With Day Limit", MaximumStepsFactory::new);
        NAMES.put(MaximumStepsFactory.class,"Until Full With Day Limit");

    }

}
