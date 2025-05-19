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

package uk.ac.ox.oxfish.biology.weather.initializer.factory;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.weather.initializer.CSVWeatherInitializer;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Paths;

/**
 * Created by carrknight on 11/29/16.
 */
public class TimeSeriesWeatherFactoryTest {


    @Test
    public void readCorrectly() throws Exception {


        TimeSeriesWeatherFactory factory = new TimeSeriesWeatherFactory(
            Paths.get("inputs", "tests", "weather.csv").toString(),
            true,
            ',',
            1
        );

        FishState mock = MovingTest.generateSimple4x4Map();
        CSVWeatherInitializer weather = factory.apply(mock);
        weather.processMap(mock.getMap(), new MersenneTwisterFast(), mock);

        Assertions.assertEquals(mock.getMap().getSeaTile(0, 0).getWindSpeedInKph(), 1, .0001);
        weather.getActuator().step(mock);
        Assertions.assertEquals(mock.getMap().getSeaTile(1, 1).getWindSpeedInKph(), 2, .0001);


        //notice that all tiles share the same weather object
        Assertions.assertEquals(mock.getMap().getSeaTile(0, 0).getWindSpeedInKph(),
            mock.getMap().getSeaTile(1, 1).getWindSpeedInKph(),
            .001d);

        weather.getActuator().step(mock);
        Assertions.assertEquals(mock.getMap().getSeaTile(2, 2).getWindSpeedInKph(), 3, .0001);

        weather.getActuator().step(mock);
        Assertions.assertEquals(mock.getMap().getSeaTile(3, 3).getWindSpeedInKph(), 1, .0001);


    }
}
