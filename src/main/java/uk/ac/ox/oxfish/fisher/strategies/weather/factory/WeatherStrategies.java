package uk.ac.ox.oxfish.fisher.strategies.weather.factory;

import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * All the factories that build weather emergency strategies
 * Created by carrknight on 9/8/15.
 */
public class WeatherStrategies {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String,
            Supplier<AlgorithmFactory<? extends WeatherEmergencyStrategy>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,
            String> NAMES = new LinkedHashMap<>();



    static
    {

        CONSTRUCTORS.put("Ignore Weather",
                         IgnoreWeatherFactory::new
        );
        NAMES.put(IgnoreWeatherFactory.class,"Ignore Weather");


        CONSTRUCTORS.put("Sail up to Threshold",
                         WindThresholdFactory::new);
        NAMES.put(WindThresholdFactory.class,"Sail up to Threshold");



    }



}
