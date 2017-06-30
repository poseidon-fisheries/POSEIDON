package uk.ac.ox.oxfish.geography.mapmakers;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A list of all constructors for the map initializers
 * Created by carrknight on 11/5/15.
 */
public class MapInitializers {


    private MapInitializers() {
    }

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends MapInitializer>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();

    static {
        CONSTRUCTORS.put("Simple Map",
                         SimpleMapInitializerFactory::new);
        NAMES.put(SimpleMapInitializerFactory.class,"Simple Map");

        CONSTRUCTORS.put("OSMOSE Map",
                         OsmoseMapInitializerFactory::new);
        NAMES.put(OsmoseMapInitializerFactory.class,"OSMOSE Map");


        CONSTRUCTORS.put("OSMOSE Bounded Map",
                         OsmoseBoundedMapInitializerFactory::new);
        NAMES.put(OsmoseBoundedMapInitializerFactory.class,"OSMOSE Bounded Map");

        CONSTRUCTORS.put("From File Map",
                         FromFileMapInitializerFactory::new);
        NAMES.put(FromFileMapInitializerFactory.class,"From File Map");


    }




}
