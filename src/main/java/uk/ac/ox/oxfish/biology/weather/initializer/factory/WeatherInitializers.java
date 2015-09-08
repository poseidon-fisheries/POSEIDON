package uk.ac.ox.oxfish.biology.weather.initializer.factory;

import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * The collection of all factories that build weather initalizers
 * Created by carrknight on 9/8/15.
 */
public class WeatherInitializers {


    /**
     * can't be instantiated
     */
    private WeatherInitializers() {
    }

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String,
            Supplier<AlgorithmFactory<? extends WeatherInitializer>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,
            String> NAMES = new LinkedHashMap<>();



    static
    {

        CONSTRUCTORS.put("Constant Weather",
                         ConstantWeatherFactory::new
        );
        NAMES.put(ConstantWeatherFactory.class,"Constant Weather");


        CONSTRUCTORS.put("Oscillating Weather",
                         OscillatingWeatherFactory::new);
        NAMES.put(OscillatingWeatherFactory.class,"Oscillating Weather");



    }


}
