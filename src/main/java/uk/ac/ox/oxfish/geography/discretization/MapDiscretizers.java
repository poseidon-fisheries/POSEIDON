package uk.ac.ox.oxfish.geography.discretization;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Contains all the map discretizers
 * Created by carrknight on 1/27/17.
 */
public class MapDiscretizers {



    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String,Supplier<AlgorithmFactory<? extends MapDiscretizer>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();



    static {
        CONSTRUCTORS.put("Squared Discretization",
                         SquaresMapDiscretizerFactory::new
        );
        NAMES.put(SquaresMapDiscretizerFactory.class, "Squared Discretization");


        CONSTRUCTORS.put("Centroid File Discretization",
                         CentroidMapFileFactory::new
        );
        NAMES.put(CentroidMapFileFactory.class, "Centroid File Discretization");


    }

}
