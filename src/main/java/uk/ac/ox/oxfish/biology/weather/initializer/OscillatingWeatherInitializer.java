package uk.ac.ox.oxfish.biology.weather.initializer;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;

import java.util.List;

/**
 * A completely unrealistic weather initializer. In a fixed period the weather goes from cold to hot and from low to high wind
 * and then back down
 * Created by carrknight on 9/7/15.
 */
public class OscillatingWeatherInitializer implements WeatherInitializer {


    private final double minTemperature;

    private final double maxTemperature;

    private final int oscillationPeriod;


    private final double minWindSpeed;

    private final double maxWindSpeed;


    public OscillatingWeatherInitializer(
            double minTemperature, double maxTemperature, int oscillationPeriod, double minWindSpeed,
            double maxWindSpeed) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        Preconditions.checkArgument(maxTemperature >= minTemperature, "max temperature must be more or equal min temperature");
        this.oscillationPeriod = oscillationPeriod;
        Preconditions.checkArgument(oscillationPeriod > 0, "oscillation period must be positive");
        this.minWindSpeed = minWindSpeed;
        this.maxWindSpeed = maxWindSpeed;
        Preconditions.checkArgument(maxWindSpeed >= minWindSpeed, "max temperature must be more or equal min temperature");

    }

    @Override
    public void processMap(
            NauticalMap map, MersenneTwisterFast random, FishState model) {


        List<SeaTile> seaTiles = map.getAllSeaTilesAsList();
        final ConstantWeather singleInstance = new ConstantWeather(minTemperature, minWindSpeed, 0);


        for(SeaTile tile : seaTiles)
        {
            tile.assignLocalWeather(singleInstance);
        }


        final double temperatureIncrement = (maxTemperature - minTemperature)/oscillationPeriod;
        final double speedIncrement = (maxWindSpeed - minWindSpeed)/oscillationPeriod;

        //create a steppable to modify the weather
        model.scheduleEveryDay(new Steppable() {
            @Override
            public void step(SimState simState)
            {
                double day = model.getDay();
                assert day >=0;

                // +1 increasing speed and temperature, -1 decreasing it
                double multiplier = Math.floor(day/oscillationPeriod)%2==0 ? 1 : -1;
                singleInstance.setTemperature(singleInstance.getTemperatureInCelsius() +
                                                      multiplier * temperatureIncrement);
                singleInstance.setWindSpeed(singleInstance.getWindSpeedInKph() +
                                                    multiplier* speedIncrement);



            }
        }, StepOrder.BIOLOGY_PHASE);


        //also add windspeed in the model aggregate data
        model.getDailyDataSet().registerGatherer("Model WindSpeed", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState state) {
                return singleInstance.getWindSpeedInKph();
            }
        },Double.NaN);


    }



}
