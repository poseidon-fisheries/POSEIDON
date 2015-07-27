package uk.ac.ox.oxfish.utility.imitation;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class CopyFriendSeaTileTest
{


    @Test
    public void ignoreFriend() throws Exception {

        ImitativeMovement movement = new CopyFriendSeaTile();

        SeaTile oldT = mock(SeaTile.class);
        SeaTile newT = mock(SeaTile.class);
        double oldFitness = 1;
        double newFitness = 2;

        //going to choose your own T because you have no friend
        SeaTile chosen  = movement.adapt(mock(Fisher.class),mock(NauticalMap.class) , oldT, newT, oldFitness, newFitness,
                                         null, Double.NaN, null);
        assertEquals(chosen,newT);

        //you have a friend but he has no fitness
        chosen  = movement.adapt(mock(Fisher.class),mock(NauticalMap.class) , oldT, newT, oldFitness, newFitness,
                                 mock(Fisher.class), Double.NaN, null);
        assertEquals(chosen,newT);

        //you have a friend with fitness but no seatile
        chosen  = movement.adapt(mock(Fisher.class),mock(NauticalMap.class) , oldT, newT, oldFitness, newFitness,
                                 mock(Fisher.class), 1, null);
        assertEquals(chosen,newT);

        //you have a normal friend but he has low fitness
        chosen  = movement.adapt(mock(Fisher.class),mock(NauticalMap.class) , oldT, newT, oldFitness, newFitness,
                                 mock(Fisher.class), 1, mock(SeaTile.class));
        assertEquals(chosen,newT);

    }

    @Test
    public void fitnessMatters() throws Exception {

        ImitativeMovement movement = new CopyFriendSeaTile();

        SeaTile oldT = mock(SeaTile.class);
        SeaTile newT = mock(SeaTile.class);
        SeaTile friendT = mock(SeaTile.class);

        //best fitness is the old
        SeaTile chosen  = movement.adapt(mock(Fisher.class),mock(NauticalMap.class) , oldT, newT, 2, 1,
                                         mock(Fisher.class), 1, friendT);
        assertEquals(chosen,oldT);

        //best fitness is the new
        chosen  = movement.adapt(mock(Fisher.class),mock(NauticalMap.class) , oldT, newT, 1, 2,
                                 mock(Fisher.class), 1, friendT);
        assertEquals(chosen,newT);

        //best fitness is friend
        chosen  = movement.adapt(mock(Fisher.class),mock(NauticalMap.class) , oldT, newT, 1, 1,
                                 mock(Fisher.class), 2, friendT);
        assertEquals(chosen,friendT);

        //friend wins ties
        chosen  = movement.adapt(mock(Fisher.class), mock(NauticalMap.class), oldT, newT, 1, 1,
                                 mock(Fisher.class), 1, friendT);
        assertEquals(chosen,friendT);
    }
}