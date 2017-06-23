package uk.ac.ox.oxfish.fisher.strategies.discarding;

import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.RandomFavoriteDestinationFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Created by carrknight on 5/3/17.
 */
public class DiscardingStrategies {


    public static final LinkedHashMap<String,Supplier<AlgorithmFactory<? extends DiscardingStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();


    static {
        CONSTRUCTORS.put("No Discarding",
                         NoDiscardingFactory::new
        );
        NAMES.put(NoDiscardingFactory.class,
                  "No Discarding");

        CONSTRUCTORS.put("Discarding All Unsellable",
                         DiscardingAllUnsellableFactory::new
        );
        NAMES.put(DiscardingAllUnsellableFactory.class,
                  "Discarding All Unsellable");


        CONSTRUCTORS.put("Specific Discarding",
                         AlwaysDiscardTheseSpeciesFactory::new
        );
        NAMES.put(AlwaysDiscardTheseSpeciesFactory.class,
                  "Specific Discarding");

    }

}
