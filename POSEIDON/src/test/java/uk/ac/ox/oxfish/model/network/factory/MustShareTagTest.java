/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.network.factory;

import com.google.common.collect.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.NetworkPredicate;

import java.util.LinkedList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MustShareTagTest {

    @Test
    public void mustShare() {

        final MustShareTag mustShare = new MustShareTag();
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

        //nothing ---> "A" is okay
        when(fisher1.getTagsList()).thenReturn(new LinkedList<>());
        when(fisher2.getTagsList()).thenReturn(Lists.newArrayList("A"));
        assertTrue(
            predicate.test(fisher1, fisher2)
        );

        //nothing ---> nothing is okay
        when(fisher1.getTagsList()).thenReturn(new LinkedList<>());
        when(fisher2.getTagsList()).thenReturn(new LinkedList<>());
        assertTrue(
            predicate.test(fisher1, fisher2)
        );

        //"A" ----> "A" is okay
        when(fisher1.getTagsList()).thenReturn(Lists.newArrayList("A"));
        when(fisher2.getTagsList()).thenReturn(Lists.newArrayList("A"));
        assertTrue(
            predicate.test(fisher1, fisher2)
        );

        //"A","B" ---> "A" is okay
        when(fisher1.getTagsList()).thenReturn(Lists.newArrayList("A"));
        when(fisher2.getTagsList()).thenReturn(Lists.newArrayList("A", "B"));
        assertTrue(
            predicate.test(fisher1, fisher2)
        );
        //  "B" ---> "A" is not okay
        when(fisher1.getTagsList()).thenReturn(Lists.newArrayList("B"));
        when(fisher2.getTagsList()).thenReturn(Lists.newArrayList("A"));
        assertFalse(predicate.test(fisher1, fisher2));
        //  "B" ---> nothing is not okay
        when(fisher1.getTagsList()).thenReturn(Lists.newArrayList("B"));
        when(fisher2.getTagsList()).thenReturn(Lists.newArrayList());
        assertFalse(predicate.test(fisher1, fisher2));
    }
}