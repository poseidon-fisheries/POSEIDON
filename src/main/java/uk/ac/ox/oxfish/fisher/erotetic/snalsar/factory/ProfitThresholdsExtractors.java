package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.ProfitThresholdExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 6/8/16.
 */
public class ProfitThresholdsExtractors {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends ProfitThresholdExtractor<SeaTile>>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();


    static{



        CONSTRUCTORS.put("Fixed Threshold",
                         FixedProfitThresholdFactory::new);
        NAMES.put(FixedProfitThresholdFactory.class,
                  "Fixed Threshold");



        CONSTRUCTORS.put("Average Profits Threshold",
                         AverageProfitsThresholdFactory::new);
        NAMES.put(AverageProfitsThresholdFactory.class,
                  "Average Profits Threshold");






    }
}
