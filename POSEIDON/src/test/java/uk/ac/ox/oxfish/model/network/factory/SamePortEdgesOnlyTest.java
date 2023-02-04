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

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.NetworkPredicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SamePortEdgesOnlyTest {


    @Test
    public void samePortTest() {

        Fisher fisher1 = mock(Fisher.class);
        Fisher fisher2 = mock(Fisher.class);

        Port port1 = mock(Port.class);
        Port port2 = mock(Port.class);

        SamePortEdgesOnly edgesOnly = new SamePortEdgesOnly();
        NetworkPredicate predicate = edgesOnly.apply(mock(FishState.class));

        when(fisher1.getHomePort()).thenReturn(port1);
        when(fisher2.getHomePort()).thenReturn(port2);
        assertFalse(predicate.test(fisher1,fisher2));
        assertFalse(predicate.test(fisher2,fisher1));


        when(fisher1.getHomePort()).thenReturn(port1);
        when(fisher2.getHomePort()).thenReturn(port1);
        assertTrue(predicate.test(fisher1,fisher2));
        assertTrue(predicate.test(fisher2,fisher1));
    }
}