/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.strategies.destination.fad;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.ports.Port;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeCornerPortMap;

public class CurrentRouteTest {

    @Test
    public void test() {
        NauticalMap map = makeCornerPortMap(3, 3);
        final Port port = map.getPorts().getFirst();

        final Fisher fisher = mock(Fisher.class);
        when(fisher.getHomePort()).thenReturn(port);
        when(fisher.getLocation()).thenReturn(map.getSeaTile(2, 2));

        final CurrentRoute currentRoute = new CurrentRoute();
        assertFalse(currentRoute.nextDestination(fisher).isPresent());
        assertFalse(currentRoute.currentDestination().isPresent());

        currentRoute.selectNewRoute(new RouteToPortSelector(map), fisher, 0, null);

        // we're at 2, 2 and want to fish there, so we should stay there
        when(fisher.canAndWantToFishHere()).thenReturn(true);
        currentRoute.nextDestination(fisher);
        when(fisher.getLocation()).thenReturn(currentRoute.currentDestination().get());
        assertEquals(map.getSeaTile(2, 2), fisher.getLocation());

        // we're at 2, 2 and we don't want to fish there anymore, so we should head for 1, 1
        when(fisher.canAndWantToFishHere()).thenReturn(false);
        currentRoute.nextDestination(fisher);
        when(fisher.getLocation()).thenReturn(currentRoute.currentDestination().get());
        assertEquals(map.getSeaTile(1, 1), fisher.getLocation());

        // we're at 1, 1 and want to fish there, so we should stay there
        when(fisher.canAndWantToFishHere()).thenReturn(true);
        currentRoute.nextDestination(fisher);
        when(fisher.getLocation()).thenReturn(currentRoute.currentDestination().get());
        assertEquals(map.getSeaTile(1, 1), fisher.getLocation());

        // we're at 1, 1 and we don't want to fish there anymore, so we should head for 0, 0
        when(fisher.canAndWantToFishHere()).thenReturn(false);
        currentRoute.nextDestination(fisher);
        when(fisher.getLocation()).thenReturn(currentRoute.currentDestination().get());
        assertEquals(map.getSeaTile(0, 0), fisher.getLocation());

        // we're now at port, so we should have exhausted our current route
        assertFalse(currentRoute.nextDestination(fisher).isPresent());
        assertFalse(currentRoute.currentDestination().isPresent());
    }
}