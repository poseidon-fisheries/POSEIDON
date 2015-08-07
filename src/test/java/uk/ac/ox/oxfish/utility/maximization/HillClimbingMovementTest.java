package uk.ac.ox.oxfish.utility.maximization;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;



public class HillClimbingMovementTest {

    @Test
    public void hillclimber() throws Exception {


        SeaTile old = mock(SeaTile.class);
        SeaTile current = mock(SeaTile.class);
        SeaTile newTile = mock(SeaTile.class);
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());


        //if new is better than old, go random
        BeamHillClimbing<SeaTile> algo = new BeamHillClimbing<SeaTile>() {
            @Override
            public SeaTile randomStep(
                    FishState state, MersenneTwisterFast random, Fisher fisher, SeaTile current) {
                return newTile;
            }
        };

        assertEquals(newTile,algo.randomize(random,mock(Fisher.class),0,current));

        //current better than old? stay!
        assertEquals(current,algo.judgeRandomization(random,
                                                     mock(Fisher.class),0,100,old,current));

        //if old is better than new, go back to new
        assertEquals(old,algo.judgeRandomization(random,
                                                     mock(Fisher.class), 100,0, old, current));

    }
}