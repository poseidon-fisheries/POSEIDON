package uk.ac.ox.oxfish.biology.weather.initializer;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;
import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple4x4Map;


public class ConstantWeatherInitializerTest {


    @Test
    public void initializesCorrectly() throws Exception {

        ConstantWeatherInitializer initializer = new ConstantWeatherInitializer(
                new FixedDoubleParameter(100),
                new FixedDoubleParameter(100),
                new FixedDoubleParameter(999) //this will be automatically bounded to 360
        );

        FishState state = generateSimple4x4Map();
        NauticalMap map = state.getMap();

        //shouldn't have any local weather
        for(int x=0; x<4; x++)
            for(int y=0; y<4; y++)
                assertNull(map.getSeaTile(x, y).grabLocalWeather());


        initializer.processMap(state.getMap(),new MersenneTwisterFast(),state);


        for(int x=0; x<4; x++)
            for(int y=0; y<4; y++) {
                SeaTile tile = map.getSeaTile(x, y);
                assertTrue(tile.grabLocalWeather() instanceof ConstantWeather);
                assertEquals(tile.getTemperatureInCelsius(),100,.001);
                assertEquals(tile.getWindSpeedInKph(),100,.001);
                assertEquals(tile.getWindDirection(),360,.001); //bounded!
            }
    }
}