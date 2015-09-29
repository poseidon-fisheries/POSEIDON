package uk.ac.ox.oxfish.biology.initializer;

import uk.ac.ox.oxfish.biology.initializer.factory.*;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A list of all constructors for biology initializers
 * Created by carrknight on 6/22/15.
 *
 */
public class BiologyInitializers {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends BiologyInitializer>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();
    static{
        CONSTRUCTORS.put("Independent Logistic",
                         IndependentLogisticFactory::new);
        NAMES.put(IndependentLogisticFactory.class,"Independent Logistic");

        CONSTRUCTORS.put("Diffusing Logistic",
                         DiffusingLogisticFactory::new);
        NAMES.put(DiffusingLogisticFactory.class,"Diffusing Logistic");


        CONSTRUCTORS.put("Habitat-Aware Diffusing Logistic",
                         RockyLogisticFactory::new);
        NAMES.put(RockyLogisticFactory.class,"Habitat-Aware Diffusing Logistic");


        CONSTRUCTORS.put("From Left To Right Fixed",
                         FromLeftToRightFactory::new);
        NAMES.put(FromLeftToRightFactory.class,"From Left To Right Fixed");

        CONSTRUCTORS.put("Random Smoothed and Fixed",
                         RandomConstantBiologyFactory::new);
        NAMES.put(RandomConstantBiologyFactory.class,"Random Smoothed and Fixed");

     CONSTRUCTORS.put("Half Bycatch",
                         HalfBycatchFactory::new);
        NAMES.put(HalfBycatchFactory.class,"Half Bycatch");

     CONSTRUCTORS.put("Split in Half",
                      SplitInitializerFactory::new);
        NAMES.put(SplitInitializerFactory.class,"Split in Half");


    }

    private BiologyInitializers() {}


}
