package uk.ac.ox.oxfish.fisher.strategies.weather.factory;

import uk.ac.ox.oxfish.fisher.strategies.weather.IgnoreWeatherStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * returns the same instance of ignore weather
 * Created by carrknight on 9/8/15.
 */
public class IgnoreWeatherFactory implements AlgorithmFactory<IgnoreWeatherStrategy>{


    private static IgnoreWeatherStrategy instance;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public IgnoreWeatherStrategy apply(FishState state) {
        if(instance == null)
            instance= new IgnoreWeatherStrategy();

        return instance;
    }
}
