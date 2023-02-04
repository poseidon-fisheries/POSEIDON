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
