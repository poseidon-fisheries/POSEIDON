package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;


import uk.ac.ox.oxfish.fisher.erotetic.snalsar.SocialAcceptabilityFeatureExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SocialAcceptabilityFeatureExtractors
{

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory
            <? extends SocialAcceptabilityFeatureExtractor<SeaTile>>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String>
            NAMES =
            new LinkedHashMap<>();

    static
    {


        CONSTRUCTORS.put("Socially Acceptable Everywhere",
                         EverywhereTrueExtractorFactory::new);
        NAMES.put(EverywhereTrueExtractorFactory.class,
                  "Socially Acceptable Everywhere");

        CONSTRUCTORS.put("No Friends Fish Here",
                         NoFriendsHereExtractorFactory::new);
        NAMES.put(NoFriendsHereExtractorFactory.class,
                  "No Friends Fish Here");


        CONSTRUCTORS.put("Nobody Fishes Here",
                         NobodyFishesHereFactory::new);
        NAMES.put(NobodyFishesHereFactory.class,
                  "Nobody Fishes Here");


    }

}
