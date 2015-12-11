package uk.ac.ox.oxfish.model.network;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * The collection of all constructors
 * Created by carrknight on 7/1/15.
 */
public class NetworkBuilders
{

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String,Supplier<NetworkBuilder>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();

    static {

        CONSTRUCTORS.put("No Network",
                         EmptyNetworkBuilder::new
        );
        NAMES.put(EmptyNetworkBuilder.class,"No Network");
        CONSTRUCTORS.put("Barabasi-Albert",
                         BarabasiAlbertBuilder::new
        );
        NAMES.put(BarabasiAlbertBuilder.class,"Barabasi-Albert");
        CONSTRUCTORS.put("Equal Out Degree",
                         EquidegreeBuilder::new
        );
        NAMES.put(EquidegreeBuilder.class,"Equal Out Degree");

    }



}
