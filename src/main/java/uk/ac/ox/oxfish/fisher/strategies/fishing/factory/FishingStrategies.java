package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.QuotaLimitDecorator;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * A map of string to constructors, good for gui
 * Created by carrknight on 5/28/15.
 */
public class FishingStrategies {

    public static final LinkedHashMap<String,
            Supplier<AlgorithmFactory<? extends FishingStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();

    static {


        CONSTRUCTORS.put("Fish Once", FishOnceFactory::new);
        NAMES.put(FishOnceFactory.class,"Fish Once");
        CONSTRUCTORS.put("Tow Limit", TowLimitFactory::new);
        NAMES.put(TowLimitFactory.class,"Tow Limit");
        CONSTRUCTORS.put("Quota Bound", QuotaLimitDecoratorFactory::new);
        NAMES.put(QuotaLimitDecoratorFactory.class,"Quota Bound");
        CONSTRUCTORS.put("Fish Until Full", FishUntilFullFactory::new);
        NAMES.put(FishUntilFullFactory.class,"Fish Until Full");
        CONSTRUCTORS.put("Until Full With Day Limit", MaximumStepsFactory::new);
        NAMES.put(MaximumStepsFactory.class,"Until Full With Day Limit");
        CONSTRUCTORS.put("WFS Logit Return", FloridaLogitReturnFactory::new);
        NAMES.put(FloridaLogitReturnFactory.class,"WFS Logit Return");

    }

}
