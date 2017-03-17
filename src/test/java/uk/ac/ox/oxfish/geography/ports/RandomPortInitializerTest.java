package uk.ac.ox.oxfish.geography.ports;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializer;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 1/21/17.
 */
public class RandomPortInitializerTest {



    @Test
    public void portsAreAllInSeparateAreas() throws Exception
    {

        SimpleMapInitializer initializer = new SimpleMapInitializer(4, 4, 0, 0, 1, 10);
        NauticalMap map = initializer.makeMap(new MersenneTwisterFast(),
                                              new GlobalBiology(new Species("fake")),
                                              mock(FishState.class));

        RandomPortInitializer ports = new RandomPortInitializer(4);
        ports.buildPorts(map,new MersenneTwisterFast(),mock(Function.class), mock(FishState.class));
        assertEquals(map.getPorts().size(),4);
        assertTrue(map.getSeaTile(3,0).isPortHere());
        assertTrue(map.getSeaTile(3,1).isPortHere());
        assertTrue(map.getSeaTile(3,2).isPortHere());
        assertTrue(map.getSeaTile(3,3).isPortHere());

    }


}