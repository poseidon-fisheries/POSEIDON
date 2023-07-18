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

package uk.ac.ox.oxfish.utility.imitation;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CopyFriendSeaTileTest {


    @SuppressWarnings("unchecked")
    @Test
    public void ignoreFriend() {


        final SeaTile newT = mock(SeaTile.class);
        final SeaTile friendT = mock(SeaTile.class);


        final ObjectiveFunction<Fisher> friendFunction = mock(ObjectiveFunction.class);

        //going to choose your own T because you have no friend
        SeaTile chosen = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(), mock(Fisher.class),
            0d, newT,
            new ArrayList<>(), friendFunction, fisher -> friendT
        ).getKey();


        Assertions.assertEquals(chosen, newT);

        //you have a friend but he has no fitness
        when(friendFunction.computeCurrentFitness(any(), any())).thenReturn(Double.NaN);
        chosen = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(), mock(Fisher.class),
            10d, newT,
            Collections.singletonList(mock(Fisher.class)), friendFunction,
            fisher -> friendT
        ).getKey();

        Assertions.assertEquals(chosen, newT);

        //you have a friend with fitness but no seatile
        when(friendFunction.computeCurrentFitness(any(), any())).thenReturn(0d);
        chosen = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(), mock(Fisher.class),
            10d, newT,
            Collections.singletonList(mock(Fisher.class)), friendFunction,
            fisher -> null
        ).getKey();

        Assertions.assertEquals(chosen, newT);

        //you have a normal friend but he has low fitness
        when(friendFunction.computeCurrentFitness(any(), any())).thenReturn(0d);
        chosen = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(), mock(Fisher.class),
            10d, newT,
            Collections.singletonList(mock(Fisher.class)), friendFunction,
            fisher -> friendT
        ).getKey();

        Assertions.assertEquals(chosen, newT);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void fitnessMatters() throws Exception {


        final SeaTile newT = mock(SeaTile.class);
        final SeaTile friendT = mock(SeaTile.class);


        final ObjectiveFunction<Fisher> friendFunction = mock(ObjectiveFunction.class);


        //best fitness is the new
        when(friendFunction.computeCurrentFitness(any(), any())).thenReturn(0d);
        Map.Entry<SeaTile, Fisher> imitation = FishStateUtilities.imitateBestFriend(
            new MersenneTwisterFast(),
            mock(Fisher.class),
            10d,
            newT,
            Collections.singletonList(
                mock(Fisher.class)),
            friendFunction,
            fisher -> friendT
        );
        final SeaTile chosen = imitation.getKey();
        Assertions.assertNull(imitation.getValue()); // we didn't imitate anyone
        Assertions.assertEquals(chosen, newT);

        //best fitness is friend
        when(friendFunction.computeCurrentFitness(any(), any())).thenReturn(100d);
        final Fisher friend = mock(Fisher.class);
        imitation = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(), mock(Fisher.class),
            10d,
            newT,
            Collections.singletonList(
                friend),
            friendFunction, fisher -> friendT
        );
        Assertions.assertEquals(imitation.getKey(), friendT);
        Assertions.assertEquals(imitation.getValue(), friend); // we imitated our friend

        //friends lose ties
        when(friendFunction.computeCurrentFitness(any(), any())).thenReturn(10d);
        imitation = FishStateUtilities.imitateBestFriend(new MersenneTwisterFast(), mock(Fisher.class),
            10d,
            newT,
            Collections.singletonList(
                mock(Fisher.class)),
            friendFunction, fisher -> friendT
        );
        Assertions.assertEquals(imitation.getKey(), newT);
        Assertions.assertNull(imitation.getValue()); // we didn't imitate anyone

    }
}