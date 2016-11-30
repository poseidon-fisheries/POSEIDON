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
public class SquaresMapDiscretizerTest {


    @Test
    public void discretizeMaps() throws Exception
    {

        SimpleMapInitializer map = new SimpleMapInitializer(8,6,0,0,1);
        NauticalMap chart = map.makeMap(new MersenneTwisterFast(),
                                              mock(GlobalBiology.class),
                                              mock(FishState.class));
        MapDiscretization discretization = new MapDiscretization(new SquaresMapDiscretizer(2, 3));
        discretization.discretize(chart);
        assertEquals(discretization.getNumberOfGroups(),12);

        assertTrue(discretization.isValid(0));
        assertTrue(!discretization.isValid(11));

        assertTrue(discretization.getGroup(5).contains(chart.getSeaTile(2,2)));


    }


    @Test
    public void discretizeMaps2() throws Exception
    {

        SimpleMapInitializer map = new SimpleMapInitializer(50,50,0,0,1);
        NauticalMap chart = map.makeMap(new MersenneTwisterFast(),
                                        mock(GlobalBiology.class),
                                        mock(FishState.class));
        MapDiscretization discretization = new MapDiscretization(new SquaresMapDiscretizer(2, 2));
        discretization.discretize(chart);
        assertEquals(discretization.getNumberOfGroups(),9);



    }
}