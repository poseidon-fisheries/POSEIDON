package uk.ac.ox.oxfish.fisher.log;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/27/15.
 */
public class LocationMemoriesTest {


    @Test
    public void rememberForget() throws Exception {



        /**
         * forgetting probability 100%!
         */
        LocationMemories<String> memories = new LocationMemories<>(1, 10, 0);

        //remember something
        SeaTile spot = mock(SeaTile.class);
        memories.memorize("Remember me?", spot);
        assertEquals("Remember me?",memories.getMemory(spot));

        //step it 9 times
        FishState mock = mock(FishState.class);
        when(mock.getRandom()).thenReturn(new MersenneTwisterFast());
        for(int i=0;i<9;i++)
            memories.step(mock);

        //should still be there!
        assertEquals("Remember me?",memories.getMemory(spot));
        //10th time
        memories.step(mock);

        //now forgotten
        assertNull(memories.getMemory(spot));

    }


    @Test
    public void forgetLimit() throws Exception {



        /**
         * forgetting probability 100%! but doesn't forget because it requires at least 1 element in memory
         */
        LocationMemories<String> memories = new LocationMemories<>(1, 5, 1);

        //remember something
        SeaTile spot = mock(SeaTile.class);
        memories.memorize("Remember me?", spot);
        assertEquals("Remember me?",memories.getMemory(spot));

        //step it 9 times
        FishState mock = mock(FishState.class);
        when(mock.getRandom()).thenReturn(new MersenneTwisterFast());
        for(int i=0;i<9;i++)

            memories.step(mock);

        //should still be there!
        assertEquals("Remember me?",memories.getMemory(spot));
        //10th time
        memories.step(mock);

        //NOT forgetten
        assertEquals("Remember me?", memories.getMemory(spot));

    }



}