package uk.ac.ox.oxfish.utility.maximization;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;



public class HillClimbingMovementTest {

    @Test
    public void hillclimber() throws Exception {

        NauticalMap map = mock(NauticalMap.class);
        MersenneTwisterFast random = new MersenneTwisterFast();
        SeaTile newTile = mock(SeaTile.class); when(newTile.getAltitude()).thenReturn(-1d);
        when(map.getSeaTile(anyInt(),anyInt())).thenReturn(newTile);

        SeaTile old = mock(SeaTile.class);
        SeaTile current = mock(SeaTile.class);

        //if new is better than old, go random
        HillClimbingMovement algo = new HillClimbingMovement(map,random);
        assertEquals(newTile,algo.adapt(old,current,0,100));
        //if old is better than new, go back to new
        assertEquals(old,algo.adapt(old,current,0,-100));


    }
}