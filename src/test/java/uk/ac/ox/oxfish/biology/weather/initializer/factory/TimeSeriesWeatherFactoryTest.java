package uk.ac.ox.oxfish.biology.weather.initializer.factory;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.biology.weather.initializer.CSVWeatherInitializer;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 11/29/16.
 */
public class TimeSeriesWeatherFactoryTest {


    @Test
    public void readCorrectly() throws Exception {


        TimeSeriesWeatherFactory factory = new TimeSeriesWeatherFactory(
                Paths.get("inputs","tests","weather.csv").toString(),
                true,
                ',',
                1
        );

        FishState mock = MovingTest.generateSimple4x4Map();
        CSVWeatherInitializer weather = factory.apply(mock);
        weather.processMap(mock.getMap(),new MersenneTwisterFast(),mock);

        assertEquals(mock.getMap().getSeaTile(0,0).getWindSpeedInKph(),1,.0001);
        weather.getActuator().step(mock);
        assertEquals(mock.getMap().getSeaTile(1,1).getWindSpeedInKph(),2,.0001);


        //notice that all tiles share the same weather object
        assertEquals(mock.getMap().getSeaTile(0,0).getWindSpeedInKph()
                           ,mock.getMap().getSeaTile(1,1).getWindSpeedInKph(),.001d);

        weather.getActuator().step(mock);
        assertEquals(mock.getMap().getSeaTile(2,2).getWindSpeedInKph(),3,.0001);

        weather.getActuator().step(mock);
        assertEquals(mock.getMap().getSeaTile(3,3).getWindSpeedInKph(),1,.0001);


    }
}