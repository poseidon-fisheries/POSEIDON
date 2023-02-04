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
