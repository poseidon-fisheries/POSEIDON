package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * The collection of all the destination strategies factories.
 * Created by carrknight on 5/27/15.
 */
public class DestinationStrategies
{



    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String,Supplier<AlgorithmFactory<? extends DestinationStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();

    static{
        CONSTRUCTORS.put("Random Favorite",
                         RandomFavoriteDestinationFactory::new
                         );
        NAMES.put(RandomFavoriteDestinationFactory.class,"Random Favorite");
        CONSTRUCTORS.put("Fixed Favorite",
                         FixedFavoriteDestinationFactory::new);
        NAMES.put(FixedFavoriteDestinationFactory.class,"Fixed Favorite");
        CONSTRUCTORS.put("Always Random",
                         RandomThenBackToPortFactory::new);
        NAMES.put(RandomThenBackToPortFactory.class,"Always Random");
        CONSTRUCTORS.put("Yearly HillClimber",
                         YearlyIterativeDestinationFactory::new);
        NAMES.put(YearlyIterativeDestinationFactory.class,"Yearly HillClimber");
        CONSTRUCTORS.put("Per Trip Iterative",
                         PerTripIterativeDestinationFactory::new);
        NAMES.put(PerTripIterativeDestinationFactory.class,"Per Trip Iterative");

    }

    private DestinationStrategies() {}

}
