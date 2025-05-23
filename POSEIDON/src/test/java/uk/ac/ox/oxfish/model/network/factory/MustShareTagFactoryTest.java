/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.network.factory;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.NetworkPredicate;

import java.util.LinkedList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MustShareTagFactoryTest {

    @Test
    public void mustShare() {

        final MustShareTagFactory mustShare = new MustShareTagFactory();
        mustShare.setMustShareOneOfThese("A,B");
        final NetworkPredicate predicate = mustShare.apply(mock(FishState.class));

        /*
         *  nothing ---> "A" is okay
         *  nothing ---> nothing is okay
         *  "A" ----> "A" is okay
         *  "A","B" ---> "A" is okay
         *  "B" ---> "A" is not okay
         *  "B" ---> nothing is not okay
         */
        final Fisher fisher1 = mock(Fisher.class);
        final Fisher fisher2 = mock(Fisher.class);

        // nothing ---> "A" is okay
        when(fisher1.getTagsList()).thenReturn(new LinkedList<>());
        when(fisher2.getTagsList()).thenReturn(Lists.newArrayList("A"));
        Assertions.assertTrue(predicate.test(fisher1, fisher2));

        // nothing ---> nothing is okay
        when(fisher1.getTagsList()).thenReturn(new LinkedList<>());
        when(fisher2.getTagsList()).thenReturn(new LinkedList<>());
        Assertions.assertTrue(predicate.test(fisher1, fisher2));

        //"A" ----> "A" is okay
        when(fisher1.getTagsList()).thenReturn(Lists.newArrayList("A"));
        when(fisher2.getTagsList()).thenReturn(Lists.newArrayList("A"));
        Assertions.assertTrue(predicate.test(fisher1, fisher2));

        //"A","B" ---> "A" is okay
        when(fisher1.getTagsList()).thenReturn(Lists.newArrayList("A"));
        when(fisher2.getTagsList()).thenReturn(Lists.newArrayList("A", "B"));
        Assertions.assertTrue(predicate.test(fisher1, fisher2));
        //  "B" ---> "A" is not okay
        when(fisher1.getTagsList()).thenReturn(Lists.newArrayList("B"));
        when(fisher2.getTagsList()).thenReturn(Lists.newArrayList("A"));
        Assertions.assertFalse(predicate.test(fisher1, fisher2));
        //  "B" ---> nothing is not okay
        when(fisher1.getTagsList()).thenReturn(Lists.newArrayList("B"));
        when(fisher2.getTagsList()).thenReturn(Lists.newArrayList());
        Assertions.assertFalse(predicate.test(fisher1, fisher2));
    }
}
