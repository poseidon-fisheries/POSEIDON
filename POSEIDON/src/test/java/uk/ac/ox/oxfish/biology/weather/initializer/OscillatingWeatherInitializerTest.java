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

package uk.ac.ox.oxfish.biology.weather.initializer;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.StepOrder;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple4x4Map;


public class OscillatingWeatherInitializerTest {


    @Test
    public void oscillatesCorrectly() throws Exception {


        OscillatingWeatherInitializer initializer = new OscillatingWeatherInitializer(

            0, 100, 5, 100, 200
        );

        FishState state = generateSimple4x4Map();
        when(state.getDailyDataSet()).thenReturn(mock(FishStateDailyTimeSeries.class));
        NauticalMap map = state.getMap();

        //shouldn't have any local weather
        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++)
                Assertions.assertNull(map.getSeaTile(x, y).grabLocalWeather());


        //prepare yourself to catch the steppable
        ArgumentCaptor<Steppable> argument = ArgumentCaptor.forClass(Steppable.class);

        //process the map
        initializer.processMap(state.getMap(), new MersenneTwisterFast(), state);
        Mockito.verify(state).scheduleEveryDay(argument.capture(), eq(StepOrder.BIOLOGY_PHASE));


        //should start with min
        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++) {
                SeaTile tile = map.getSeaTile(x, y);
                Assertions.assertTrue(tile.grabLocalWeather() instanceof ConstantWeather);
                Assertions.assertEquals(tile.getTemperatureInCelsius(), 0, .001);
                Assertions.assertEquals(tile.getWindSpeedInKph(), 100, .001);
                Assertions.assertEquals(tile.getWindDirection(), 0, .001); //bounded!
            }


        //step
        Steppable weatherStep = argument.getValue();
        when(state.getDay()).thenReturn(0);
        weatherStep.step(state);
        Assertions.assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 20, .001);
        Assertions.assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 120, .001);
        when(state.getDay()).thenReturn(1);
        weatherStep.step(state);
        when(state.getDay()).thenReturn(2);
        weatherStep.step(state);
        when(state.getDay()).thenReturn(3);
        weatherStep.step(state);
        Assertions.assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 80, .001);
        Assertions.assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 180, .001);
        when(state.getDay()).thenReturn(4);
        weatherStep.step(state);
        Assertions.assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 100, .001);
        Assertions.assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 200, .001);

        //should go back now!
        when(state.getDay()).thenReturn(5);
        weatherStep.step(state);
        Assertions.assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 80, .001);
        Assertions.assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 180, .001);
        when(state.getDay()).thenReturn(6);
        weatherStep.step(state);
        when(state.getDay()).thenReturn(7);
        weatherStep.step(state);
        when(state.getDay()).thenReturn(8);
        weatherStep.step(state);
        when(state.getDay()).thenReturn(9);
        weatherStep.step(state);
        Assertions.assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 0, .001);
        Assertions.assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 100, .001);


        //should go back up!
        when(state.getDay()).thenReturn(10);
        weatherStep.step(state);
        Assertions.assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 20, .001);
        Assertions.assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 120, .001);


    }
}
