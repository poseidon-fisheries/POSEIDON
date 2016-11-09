package uk.ac.ox.oxfish.geography;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializer;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 11/9/16.
 */
public class MapDiscretizerTest {


    @Test
    public void discretizeMaps() throws Exception
    {

        SimpleMapInitializer map = new SimpleMapInitializer(8,6,0,0,1);
        NauticalMap chart = map.makeMap(new MersenneTwisterFast(),
                                              mock(GlobalBiology.class),
                                              mock(FishState.class));
        MapDiscretizer discretizer = new MapDiscretizer(chart,2,3);
        assertEquals(discretizer.getNumberOfGroups(),12);

        assertTrue(discretizer.isValid(0));
        assertTrue(!discretizer.isValid(11));

        assertTrue(discretizer.getGroup(5).contains(chart.getSeaTile(2,2)));


    }
}