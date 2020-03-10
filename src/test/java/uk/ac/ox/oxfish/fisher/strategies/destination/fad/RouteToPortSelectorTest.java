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
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeCornerPortMap;

public class RouteToPortSelectorTest {

    @Test
    public void selectRoute() {

        NauticalMap map = makeCornerPortMap(3, 3);
        final Port port = map.getPorts().getFirst();

        final Fisher fisher = mock(Fisher.class);
        when(fisher.getHomePort()).thenReturn(port);
        when(fisher.getLocation()).thenReturn(port.getLocation());

        final RouteToPortSelector routeToPortSelector = new RouteToPortSelector(map);
        assertTrue(isSame(
            makeRoute(fisher, port.getLocation()),
            routeToPortSelector.selectRoute(fisher, 0, null)
        ));

        when(fisher.getLocation()).thenReturn(map.getSeaTile(2, 2));
        assertTrue(isSame(
            makeRoute(fisher, fisher.getLocation(), map.getSeaTile(1, 1), port.getLocation()),
            routeToPortSelector.selectRoute(fisher, 0, null)
        ));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static boolean isSame(Optional<Route> route1, Optional<Route> route2) {
        return route1
            .flatMap(r1 -> route2.filter(r2 -> r2.isSameAs(r1)))
            .isPresent();
    }

    private static Optional<Route> makeRoute(Fisher fisher, SeaTile... tiles) {
        return Optional.of(new Route(new LinkedList<>(Arrays.asList(tiles)), fisher));
    }

}