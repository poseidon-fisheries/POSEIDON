package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.SafetyFeatureExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The safety extractors
 * Created by carrknight on 5/31/16.
 */
public class SafetyFeatureExtractors {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends SafetyFeatureExtractor<SeaTile>>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();


    static{



        CONSTRUCTORS.put("Safe Everywhere",
                         EverywhereTrueExtractorFactory::new);
        NAMES.put(EverywhereTrueExtractorFactory.class,
                  "Safe Everywhere");

        CONSTRUCTORS.put("Less Than X Fishers Currently Here Is Safe",
                         LessThanXFishersHereExtractorFactory::new);
        NAMES.put(LessThanXFishersHereExtractorFactory.class,
                  "Less Than X Fishers Currently Here Is Safe");




    }

}
