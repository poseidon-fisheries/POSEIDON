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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.NetworkPredicate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SamePortEdgesOnlyFactoryTest {

    @Test
    public void samePortTest() {

        final Fisher fisher1 = mock(Fisher.class);
        final Fisher fisher2 = mock(Fisher.class);

        final Port port1 = mock(Port.class);
        final Port port2 = mock(Port.class);

        final SamePortEdgesOnlyFactory edgesOnly = new SamePortEdgesOnlyFactory();
        final NetworkPredicate predicate = edgesOnly.apply(mock(FishState.class));

        when(fisher1.getHomePort()).thenReturn(port1);
        when(fisher2.getHomePort()).thenReturn(port2);
        Assertions.assertFalse(predicate.test(fisher1, fisher2));
        Assertions.assertFalse(predicate.test(fisher2, fisher1));

        when(fisher1.getHomePort()).thenReturn(port1);
        when(fisher2.getHomePort()).thenReturn(port1);
        Assertions.assertTrue(predicate.test(fisher1, fisher2));
        Assertions.assertTrue(predicate.test(fisher2, fisher1));
    }
}
