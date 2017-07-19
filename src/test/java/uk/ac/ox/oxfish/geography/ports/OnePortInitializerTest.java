package uk.ac.ox.oxfish.geography.ports;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 1/21/17.
 */
public class OnePortInitializerTest {


    @Test
    public void createsPortWhereYouWant() throws Exception
    {

        SimpleMapInitializer initializer = new SimpleMapInitializer(4, 4, 0, 0, 1, 10);
        NauticalMap map = initializer.makeMap(new MersenneTwisterFast(),
                                               new GlobalBiology(new Species("fake")),
                                               mock(FishState.class));

        OnePortInitializer ports = new OnePortInitializer(3,1);
        ports.buildPorts(map, new MersenneTwisterFast(), mock(Function.class), mock(FishState.class),
                         new FixedGasPrice(2d ));
        assertEquals(map.getPorts().size(),1);
        assertEquals(map.getPorts().get(0).getGasPricePerLiter(),2d,.0001d);
        assertEquals(map.getPorts().iterator().next().getLocation().getGridX(),3);
        assertEquals(map.getPorts().iterator().next().getLocation().getGridY(),1);

    }
}