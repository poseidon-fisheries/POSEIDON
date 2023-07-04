/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology;

import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple4x4Map;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;


public class InfiniteSchoolTest {


    @SuppressWarnings("unchecked")
    @Test
    public void small() throws Exception {

        final FishState state = generateSimple4x4Map();
        final NauticalMap map = state.getMap();

        final InfiniteSchool school = new InfiniteSchool(1, 1, 1, 0, 100, null, entry(0, 0), entry(3, 3));
        assertTrue(school.contains(map.getSeaTile(1, 1)));
        assertFalse(school.contains(map.getSeaTile(0, 0)));
        assertFalse(school.contains(map.getSeaTile(2, 2)));
        assertFalse(school.contains(map.getSeaTile(1, 2)));
        assertFalse(school.contains(map.getSeaTile(2, 1)));
        assertFalse(school.contains(map.getSeaTile(1, 0)));
        assertFalse(school.contains(map.getSeaTile(0, 1)));


        school.step(state);
        assertEquals(school.getPositionX(), 0);
        assertEquals(school.getPositionY(), 0);
        school.step(state);
        assertEquals(school.getPositionX(), 1);
        assertEquals(school.getPositionY(), 1);
        school.step(state);
        assertEquals(school.getPositionX(), 2);
        assertEquals(school.getPositionY(), 2);
        school.step(state);
        assertEquals(school.getPositionX(), 3);
        assertEquals(school.getPositionY(), 3);
        school.step(state);
        assertEquals(school.getPositionX(), 2);
        assertEquals(school.getPositionY(), 2);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void diameter() throws Exception {

        final FishState state = generateSimple4x4Map();
        final NauticalMap map = state.getMap();

        //diameter 1
        final InfiniteSchool school = new InfiniteSchool(1, 1, 1, 1, 100, null, entry(0, 0), entry(3, 3));
        assertTrue(school.contains(map.getSeaTile(1, 1)));
        //doesn't contain the diagonal neighbors
        assertFalse(school.contains(map.getSeaTile(0, 0)));
        assertFalse(school.contains(map.getSeaTile(2, 2)));
        //but contains the side ones
        assertTrue(school.contains(map.getSeaTile(1, 2)));
        assertTrue(school.contains(map.getSeaTile(2, 1)));
        assertTrue(school.contains(map.getSeaTile(1, 0)));
        assertTrue(school.contains(map.getSeaTile(0, 1)));


    }


    @SuppressWarnings("unchecked")
    @Test
    public void slow() throws Exception {

        final FishState state = generateSimple4x4Map();
        final NauticalMap map = state.getMap();

        //moves every 2 days
        final InfiniteSchool school = new InfiniteSchool(1, 1, 2, 0, 100, null, entry(0, 0), entry(3, 3));


        school.step(state);
        assertEquals(school.getPositionX(), 1);
        assertEquals(school.getPositionY(), 1);
        school.step(state);
        assertEquals(school.getPositionX(), 0);
        assertEquals(school.getPositionY(), 0);
        school.step(state);
        assertEquals(school.getPositionX(), 0);
        assertEquals(school.getPositionY(), 0);
        school.step(state);

        assertEquals(school.getPositionX(), 1);
        assertEquals(school.getPositionY(), 1);

    }
}