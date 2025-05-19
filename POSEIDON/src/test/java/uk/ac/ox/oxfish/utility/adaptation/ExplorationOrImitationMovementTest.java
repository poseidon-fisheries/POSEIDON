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

package uk.ac.ox.oxfish.utility.adaptation;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;


public class ExplorationOrImitationMovementTest {


    @Test
    public void imitateBestFriend() throws Exception {

        final Fisher optimizer = mock(Fisher.class);
        final Fisher friend1 = mock(Fisher.class);
        final Fisher friend2 = mock(Fisher.class);
        when(friend1.isAllowedAtSea()).thenReturn(true);
        when(friend2.isAllowedAtSea()).thenReturn(true);
        final Map<Fisher, Double> fitness = new HashMap<>();
        fitness.put(friend1, 100d);
        fitness.put(friend2, 10d);
        fitness.put(optimizer, 0d);
        final Map<Fisher, SeaTile> locations = new HashMap<>();
        locations.put(friend1, mock(SeaTile.class));
        locations.put(friend2, mock(SeaTile.class));
        when(optimizer.getDirectedFriends()).thenReturn(Arrays.asList(friend1, friend2));

        //contains the result, for testing
        final SeaTile[] newObjective = {null};

        //imitate best friend
        final AdaptationAlgorithm<SeaTile> algorithm = spy(new BeamHillClimbing<SeaTile>(
            (state, random, fisher, current) -> null
        ));

        final ExploreImitateAdaptation<SeaTile> test = new ExploreImitateAdaptation<SeaTile>(
            fisher -> true,
            algorithm,
            (fisher, change, model) -> newObjective[0] = change,
            (Sensor<Fisher, SeaTile>) fisher -> locations.get(fisher),
            (observer, observed) -> fitness.get(observed),
            0d,
            1d, a -> true
        );


        test.adapt(optimizer, mock(FishState.class), new MersenneTwisterFast());

        //it should have not explored!
        verify(algorithm, never()).randomize(any(), any(), anyDouble(), any());
        verify(algorithm, never()).exploit(any(), any(), anyDouble(), any());
        //should have copied a friend
        Assertions.assertTrue(newObjective[0].equals(locations.get(friend1)) ||
            newObjective[0].equals(locations.get(friend2)));


    }


    @Test
    public void evenIfFriendsAreWorseYouCallImitate() throws Exception {

        final Fisher optimizer = mock(Fisher.class);
        final Fisher friend1 = mock(Fisher.class);
        final Fisher friend2 = mock(Fisher.class);
        when(friend1.isAllowedAtSea()).thenReturn(true);
        when(friend2.isAllowedAtSea()).thenReturn(true);
        final Map<Fisher, Double> fitness = new HashMap<>();
        fitness.put(friend1, 100d);
        fitness.put(friend2, 10d);
        fitness.put(optimizer, 1000d);
        final Map<Fisher, SeaTile> locations = new HashMap<>();
        locations.put(friend1, mock(SeaTile.class));
        locations.put(friend2, mock(SeaTile.class));
        when(optimizer.getDirectedFriends()).thenReturn(Arrays.asList(friend1, friend2));

        //contains the result, for testing
        final SeaTile[] newObjective = {null};
        final SeaTile randomized = mock(SeaTile.class);

        //imitate best friend
        final AdaptationAlgorithm<SeaTile> algorithm = spy(new BeamHillClimbing<SeaTile>(
            (state, random, fisher, current) -> randomized

        ));

        final ExploreImitateAdaptation<SeaTile> test = new ExploreImitateAdaptation<>(
            fisher -> true,
            algorithm,
            (fisher, change, model) -> newObjective[0] = change,
            fisher -> locations.get(fisher),
            (observer, observed) -> fitness.get(observed),
            0d,
            1d, a -> true
        );


        test.adapt(optimizer, mock(FishState.class), new MersenneTwisterFast());

        //it should have neither explored nor imitate
        verify(algorithm, never()).exploit(any(), any(), anyDouble(), any());
        verify(algorithm, never()).randomize(any(), any(), anyDouble(), any());
        verify(algorithm, times(1)).imitate(any(), any(), anyDouble(), any(), anyCollection(), any(), any());
        //should have stayed on its own
        Assertions.assertNull(newObjective[0]);
    }


    @Test
    public void friendsAreBetterButHardcodedExploration() throws Exception {
        final Fisher optimizer = mock(Fisher.class);
        final Fisher friend1 = mock(Fisher.class);
        final Fisher friend2 = mock(Fisher.class);
        final Map<Fisher, Double> fitness = new HashMap<>();
        fitness.put(friend1, 100d);
        fitness.put(friend2, 10d);
        fitness.put(optimizer, 0d);
        final Map<Fisher, SeaTile> locations = new HashMap<>();
        locations.put(friend1, mock(SeaTile.class));
        locations.put(friend2, mock(SeaTile.class));
        when(optimizer.getDirectedFriends()).thenReturn(Arrays.asList(friend1, friend2));

        //contains the result, for testing
        final SeaTile[] newObjective = {null};
        final SeaTile randomized = mock(SeaTile.class);

        //imitate best friend
        final AdaptationAlgorithm<SeaTile> algorithm = spy(
            new BeamHillClimbing<SeaTile>(
                (state, random, fisher, current) -> randomized
            )
        );

        final ExploreImitateAdaptation<SeaTile> test = new ExploreImitateAdaptation<>(
            fisher -> true,
            algorithm,
            (fisher, change, model) -> newObjective[0] = change,
            fisher -> locations.get(fisher),
            (observer, observed) -> fitness.get(observed),
            1d,
            1d, a -> true
        );


        test.adapt(optimizer, mock(FishState.class), new MersenneTwisterFast());

        //it should have explored! explored!
        verify(algorithm, times(1)).randomize(any(), any(), anyDouble(), any());
        verify(algorithm, never()).exploit(any(), any(), anyDouble(), any());
        verify(algorithm, never()).imitate(any(), any(), anyDouble(), any(), anyCollection(), any(), any());
        //should have randomized
        Assertions.assertEquals(newObjective[0], randomized);

    }
}
