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

package uk.ac.ox.oxfish.geography.pathfinding;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;

import static com.google.common.collect.Iterables.elementsEqual;
import static org.junit.Assert.*;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

/**
 * Created by carrknight on 11/4/15.
 */
public class PathfinderTest {

    @Test
    public void simplePathfindingTest() {

        //3x3 map, where  the middle column (0,1 and 1,1 but not 2,1) has land in it
        NauticalMap map = makeMap(new int[][]{
            {-1, 10, -1},
            {-1, 10, -1},
            {-1, -1, -1}
        });

        //we want to go from 0,0 to 0,2
        SeaTile start = map.getSeaTile(0, 0);
        SeaTile end = map.getSeaTile(0, 2);

        //start with direct pathfinder
        StraightLinePathfinder liner = new StraightLinePathfinder();
        Deque<SeaTile> route = liner.getRoute(map, start, end);
        //it should be very short
        assertEquals(route.peekFirst(), start);
        assertEquals(route.peekLast(), end);
        assertEquals(route.size(), 3);
        route.poll();
        assertEquals(route.peekFirst(), map.getSeaTile(0, 1)); //it paths overland

        //now let's do BreadthFirst
        BreadthFirstPathfinder breadthFirst = new BreadthFirstPathfinder();
        route = breadthFirst.getRoute(map, start, end);
        //it should be very short
        assertEquals(route.peekFirst(), start);
        assertEquals(route.peekLast(), end);
        assertTrue(route.size() > 3);
        assertFalse(route.contains(map.getSeaTile(0, 1)));
        assertFalse(route.contains(map.getSeaTile(1, 1)));
        route.poll();

        //let's do A*
        AStarPathfinder star = new AStarPathfinder(new CartesianDistance(1));
        route = star.getRoute(map, start, end);
        //it should be very short
        assertEquals(route.peekFirst(), start);
        assertEquals(route.peekLast(), end);
        assertEquals(route.size(), 5);
        assertFalse(route.contains(map.getSeaTile(0, 1)));
        assertFalse(route.contains(map.getSeaTile(1, 1)));
        route.poll();
    }

    @Test
    public void fallbackPathfindingTest() {

        NauticalMap map = makeMap(new int[][]{
            {-1, 10, -1},
            {-1, 10, -1},
            {-1, -1, -1}
        });
        final SeaTile topLeft = map.getSeaTile(0, 0);
        final SeaTile bottomLeft = map.getSeaTile(2, 0);
        final SeaTile topRight = map.getSeaTile(0, 2);

        final CartesianDistance distance = new CartesianDistance(1);
        final AStarFallbackPathfinder fallbackPathfinder = new AStarFallbackPathfinder(distance);
        final AStarPathfinder aStarPathfinder = new AStarPathfinder(distance);
        final BreadthFirstPathfinder breadthFirstPathfinder = new BreadthFirstPathfinder();
        final StraightLinePathfinder straightLinePathfinder = new StraightLinePathfinder();

        // Straight line from top left to bottom left
        final ImmutableList<SeaTile> expectedStraightRoute = ImmutableList.of(
            topLeft, map.getSeaTile(1, 0), bottomLeft
        );
        final Deque<SeaTile> straightRoute = fallbackPathfinder.getRoute(map, topLeft, bottomLeft);
        assertTrue(elementsEqual(straightRoute, expectedStraightRoute));
        assertTrue(elementsEqual(straightRoute, aStarPathfinder.getRoute(map, topLeft, bottomLeft)));
        assertTrue(elementsEqual(straightRoute, breadthFirstPathfinder.getRoute(map, topLeft, bottomLeft)));
        assertTrue(elementsEqual(straightRoute, straightLinePathfinder.getRoute(map, topLeft, bottomLeft)));

        // Route around land from top left to top right
        final Deque<SeaTile> routeAround = fallbackPathfinder.getRoute(map, topLeft, topRight);
        final ImmutableList<SeaTile> expectedRouteAround = ImmutableList.of(
            topLeft,
            map.getSeaTile(1, 0),
            map.getSeaTile(2, 1),
            map.getSeaTile(1, 2),
            topRight
        );
        assertTrue(elementsEqual(routeAround, expectedRouteAround));
        assertTrue(elementsEqual(routeAround, aStarPathfinder.getRoute(map, topLeft, topRight)));
        assertTrue(elementsEqual(routeAround, breadthFirstPathfinder.getRoute(map, topLeft, topRight)));
        assertFalse(elementsEqual(routeAround, straightLinePathfinder.getRoute(map, topLeft, topRight)));

        // Fallback and aStar (but not StraightLine) should return null if the map is impassable
        // (BreadthFirstPathFinder would stall, so we're not testing it)
        NauticalMap impassableMap = makeMap(new int[][]{
            {-1, 10, -1},
            {-1, 10, -1},
            {-1, 10, -1}
        });
        final SeaTile start = impassableMap.getSeaTile(0, 0);
        final SeaTile end = impassableMap.getSeaTile(0, 2);
        assertNull(fallbackPathfinder.getRoute(impassableMap, start, end));
        assertNull(aStarPathfinder.getRoute(impassableMap, start, end));
        assertNotNull(straightLinePathfinder.getRoute(impassableMap, start, end));
    }

}