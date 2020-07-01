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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Deque;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeCornerPortMap;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeRoute;

public class FadDestinationStrategyTest {

    @Test
    public void test() {
        final FishState fishState = mock(FishState.class);
        final NauticalMap map = makeCornerPortMap(3, 3);
        final Port port = map.getPorts().getFirst();
        final Fisher fisher = mock(Fisher.class);
        when(fisher.getHomePort()).thenReturn(port);

        final FadDeploymentRouteSelector fadDeploymentRouteSelector = mock(FadDeploymentRouteSelector.class);
        when(fadDeploymentRouteSelector.selectRoute(any(), anyInt(), any()))
            .thenAnswer(__ -> {
                final int[][] points = fisher.getLocation() == port.getLocation()
                    ? new int[][]{{0, 0}, {0, 1}, {0, 2}}
                    : new int[][]{{2, 2}, {1, 1}, {0, 0}};
                return Optional.of(new Route(makeRoute(map, points), fisher));
            });

        final FadSettingRouteSelector fadSettingRouteSelector = mock(FadSettingRouteSelector.class);
        when(fadSettingRouteSelector.selectRoute(any(), anyInt(), any()))
            .thenAnswer(__ -> Optional.of(new Route(makeRoute(map, new int[][]{{0, 2}, {1, 2}, {2, 2}}), fisher)));

        final FadDestinationStrategy fadDestinationStrategy =
            new FadDestinationStrategy(map, fadDeploymentRouteSelector, fadSettingRouteSelector);

        assertEquals(fadDeploymentRouteSelector, fadDestinationStrategy.getFadDeploymentRouteSelector());
        assertEquals(fadSettingRouteSelector, fadDestinationStrategy.getFadSettingRouteSelector());

        when(fisher.getLocation()).thenReturn(port.getLocation());

        final Deque<SeaTile> expectedRoute = makeRoute(map, new int[][]{
            {0, 1}, {0, 2}, // FAD deployment destination
            {1, 2}, {2, 2}, // FAD setting destination
            {1, 1}, {0, 0} //  ... and back to port
        });

        final ImmutableList.Builder<SeaTile> actualRoute = ImmutableList.builder();
        do {
            final SeaTile destination = fadDestinationStrategy.chooseDestination(fisher, null, fishState, null);
            when(fisher.getLocation()).thenReturn(destination);
            actualRoute.add(fisher.getLocation());
        } while (fisher.getLocation() != port.getLocation());
        assertEquals(expectedRoute, actualRoute.build());

    }

}