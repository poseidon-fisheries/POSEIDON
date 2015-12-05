package uk.ac.ox.oxfish.utility.imitation;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CopyFriendSeaTileTest
{


    @Test
    public void ignoreFriend() throws Exception {


        SeaTile newT = mock(SeaTile.class);
        SeaTile friendT = mock(SeaTile.class);


        ObjectiveFunction<Fisher> friendFunction = mock(ObjectiveFunction.class);

        //going to choose your own T because you have no friend
        SeaTile chosen  = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                               0d, newT, new ArrayList<>(),
                                                               friendFunction, fisher -> friendT).getFirst();


        assertEquals(chosen, newT);

        //you have a friend but he has no fitness
        when(friendFunction.computeCurrentFitness(any())).thenReturn(Double.NaN);
        chosen  = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                               10d, newT, Collections.singletonList(mock(Fisher.class)),
                                                               friendFunction, fisher -> friendT).getFirst();

        assertEquals(chosen,newT);

        //you have a friend with fitness but no seatile
        when(friendFunction.computeCurrentFitness(any())).thenReturn(0d);
        chosen  = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                       10d, newT, Collections.singletonList(mock(Fisher.class)),
                                                       friendFunction, fisher -> null).getFirst();

        assertEquals(chosen,newT);

        //you have a normal friend but he has low fitness
        when(friendFunction.computeCurrentFitness(any())).thenReturn(0d);
        chosen  = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                       10d, newT, Collections.singletonList(mock(Fisher.class)),
                                                       friendFunction, fisher -> friendT).getFirst();

        assertEquals(chosen,newT);

    }

    @Test
    public void fitnessMatters() throws Exception {


        SeaTile newT = mock(SeaTile.class);
        SeaTile friendT = mock(SeaTile.class);


        ObjectiveFunction<Fisher> friendFunction = mock(ObjectiveFunction.class);




        //best fitness is the new
        when(friendFunction.computeCurrentFitness(any())).thenReturn(0d);
        Pair<SeaTile, Fisher> imitation = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                                                       10d, newT,
                                                                                       Collections.singletonList(
                                                                                               mock(Fisher.class)),
                                                                                       friendFunction,
                                                                                       fisher -> friendT);
        SeaTile chosen  = imitation.getFirst();
        assertNull(imitation.getSecond()); // we didn't imitate anyone
        assertEquals(chosen,newT);

        //best fitness is friend
        when(friendFunction.computeCurrentFitness(any())).thenReturn(100d);
        Fisher friend = mock(Fisher.class);
        imitation = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                                                       10d, newT,
                                                                                       Collections.singletonList(
                                                                                               friend),
                                                                                       friendFunction,
                                                                                       fisher -> friendT);
        assertEquals(imitation.getFirst(),friendT);
        assertEquals(imitation.getSecond(),friend); // we imitated our friend

        //friends lose ties
        when(friendFunction.computeCurrentFitness(any())).thenReturn(10d);
        imitation = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(),
                                                                                       10d, newT,
                                                                                       Collections.singletonList(
                                                                                               mock(Fisher.class)),
                                                                                       friendFunction,
                                                                                       fisher -> friendT);
        assertEquals(imitation.getFirst(),newT);
        assertNull(imitation.getSecond()); // we didn't imitate anyone

    }
}