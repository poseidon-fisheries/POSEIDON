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
public class ConstantWeatherFactory implements AlgorithmFactory<ConstantWeatherInitializer> {


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
        return new ConstantWeatherInitializer(temperature, windSpeed, windOrientation);
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
