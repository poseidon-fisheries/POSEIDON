/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple4x4Map;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;


public class InfiniteSchoolTest {


    @SuppressWarnings("unchecked")
    @Test
    public void small() throws Exception {

        final FishState state = generateSimple4x4Map();
        final NauticalMap map = state.getMap();

        final InfiniteSchool school = new InfiniteSchool(1, 1, 1, 0, 100, null, entry(0, 0), entry(3, 3));
        Assertions.assertTrue(school.contains(map.getSeaTile(1, 1)));
        Assertions.assertFalse(school.contains(map.getSeaTile(0, 0)));
        Assertions.assertFalse(school.contains(map.getSeaTile(2, 2)));
        Assertions.assertFalse(school.contains(map.getSeaTile(1, 2)));
        Assertions.assertFalse(school.contains(map.getSeaTile(2, 1)));
        Assertions.assertFalse(school.contains(map.getSeaTile(1, 0)));
        Assertions.assertFalse(school.contains(map.getSeaTile(0, 1)));


        school.step(state);
        Assertions.assertEquals(school.getPositionX(), 0);
        Assertions.assertEquals(school.getPositionY(), 0);
        school.step(state);
        Assertions.assertEquals(school.getPositionX(), 1);
        Assertions.assertEquals(school.getPositionY(), 1);
        school.step(state);
        Assertions.assertEquals(school.getPositionX(), 2);
        Assertions.assertEquals(school.getPositionY(), 2);
        school.step(state);
        Assertions.assertEquals(school.getPositionX(), 3);
        Assertions.assertEquals(school.getPositionY(), 3);
        school.step(state);
        Assertions.assertEquals(school.getPositionX(), 2);
        Assertions.assertEquals(school.getPositionY(), 2);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void diameter() throws Exception {

        final FishState state = generateSimple4x4Map();
        final NauticalMap map = state.getMap();

        //diameter 1
        final InfiniteSchool school = new InfiniteSchool(1, 1, 1, 1, 100, null, entry(0, 0), entry(3, 3));
        Assertions.assertTrue(school.contains(map.getSeaTile(1, 1)));
        //doesn't contain the diagonal neighbors
        Assertions.assertFalse(school.contains(map.getSeaTile(0, 0)));
        Assertions.assertFalse(school.contains(map.getSeaTile(2, 2)));
        //but contains the side ones
        Assertions.assertTrue(school.contains(map.getSeaTile(1, 2)));
        Assertions.assertTrue(school.contains(map.getSeaTile(2, 1)));
        Assertions.assertTrue(school.contains(map.getSeaTile(1, 0)));
        Assertions.assertTrue(school.contains(map.getSeaTile(0, 1)));


    }


    @SuppressWarnings("unchecked")
    @Test
    public void slow() throws Exception {

        final FishState state = generateSimple4x4Map();
        final NauticalMap map = state.getMap();

        //moves every 2 days
        final InfiniteSchool school = new InfiniteSchool(1, 1, 2, 0, 100, null, entry(0, 0), entry(3, 3));


        school.step(state);
        Assertions.assertEquals(school.getPositionX(), 1);
        Assertions.assertEquals(school.getPositionY(), 1);
        school.step(state);
        Assertions.assertEquals(school.getPositionX(), 0);
        Assertions.assertEquals(school.getPositionY(), 0);
        school.step(state);
        Assertions.assertEquals(school.getPositionX(), 0);
        Assertions.assertEquals(school.getPositionY(), 0);
        school.step(state);

        Assertions.assertEquals(school.getPositionX(), 1);
        Assertions.assertEquals(school.getPositionY(), 1);

    }
}
