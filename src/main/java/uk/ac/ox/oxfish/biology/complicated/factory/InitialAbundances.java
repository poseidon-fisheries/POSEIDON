package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.InitialAbundance;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 7/8/17.
 */
public class InitialAbundances {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends InitialAbundance>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();

    static{
        CONSTRUCTORS.put("Abundance From File",
                         InitialAbundanceFromFileFactory::new);
        NAMES.put(InitialAbundanceFromFileFactory.class,"Abundance From File");

        CONSTRUCTORS.put("Abundance From List",
                         InitialAbundanceFromListFactory::new);
        NAMES.put(InitialAbundanceFromListFactory.class,"Abundance From List");
    }

}
