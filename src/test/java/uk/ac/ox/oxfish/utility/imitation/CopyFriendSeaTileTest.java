package uk.ac.ox.oxfish.utility.imitation;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CopyFriendSeaTileTest
{


    @Test
    public void ignoreFriend() throws Exception {


        SeaTile oldT = mock(SeaTile.class);
        SeaTile newT = mock(SeaTile.class);
        SeaTile friendT = mock(SeaTile.class);
        double oldFitness = 1;
        double newFitness = 2;

        ObjectiveFunction<Fisher> friendFunction = mock(ObjectiveFunction.class);

        //going to choose your own T because you have no friend
        SeaTile chosen  = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                               0d, newT, new ArrayList<>(),
                                                               friendFunction, fisher -> friendT);


        assertEquals(chosen, newT);

        //you have a friend but he has no fitness
        when(friendFunction.computeCurrentFitness(any())).thenReturn(Double.NaN);
        chosen  = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                               10d, newT, Collections.singletonList(mock(Fisher.class)),
                                                               friendFunction, fisher -> friendT);

        assertEquals(chosen,newT);

        //you have a friend with fitness but no seatile
        when(friendFunction.computeCurrentFitness(any())).thenReturn(0d);
        chosen  = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                       10d, newT, Collections.singletonList(mock(Fisher.class)),
                                                       friendFunction, fisher -> null);

        assertEquals(chosen,newT);

        //you have a normal friend but he has low fitness
        when(friendFunction.computeCurrentFitness(any())).thenReturn(0d);
        chosen  = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                       10d, newT, Collections.singletonList(mock(Fisher.class)),
                                                       friendFunction, fisher -> friendT);

        assertEquals(chosen,newT);

    }

    @Test
    public void fitnessMatters() throws Exception {


        SeaTile oldT = mock(SeaTile.class);
        SeaTile newT = mock(SeaTile.class);
        SeaTile friendT = mock(SeaTile.class);
        double oldFitness = 1;
        double newFitness = 2;

        ObjectiveFunction<Fisher> friendFunction = mock(ObjectiveFunction.class);




        //best fitness is the new
        when(friendFunction.computeCurrentFitness(any())).thenReturn(0d);
        SeaTile chosen  = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                       10d, newT, Collections.singletonList(mock(Fisher.class)),
                                                       friendFunction, fisher -> friendT);

        assertEquals(chosen,newT);

        //best fitness is friend
        when(friendFunction.computeCurrentFitness(any())).thenReturn(100d);
        chosen  = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                               10d, newT, Collections.singletonList(mock(Fisher.class)),
                                                               friendFunction, fisher -> friendT);

        assertEquals(chosen,friendT);

        //friends lose ties
        when(friendFunction.computeCurrentFitness(any())).thenReturn(10d);
        chosen  = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                       10d, newT, Collections.singletonList(mock(Fisher.class)),
                                                       friendFunction, fisher -> friendT);

        assertEquals(chosen,newT);
    }
}