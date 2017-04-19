package uk.ac.ox.oxfish.biology.weather.initializer;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.StepOrder;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple4x4Map;


public class OscillatingWeatherInitializerTest
{


    @Test
    public void oscillatesCorrectly() throws Exception {


        OscillatingWeatherInitializer initializer = new OscillatingWeatherInitializer(

                0,100,5,100,200
        );

        FishState state = generateSimple4x4Map();
        when(state.getDailyDataSet()).thenReturn(mock(FishStateDailyTimeSeries.class));
        NauticalMap map = state.getMap();

        //shouldn't have any local weather
        for(int x=0; x<4; x++)
            for(int y=0; y<4; y++)
                assertNull(map.getSeaTile(x, y).grabLocalWeather());


        //prepare yourself to catch the steppable
        ArgumentCaptor<Steppable> argument = ArgumentCaptor.forClass(Steppable.class);

        //process the map
        initializer.processMap(state.getMap(),new MersenneTwisterFast(),state);
        Mockito.verify(state).scheduleEveryDay(argument.capture(), eq(StepOrder.BIOLOGY_PHASE));




        //should start with min
        for(int x=0; x<4; x++)
            for(int y=0; y<4; y++) {
                SeaTile tile = map.getSeaTile(x, y);
                assertTrue(tile.grabLocalWeather() instanceof ConstantWeather);
                assertEquals(tile.getTemperatureInCelsius(), 0, .001);
                assertEquals(tile.getWindSpeedInKph(), 100, .001);
                assertEquals(tile.getWindDirection(), 0, .001); //bounded!
            }


        //step
        Steppable weatherStep = argument.getValue();
        when(state.getDay()).thenReturn(0);
        weatherStep.step(state);
        assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 20, .001);
        assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 120, .001);
        when(state.getDay()).thenReturn(1);weatherStep.step(state);
        when(state.getDay()).thenReturn(2);weatherStep.step(state);
        when(state.getDay()).thenReturn(3);weatherStep.step(state);
        assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 80, .001);
        assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 180, .001);
        when(state.getDay()).thenReturn(4);
        weatherStep.step(state);
        assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 100, .001);
        assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 200, .001);

        //should go back now!
        when(state.getDay()).thenReturn(5);
        weatherStep.step(state);
        assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 80, .001);
        assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 180, .001);
        when(state.getDay()).thenReturn(6);weatherStep.step(state);
        when(state.getDay()).thenReturn(7);weatherStep.step(state);
        when(state.getDay()).thenReturn(8);weatherStep.step(state);
        when(state.getDay()).thenReturn(9);weatherStep.step(state);
        assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 0, .001);
        assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 100, .001);


        //should go back up!
        when(state.getDay()).thenReturn(10);weatherStep.step(state);
        assertEquals(map.getSeaTile(0, 0).getTemperatureInCelsius(), 20, .001);
        assertEquals(map.getSeaTile(0, 0).getWindSpeedInKph(), 120, .001);


    }
}