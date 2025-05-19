/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.demoes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.OscillatingWeatherFactory;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.WindThresholdFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;

public class FishersDoNotLikeStorms {

    // when the storm comes, boats go to port.
    @Test
    public void doNotLikeStorms() throws Exception {

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

        // count fishers at sea, should be none
        double fishersAtSea = state.getFishers().stream().mapToDouble(
            value -> value.getLocation().equals(value.getHomePort().getLocation()) ? 0 : 1).sum();
        Assertions.assertEquals(100, state.getMap().getSeaTile(0, 0).getWindSpeedInKph(), 1);
        Assertions.assertEquals(0, fishersAtSea, .001);

        while (state.getDay() < 300)
            state.schedule.step(state);
        fishersAtSea = state.getFishers().stream().mapToDouble(
            value -> value.getLocation().equals(value.getHomePort().getLocation()) ? 0 : 1).sum();
        Assertions.assertTrue(fishersAtSea >= 30);
        Assertions.assertEquals(0, state.getMap().getSeaTile(0, 0).getWindSpeedInKph(), 1);

    }
}
