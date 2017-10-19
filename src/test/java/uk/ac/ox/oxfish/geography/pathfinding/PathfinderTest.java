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

import org.junit.Test;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

import java.util.Deque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 11/4/15.
 */
public class PathfinderTest {


    @Test
    public void simplePathfindingTest() throws Exception {


        ObjectGrid2D grid2D = new ObjectGrid2D(3,3);
        //3x3 map, where  the middle column (0,1 and 1,1 but not 2,1) has land in it
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                if(j == 1 && i<2)
                    grid2D.field[i][j] = new SeaTile(i,j,100, new TileHabitat(0d));
                else
                    grid2D.field[i][j] = new SeaTile(i,j,-100, new TileHabitat(0d));

        //great
        NauticalMap map = new NauticalMap(new GeomGridField(grid2D),new GeomVectorField(),
                                          new CartesianDistance(1),mock(Pathfinder.class));


        //we want to go from 0,0 to 0,2
        SeaTile start = map.getSeaTile(0,0);
        SeaTile end = map.getSeaTile(0,2);

        //start with direct pathfinder
        StraightLinePathfinder liner = new StraightLinePathfinder();
        Deque<SeaTile> route = liner.getRoute(map, start, end);
        //it should be very short
        assertEquals(route.peekFirst(), start);
        assertEquals(route.peekLast(),end);
        assertEquals(route.size(), 3);
        route.poll();
        assertEquals(route.peekFirst(),map.getSeaTile(0,1)); //it paths overland



        //now let's do BreadthFirst
        BreadthFirstPathfinder breadthFirst = new BreadthFirstPathfinder();
        route = breadthFirst.getRoute(map, start, end);
        //it should be very short
        assertEquals(route.peekFirst(), start);
        assertEquals(route.peekLast(),end);
        assertTrue(route.size() > 3);
        assertTrue(!route.contains(map.getSeaTile(0,1)));
        assertTrue(!route.contains(map.getSeaTile(1,1)));
        route.poll();


        //let's do A*
        AStarPathfinder star = new AStarPathfinder(new CartesianDistance(1));
        route = star.getRoute(map, start, end);
        //it should be very short
        assertEquals(route.peekFirst(), start);
        assertEquals(route.peekLast(),end);
        assertEquals(route.size(), 5);
        assertTrue(!route.contains(map.getSeaTile(0,1)));
        assertTrue(!route.contains(map.getSeaTile(1,1)));
        route.poll();
    }
}