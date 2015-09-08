package uk.ac.ox.oxfish.biology.weather.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.List;

/**
 * a very simple weather initializer where every tile is given exactly the same weather
 * Created by carrknight on 9/7/15.
 */
public class ConstantWeatherInitializer implements WeatherInitializer
{



    private final DoubleParameter temperature;

    private final DoubleParameter windSpeed;

    private final DoubleParameter windOrientation;


    public ConstantWeatherInitializer(
            DoubleParameter temperature, DoubleParameter windSpeed,
            DoubleParameter windOrientation) {
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.windOrientation = windOrientation;
    }

    public ConstantWeatherInitializer(
            double temperature, double windSpeed,
            double windOrientation) {
        this.temperature = new FixedDoubleParameter(temperature);
        this.windSpeed = new FixedDoubleParameter(windSpeed);
        this.windOrientation = new FixedDoubleParameter(windOrientation);
    }



    @Override
    public void processMap(
            NauticalMap map, MersenneTwisterFast random, FishState model)
    {

        List<SeaTile> seaTiles = map.getAllSeaTilesAsList();

        for(SeaTile tile : seaTiles)
        {
            Double temperature = Math.max(this.temperature.apply(random), 0);
            Double speed = Math.max(windSpeed.apply(random),0);
            Double angle = Math.min(Math.max(windOrientation.apply(random), 0), 360);
            tile.assignLocalWeather(new ConstantWeather(temperature, speed, angle));
        }


    }
}
