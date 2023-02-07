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

package uk.ac.ox.oxfish.demoes;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.OscillatingWeatherFactory;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.WindThresholdFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;


public class FishersDoNotLikeStorms {


    //when the storm comes, boats go to port.
    @Test
    public void doNotLikeStorms() throws Exception
    {

        PrototypeScenario scenario = new PrototypeScenario();

        OscillatingWeatherFactory weatherInitializer = new OscillatingWeatherFactory();
        weatherInitializer.setMaxWindSpeed(new FixedDoubleParameter(100));
        weatherInitializer.setMinWindSpeed(new FixedDoubleParameter(0));
        weatherInitializer.setOscillationPeriod(new FixedDoubleParameter(150));
        scenario.setWeatherInitializer(weatherInitializer);
        scenario.setFishers(100);

        WindThresholdFactory weatherStrategy = new WindThresholdFactory();
        weatherStrategy.setMaximumWindSpeedTolerated(new NormalDoubleParameter(50, 10));
        scenario.setWeatherStrategy(weatherStrategy);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();

        while (state.getDay() < 150)
            state.schedule.step(state);

        //count fishers at sea, should be none
        double fishersAtSea = state.getFishers().stream().mapToDouble(
                value -> value.getLocation().equals(value.getHomePort().getLocation()) ? 0 : 1).sum();
        Assert.assertEquals(100, state.getMap().getSeaTile(0, 0).getWindSpeedInKph(), 1);
        Assert.assertEquals(0, fishersAtSea, .001);

        while (state.getDay() < 300)
            state.schedule.step(state);
        fishersAtSea = state.getFishers().stream().mapToDouble(
                value -> value.getLocation().equals(value.getHomePort().getLocation()) ? 0 : 1).sum();
        Assert.assertTrue(fishersAtSea >= 30);
        Assert.assertEquals(0, state.getMap().getSeaTile(0, 0).getWindSpeedInKph(), 1);



    }
}
