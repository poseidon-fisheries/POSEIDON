package uk.ac.ox.oxfish.biology.weather.initializer.factory;

import uk.ac.ox.oxfish.biology.weather.initializer.ConstantWeatherInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Returns a constant weather initializer
 * Created by carrknight on 9/8/15.
 */
public class ConstantWeatherFactory implements AlgorithmFactory<ConstantWeatherInitializer>
{


    private DoubleParameter temperature = new FixedDoubleParameter(30);

    private DoubleParameter windSpeed = new FixedDoubleParameter(0);

    private DoubleParameter windOrientation = new FixedDoubleParameter(0);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ConstantWeatherInitializer apply(FishState state) {
        return new ConstantWeatherInitializer(temperature,windSpeed,windOrientation);
    }


    public DoubleParameter getTemperature() {
        return temperature;
    }

    public void setTemperature(DoubleParameter temperature) {
        this.temperature = temperature;
    }

    public DoubleParameter getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(DoubleParameter windSpeed) {
        this.windSpeed = windSpeed;
    }

    public DoubleParameter getWindOrientation() {
        return windOrientation;
    }

    public void setWindOrientation(DoubleParameter windOrientation) {
        this.windOrientation = windOrientation;
    }
}
