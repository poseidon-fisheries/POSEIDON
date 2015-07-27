package uk.ac.ox.oxfish.utility.maximization;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class ExplorationOrImitationMovementTest 
{


    @Test
    public void imitateBestFriend() throws Exception {

        Fisher optimizer = mock(Fisher.class);
        Fisher friend1 = mock(Fisher.class);
        Fisher friend2 = mock(Fisher.class);
        Map<Fisher,Double> fitness = new HashMap<>();
        fitness.put(friend1,100d);
        fitness.put(friend2,10d);
        Map<Fisher,SeaTile> locations = new HashMap<>();
        locations.put(friend1,mock(SeaTile.class));
        locations.put(friend2,mock(SeaTile.class));
        when(optimizer.getAllFriends()).thenReturn(Arrays.asList(friend1,friend2));


        IterativeMovement delegate = mock(IterativeMovement.class);
        //imitate best friend
        ExplorationOrImitationMovement test = new ExplorationOrImitationMovement(
                delegate,
                0,
                true,
                new MersenneTwisterFast(),
                fitness::get,
                locations::get
        );

        final SeaTile newObjective = test.adapt(optimizer,mock(NauticalMap.class) , mock(SeaTile.class), mock(SeaTile.class), 15, 10);
        //it should have not explored!
        verify(delegate,never()).adapt(any(),any() , any(), any(), anyDouble(), anyDouble());
        //should have copied friend 1
        assertEquals(newObjective, locations.get(friend1));


    }


    @Test
    public void friendsAreWorseSoExplore() throws Exception {

        Fisher optimizer = mock(Fisher.class);
        Fisher friend1 = mock(Fisher.class);
        Fisher friend2 = mock(Fisher.class);
        Map<Fisher,Double> fitness = new HashMap<>();
        fitness.put(friend1,15d);
        fitness.put(friend2,10d);
        Map<Fisher,SeaTile> locations = new HashMap<>();
        locations.put(friend1,mock(SeaTile.class));
        locations.put(friend2,mock(SeaTile.class));
        when(optimizer.getAllFriends()).thenReturn(Arrays.asList(friend1,friend2));


        IterativeMovement delegate = mock(IterativeMovement.class);
        final SeaTile explored = mock(SeaTile.class);
        when(delegate.adapt(any(), any(), any(), any(), anyDouble(), anyDouble())).thenReturn(explored);
        //imitate best friend
        ExplorationOrImitationMovement test = new ExplorationOrImitationMovement(
                delegate,
                0,
                true,
                new MersenneTwisterFast(),
                fitness::get,
                locations::get
        );

        final SeaTile newObjective = test.adapt(optimizer,mock(NauticalMap.class) , mock(SeaTile.class), mock(SeaTile.class), 10, 16);
        //it should have not explored!
        verify(delegate,times(1)).adapt(any(),any() , any(), any(), anyDouble(), anyDouble());
        assertEquals(newObjective, explored);


    }


    @Test
    public void friendsAreBetterButHardcodedExploration() throws Exception {

        Fisher optimizer = mock(Fisher.class);
        Fisher friend1 = mock(Fisher.class);
        Fisher friend2 = mock(Fisher.class);
        Map<Fisher,Double> fitness = new HashMap<>();
        fitness.put(friend1,100d);
        fitness.put(friend2,10d);
        Map<Fisher,SeaTile> locations = new HashMap<>();
        locations.put(friend1,mock(SeaTile.class));
        locations.put(friend2,mock(SeaTile.class));
        when(optimizer.getAllFriends()).thenReturn(Arrays.asList(friend1,friend2));


        IterativeMovement delegate = mock(IterativeMovement.class);
        final SeaTile explored = mock(SeaTile.class);
        when(delegate.adapt(any(),any() , any(), any(), anyDouble(), anyDouble())).thenReturn(explored);
        //imitate best friend
        ExplorationOrImitationMovement test = new ExplorationOrImitationMovement(
                delegate,
                1.0,
                true,
                new MersenneTwisterFast(),
                fitness::get,
                locations::get
        );

        final SeaTile newObjective = test.adapt(optimizer,mock(NauticalMap.class) , mock(SeaTile.class), mock(SeaTile.class), 15, 10);
        //it should have not explored!
        verify(delegate,times(1)).adapt(any(),any() , any(), any(), anyDouble(), anyDouble());
        assertEquals(newObjective, explored);


    }
}