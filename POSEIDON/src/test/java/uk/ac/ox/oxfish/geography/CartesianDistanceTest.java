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

package uk.ac.ox.oxfish.geography;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.geography.pathfinding.Pathfinder;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;

import static com.google.common.collect.Iterables.get;

public class CartesianDistanceTest {

    @Test
    public void simpleDistances() throws Exception {

        CartesianDistance distance = new CartesianDistance(1);

        Assertions.assertEquals(distance.distance(0, 0, 1, 1), Math.sqrt(2), .001);

        distance = new CartesianDistance(2);

        Assertions.assertEquals(distance.distance(0, 2, 0, 0), 4, .001);

    }

    @Test
    public void testDistancesAlongRoute() {

        final CartesianDistance distance = new CartesianDistance(1);
        final NauticalMap map = TestUtilities.makeMap(3, 3, -1);
        final Pathfinder pathfinder = new AStarFallbackPathfinder(distance);
        final SeaTile startTile = map.getSeaTile(0, 0);

        final Deque<SeaTile> straightRoute = pathfinder.getRoute(map, startTile, map.getSeaTile(0, 2));
        checkSecondValueOfEntries(distance.cumulativeDistanceAlongRouteInKm(straightRoute, map), 0.0, 1.0, 2.0);

        final Deque<SeaTile> diagonalRoute = pathfinder.getRoute(map, startTile, map.getSeaTile(2, 2));
        checkSecondValueOfEntries(distance.cumulativeDistanceAlongRouteInKm(diagonalRoute, map), 0.0, 1.41, 2.82);

        for (final SeaTile endTile : map.getAllSeaTilesAsList()) {
            // First value of cumulative distance should always be zero
            final Deque<SeaTile> route = pathfinder.getRoute(map, startTile, endTile);
            final List<Entry<SeaTile, Double>> cumDist = distance.cumulativeDistanceAlongRouteInKm(route, map);
            Assertions.assertEquals(0.0, get(cumDist, 0).getValue(), 0.01);
        }
    }

    private <T> void checkSecondValueOfEntries(final Collection<Entry<T, Double>> pairs, final double... values) {
        Assertions.assertArrayEquals(values, pairs.stream().mapToDouble(Entry::getValue).toArray(), 0.01);
    }
}
