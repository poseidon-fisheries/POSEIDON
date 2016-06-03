package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.LegalityFeatureExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The legality extractors
 * Created by carrknight on 5/31/16.
 */
public class LegalityFeatureExtractors {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends LegalityFeatureExtractor<SeaTile>>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();


    static {

        CONSTRUCTORS.put("Ignore Rules",
                         EverywhereTrueExtractorFactory::new);
        NAMES.put(EverywhereTrueExtractorFactory.class,
                  "Ignore Rules");

        CONSTRUCTORS.put("Follow the Rules",
                         EverywhereTrueExtractorFactory::new);
        NAMES.put(EverywhereTrueExtractorFactory.class,
                  "Follow the Rules");

    }
}
