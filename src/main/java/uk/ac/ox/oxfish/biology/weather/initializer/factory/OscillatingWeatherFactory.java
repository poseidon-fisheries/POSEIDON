package uk.ac.ox.oxfish.biology.weather.initializer.factory;

import uk.ac.ox.oxfish.biology.weather.initializer.OscillatingWeatherInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Builds an OscillatingWeatherInitializer
 * Created by carrknight on 9/8/15.
 */
public class OscillatingWeatherFactory implements AlgorithmFactory<OscillatingWeatherInitializer>
{


    private DoubleParameter minTemperature = new FixedDoubleParameter(20);

    private DoubleParameter maxTemperature = new FixedDoubleParameter(45);


    private DoubleParameter minWindSpeed = new FixedDoubleParameter(0);

    private DoubleParameter maxWindSpeed = new FixedDoubleParameter(10);



    private DoubleParameter oscillationPeriod = new FixedDoubleParameter(100);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public OscillatingWeatherInitializer apply(FishState state) {

        return new OscillatingWeatherInitializer(
                minTemperature.apply(state.getRandom()),
                maxTemperature.apply(state.getRandom()),
                oscillationPeriod.apply(state.getRandom()).intValue(),

                minWindSpeed.apply(state.getRandom()),
                maxWindSpeed.apply(state.getRandom()));


    }

    public OscillatingWeatherFactory() {
    }

    public DoubleParameter getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(DoubleParameter minTemperature) {
        this.minTemperature = minTemperature;
    }

    public DoubleParameter getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(DoubleParameter maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public DoubleParameter getMinWindSpeed() {
        return minWindSpeed;
    }

    public void setMinWindSpeed(DoubleParameter minWindSpeed) {
        this.minWindSpeed = minWindSpeed;
    }

    public DoubleParameter getMaxWindSpeed() {
        return maxWindSpeed;
    }

    public void setMaxWindSpeed(DoubleParameter maxWindSpeed) {
        this.maxWindSpeed = maxWindSpeed;
    }


    public DoubleParameter getOscillationPeriod() {
        return oscillationPeriod;
    }

    public void setOscillationPeriod(DoubleParameter oscillationPeriod) {
        this.oscillationPeriod = oscillationPeriod;
    }
}
