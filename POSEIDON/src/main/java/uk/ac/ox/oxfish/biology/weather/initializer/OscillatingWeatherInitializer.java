/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.weather.initializer;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
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
        double maxWindSpeed
    ) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        Preconditions.checkArgument(
            maxTemperature >= minTemperature,
            "max temperature must be more or equal min temperature"
        );
        this.oscillationPeriod = oscillationPeriod;
        Preconditions.checkArgument(oscillationPeriod > 0, "oscillation period must be positive");
        this.minWindSpeed = minWindSpeed;
        this.maxWindSpeed = maxWindSpeed;
        Preconditions.checkArgument(
            maxWindSpeed >= minWindSpeed,
            "max temperature must be more or equal min temperature"
        );

    }

    @Override
    public void processMap(
        NauticalMap map, MersenneTwisterFast random, FishState model
    ) {


        List<SeaTile> seaTiles = map.getAllSeaTilesAsList();
        final ConstantWeather singleInstance = new ConstantWeather(minTemperature, minWindSpeed, 0);


        for (SeaTile tile : seaTiles) {
            tile.assignLocalWeather(singleInstance);
        }


        final double temperatureIncrement = (maxTemperature - minTemperature) / oscillationPeriod;
        final double speedIncrement = (maxWindSpeed - minWindSpeed) / oscillationPeriod;

        //create a steppable to modify the weather
        model.scheduleEveryDay((Steppable) simState -> {
            double day = model.getDay();
            assert day >= 0;

            // +1 increasing speed and temperature, -1 decreasing it
            double multiplier = Math.floor(day / oscillationPeriod) % 2 == 0 ? 1 : -1;
            singleInstance.setTemperature(singleInstance.getTemperatureInCelsius() +
                multiplier * temperatureIncrement);
            singleInstance.setWindSpeed(singleInstance.getWindSpeedInKph() +
                multiplier * speedIncrement);


        }, StepOrder.BIOLOGY_PHASE);


        //also add windspeed in the model aggregate data
        model.getDailyDataSet().registerGatherer("Model WindSpeed",
            (Gatherer<FishState>) state -> singleInstance.getWindSpeedInKph(), Double.NaN);


    }


}
