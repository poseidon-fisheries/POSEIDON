package uk.ac.ox.oxfish.fisher.log.initializers;

import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.discretization.IdentityDiscretizerFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Created by carrknight on 2/17/17.
 */
public class LogbookInitializers {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String,
            Supplier<AlgorithmFactory<? extends LogbookInitializer>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String>
            NAMES = new LinkedHashMap<>();



    static {
        CONSTRUCTORS.put("No Logbook",
                         NoLogbookFactory::new
        );
        NAMES.put(NoLogbookFactory.class, "No Logbook");


        CONSTRUCTORS.put("Logit-like Logbook",
                         LogisticLogbookFactory::new
        );
        NAMES.put(LogisticLogbookFactory.class, "Logit-like Logbook");




    }
}
