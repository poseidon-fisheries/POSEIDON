package uk.ac.ox.oxfish.biology;

import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import static org.junit.Assert.*;
import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple4x4Map;


public class InfiniteSchoolTest {


    @Test
    public void small() throws Exception {

        FishState state = generateSimple4x4Map();
        NauticalMap map = state.getMap();

        InfiniteSchool school = new InfiniteSchool(1, 1, 1, 0, 100, null, new Pair<>(0, 0), new Pair<>(3, 3));
        assertTrue(school.contains(map.getSeaTile(1,1)));
        assertTrue(!school.contains(map.getSeaTile(0,0)));
        assertTrue(!school.contains(map.getSeaTile(2,2)));
        assertTrue(!school.contains(map.getSeaTile(1,2)));
        assertTrue(!school.contains(map.getSeaTile(2,1)));
        assertTrue(!school.contains(map.getSeaTile(1,0)));
        assertTrue(!school.contains(map.getSeaTile(0,1)));


        school.step(state);
        assertEquals(school.getPositionX(),0);
        assertEquals(school.getPositionY(),0);
        school.step(state);
        assertEquals(school.getPositionX(),1);
        assertEquals(school.getPositionY(),1);
        school.step(state);
        assertEquals(school.getPositionX(),2);
        assertEquals(school.getPositionY(),2);
        school.step(state);
        assertEquals(school.getPositionX(),3);
        assertEquals(school.getPositionY(),3);
        school.step(state);
        assertEquals(school.getPositionX(),2);
        assertEquals(school.getPositionY(),2);
    }


    @Test
    public void diameter() throws Exception {

        FishState state = generateSimple4x4Map();
        NauticalMap map = state.getMap();

        //diameter 1
        InfiniteSchool school = new InfiniteSchool(1, 1, 1, 1, 100, null, new Pair<>(0, 0), new Pair<>(3, 3));
        assertTrue(school.contains(map.getSeaTile(1,1)));
        //doesn't contain the diagonal neighbors
        assertTrue(!school.contains(map.getSeaTile(0,0)));
        assertTrue(!school.contains(map.getSeaTile(2,2)));
        //but contains the side ones
        assertTrue(school.contains(map.getSeaTile(1,2)));
        assertTrue(school.contains(map.getSeaTile(2,1)));
        assertTrue(school.contains(map.getSeaTile(1,0)));
        assertTrue(school.contains(map.getSeaTile(0,1)));


    }


    @Test
    public void slow() throws Exception {

        FishState state = generateSimple4x4Map();
        NauticalMap map = state.getMap();

        //moves every 2 days
        InfiniteSchool school = new InfiniteSchool(1, 1, 2, 0, 100, null, new Pair<>(0, 0), new Pair<>(3, 3));


        school.step(state);
        assertEquals(school.getPositionX(),1);
        assertEquals(school.getPositionY(),1);
        school.step(state);
        assertEquals(school.getPositionX(),0);
        assertEquals(school.getPositionY(),0);
        school.step(state);
        assertEquals(school.getPositionX(),0);
        assertEquals(school.getPositionY(),0);
        school.step(state);

        assertEquals(school.getPositionX(),1);
        assertEquals(school.getPositionY(),1);

    }
}