package uk.ac.ox.oxfish.utility;

import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializers;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.DestinationStrategies;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.Regulations;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Just a way to link a class to its constructor map
 * Created by carrknight on 5/29/15.
 */
public class StrategyFactories {


    //notice the <? extends StrategyFactory>. It's the need for hacks like these that explains why so many engineers
    //join terrorist organizations.mason
    public static final Map<Class,Map<String,? extends Supplier<? extends StrategyFactory<?>>>> CONSTRUCTOR_MAP = new HashMap<>();
    public static final Map<Class,Map<? extends Class<? extends StrategyFactory>,String>> NAMES_MAP = new HashMap<>();
    static
    {
        CONSTRUCTOR_MAP.put(DepartingStrategy.class, DepartingStrategies.CONSTRUCTORS);
        NAMES_MAP.put(DepartingStrategy.class, DepartingStrategies.NAMES);
        CONSTRUCTOR_MAP.put(DestinationStrategy.class, DestinationStrategies.CONSTRUCTORS);
        NAMES_MAP.put(DestinationStrategy.class, DestinationStrategies.NAMES);
        CONSTRUCTOR_MAP.put(FishingStrategy.class, FishingStrategies.CONSTRUCTORS);
        NAMES_MAP.put(FishingStrategy.class, FishingStrategies.NAMES);
        CONSTRUCTOR_MAP.put(Regulation.class, Regulations.CONSTRUCTORS);
        NAMES_MAP.put(Regulation.class, Regulations.NAMES);
        CONSTRUCTOR_MAP.put(BiologyInitializer.class, BiologyInitializers.CONSTRUCTORS);
        NAMES_MAP.put(BiologyInitializer.class, BiologyInitializers.NAMES);


    }


}
