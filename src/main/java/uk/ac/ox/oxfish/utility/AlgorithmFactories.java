package uk.ac.ox.oxfish.utility;

import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializers;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.WeatherInitializers;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.DestinationStrategies;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.WeatherStrategies;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.factory.Markets;
import uk.ac.ox.oxfish.model.network.NetworkBuilders;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.Regulations;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.Probabilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Just a way to link a class to its constructor map
 * Created by carrknight on 5/29/15.
 */
public class AlgorithmFactories {


    //notice the <? extends AlgorithmFactory>. It's the need for hacks like these that explains why so many engineers
    //join terrorist organizations
    public static final Map<Class,Map<String,? extends Supplier<? extends AlgorithmFactory<?>>>> CONSTRUCTOR_MAP = new HashMap<>();
    public static final Map<Class,Map<Class<? extends AlgorithmFactory>,String>> NAMES_MAP = new HashMap<>();
    static
    {
        CONSTRUCTOR_MAP.put(DepartingStrategy.class, DepartingStrategies.CONSTRUCTORS);
        NAMES_MAP.put(DepartingStrategy.class, DepartingStrategies.NAMES);
        CONSTRUCTOR_MAP.put(DestinationStrategy.class, DestinationStrategies.CONSTRUCTORS);
        NAMES_MAP.put(DestinationStrategy.class, DestinationStrategies.NAMES);
        CONSTRUCTOR_MAP.put(FishingStrategy.class, FishingStrategies.CONSTRUCTORS);
        NAMES_MAP.put(FishingStrategy.class, FishingStrategies.NAMES);
        CONSTRUCTOR_MAP.put(Regulation.class, Regulations.CONSTRUCTORS);
        NAMES_MAP.put(Regulation.class, Regulations.NAMES);
        CONSTRUCTOR_MAP.put(BiologyInitializer.class, BiologyInitializers.CONSTRUCTORS);
        NAMES_MAP.put(BiologyInitializer.class, BiologyInitializers.NAMES);
        CONSTRUCTOR_MAP.put(DirectedGraph.class, NetworkBuilders.CONSTRUCTORS);
        NAMES_MAP.put(DirectedGraph.class, NetworkBuilders.NAMES);
        CONSTRUCTOR_MAP.put(Market.class, Markets.CONSTRUCTORS);
        NAMES_MAP.put(Market.class, Markets.NAMES);
        CONSTRUCTOR_MAP.put(AdaptationProbability.class, Probabilities.CONSTRUCTORS);
        NAMES_MAP.put(AdaptationProbability.class, Probabilities.NAMES);
        CONSTRUCTOR_MAP.put(WeatherInitializer.class, WeatherInitializers.CONSTRUCTORS);
        NAMES_MAP.put(WeatherInitializer.class, WeatherInitializers.NAMES);
        CONSTRUCTOR_MAP.put(WeatherEmergencyStrategy.class, WeatherStrategies.CONSTRUCTORS);
        NAMES_MAP.put(WeatherEmergencyStrategy.class, WeatherStrategies.NAMES);
    }


    /**
     * look up for any algorithm factory with a specific name, returning the first it finds
     * @param name the name
     * @return  the factory or null if there isn't any!
     */
    public static AlgorithmFactory constructorLookup(String name)
    {
        for(Map<String,? extends Supplier<? extends AlgorithmFactory<?>>> map : CONSTRUCTOR_MAP.values())
        {
            final Supplier<? extends AlgorithmFactory<?>> supplier = map.get(name);
            if(supplier != null)
                return supplier.get();
        }
        System.err.println("failed to find constructor named: " + name);
        return null;
    }

    /**
     * look up the name of the algorithm factory that has this class
     * @param factory the name
     * @return  the factory or null if there isn't any!
     */
    public static String nameLookup(Class<? extends AlgorithmFactory> factory)
    {
        for(Map<Class<? extends AlgorithmFactory>,String> map : NAMES_MAP.values())
        {
            final String name = map.get(factory);
            if(name != null)
                return name;
        }
        System.err.println("failed to find constructor: " + factory);
        return null;
    }

    /**
     * returns a list with all the factories available in the constructor Maps
     */
    public static List<Class<? extends AlgorithmFactory>> getAllAlgorithmFactories()
    {
        List<Class<? extends AlgorithmFactory>> classes = new LinkedList<>();
        for(Map<Class<? extends AlgorithmFactory>,String> names : NAMES_MAP.values())
            classes.addAll(names.keySet());
        return classes;
    }
}
