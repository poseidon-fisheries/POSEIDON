package uk.ac.ox.oxfish.geography.ports;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 1/21/17.
 */
public class PortInitializers {

    /* the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends PortInitializer>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();


    static {
        CONSTRUCTORS.put("Random Ports",
                         RandomPortFactory::new);
        NAMES.put(RandomPortFactory.class,"Random Ports");

        CONSTRUCTORS.put("One Port",
                         OnePortFactory::new);
        NAMES.put(OnePortFactory.class,"One Port");

        CONSTRUCTORS.put("Two Ports",
                         TwoPortsFactory::new);
        NAMES.put(TwoPortsFactory.class,"Two Ports");



        CONSTRUCTORS.put("List of Ports",
                         PortListFactory::new);
        NAMES.put(PortListFactory.class,"List of Ports");




    }



}
