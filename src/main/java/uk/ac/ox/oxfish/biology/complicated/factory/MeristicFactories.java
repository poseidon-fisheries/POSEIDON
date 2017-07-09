package uk.ac.ox.oxfish.biology.complicated.factory;


import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 7/8/17.
 */
public class MeristicFactories {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends Meristics>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();

    static{
        CONSTRUCTORS.put("Weight List Meristics",
                         ListMeristicFactory::new);
        NAMES.put(ListMeristicFactory.class,"Weight List Meristics");

        CONSTRUCTORS.put("Stock Assessment Meristics From File",
                         MeristicsFileFactory::new);
        NAMES.put(MeristicsFileFactory.class,"Stock Assessment Meristics From File");

    }

    private MeristicFactories() {}

}
