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
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 1/21/17.
 */
public class TwoPortsInitializerTest {


    @Test
    public void createsPortWhereYouWant() throws Exception
    {

        SimpleMapInitializer initializer = new SimpleMapInitializer(4, 4, 0, 0, 1, 10);
        NauticalMap map = initializer.makeMap(new MersenneTwisterFast(),
                                              new GlobalBiology(new Species("fake")),
                                              mock(FishState.class));

        TwoPortsInitializer ports = new TwoPortsInitializer(3,
                                                            1,
                                                            3,
                                                            2,
                                                            "North",
                                                            "South");
        ports.buildPorts(map,new MersenneTwisterFast(),mock(Function.class));
        assertEquals(map.getPorts().size(),2);
        for(Port port : map.getPorts())
        {
            assertEquals(port.getLocation().getGridX(),3);
            if(port.getName().equals("North"))
                assertEquals(port.getLocation().getGridY(),1);
            else
                {
                    assertEquals(port.getLocation().getGridY(),2);
                    assertEquals("South",port.getName());

                }
        }


    }
}