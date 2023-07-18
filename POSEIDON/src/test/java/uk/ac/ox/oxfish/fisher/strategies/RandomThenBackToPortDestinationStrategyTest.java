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

package uk.ac.ox.oxfish.fisher.strategies;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.strategies.destination.RandomThenBackToPortDestinationStrategy;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.EquirectangularDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.StraightLinePathfinder;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RandomThenBackToPortDestinationStrategyTest {


    public static FishState generateSimpleSquareMap(int size) {
        ObjectGrid2D grid2D = new ObjectGrid2D(size, size);

        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) {
                grid2D.field[i][j] = new SeaTile(i, j, -100, new TileHabitat(0d));
            }

        NauticalMap map = new NauticalMap(new GeomGridField(grid2D), new GeomVectorField(),
            new EquirectangularDistance(0.0, 1), new StraightLinePathfinder()
        );
        FishState model = mock(FishState.class);
        when(model.getMap()).thenReturn(map);
        when(model.getStepsPerDay()).thenReturn(1);
        when(model.getHoursPerStep()).thenReturn(24d);
        return model;
    }

    @Test
    public void neverPicksLand() throws Exception {


        FishState model = generateSimple2x2Map(1);
        NauticalMap map = model.getMap();


        //port at 1,1 corner
        Port port = new Port("Port 0", map.getSeaTile(1, 1), mock(MarketMap.class), 0);
        map.addPort(port);
        //create fisher
        MersenneTwisterFast random = new MersenneTwisterFast();
        /*Fisher fisher = new Fisher(0, port,
                                     random, new Anarchy(),
                                     mock(DepartingStrategy.class),
                                     mock(DestinationStrategy.class), mock(FishingStrategy.class), mock(Boat.class),
                                     mock(Hold.class), mock(Gear.class));
*/

        //choose 100 times
        Fisher fisher = mock(Fisher.class);
        when(fisher.getHomePort()).thenReturn(port);
        when(fisher.isAtPort()).thenReturn(true);
        when(fisher.isGoingToPort()).thenReturn(true);
        RandomThenBackToPortDestinationStrategy strategy = new RandomThenBackToPortDestinationStrategy();
        Action action = new AtPort();
        for (int i = 0; i < 100; i++) {
            SeaTile destination = strategy.chooseDestination(fisher,
                random, model, action
            );
            Assertions.assertEquals(destination.getGridX(), 0);
            Assertions.assertTrue(destination.isWater());
        }


    }

    public static FishState generateSimple2x2Map(final int distancePerCell) {
        ObjectGrid2D grid2D = new ObjectGrid2D(2, 2);
        //2x2, first column sea, second  column land
        SeaTile seaTile = new SeaTile(0, 0, -100, new TileHabitat(0d));
        grid2D.field[0][0] = seaTile;
        seaTile.setBiology(mock(LocalBiology.class));

        SeaTile tile2 = new SeaTile(0, 1, -100, new TileHabitat(0d));
        grid2D.field[0][1] = tile2;
        tile2.setBiology(mock(LocalBiology.class));

        grid2D.field[1][0] = new SeaTile(1, 0, 100, new TileHabitat(0d));
        grid2D.field[1][1] = new SeaTile(1, 1, 100, new TileHabitat(0d));
        //great
        NauticalMap map = new NauticalMap(new GeomGridField(grid2D), new GeomVectorField(),
            new CartesianDistance(1), new StraightLinePathfinder()
        );
        FishState model = mock(FishState.class);
        when(model.getMap()).thenReturn(map);
        when(model.getStepsPerDay()).thenReturn(distancePerCell);
        when(model.getHoursPerStep()).thenReturn(24d);
        return model;
    }

    @Test
    public void keepsGoing() throws Exception {


        FishState model = generateSimple2x2Map(1);
        NauticalMap map = model.getMap();


        //port at 1,1 corner
        Port port = new Port("Port 0", map.getSeaTile(1, 1), mock(MarketMap.class), 0);
        map.addPort(port);
        //create fisher
        MersenneTwisterFast random = new MersenneTwisterFast();
        Fisher fisher = mock(Fisher.class);
        //FISHER IS AT SEA
        when(fisher.getDestination()).thenReturn(map.getSeaTile(0, 0)); //he's going to 0,0
        when(fisher.getLocation()).thenReturn(map.getSeaTile(0, 1)); //he's at 0,1
        when(fisher.getHomePort()).thenReturn(port);

        //he should decide to keep going 0,0

        //choose 20 times
        RandomThenBackToPortDestinationStrategy strategy = new RandomThenBackToPortDestinationStrategy();
        for (int i = 0; i < 520; i++) {
            SeaTile destination = strategy.chooseDestination(fisher,
                random, model, null
            );
            Assertions.assertEquals(destination.getGridX(), 0);
            Assertions.assertEquals(destination.getGridY(), 0);
            Assertions.assertEquals(destination, fisher.getDestination());
        }


    }

    //reaches destination, should choose to go to port
    @Test
    public void goBack() throws Exception {


        FishState model = generateSimple2x2Map(1);
        NauticalMap map = model.getMap();


        //port at 1,1 corner
        Port port = new Port("Port 0", map.getSeaTile(1, 1), mock(MarketMap.class), 0);
        map.addPort(port);
        //create fisher
        MersenneTwisterFast random = new MersenneTwisterFast();
        Fisher fisher = mock(Fisher.class);
        //FISHER IS AT SEA
        when(fisher.getDestination()).thenReturn(map.getSeaTile(0, 0)); //he's going to 0,0
        when(fisher.getLocation()).thenReturn(map.getSeaTile(0, 0)); //he's arrived
        when(fisher.getHomePort()).thenReturn(port);

        //he should decide to keep going 0,0

        //choose 20 times
        RandomThenBackToPortDestinationStrategy strategy = new RandomThenBackToPortDestinationStrategy();
        for (int i = 0; i < 520; i++) {
            SeaTile destination = strategy.chooseDestination(fisher,
                random, model, null
            );
            Assertions.assertEquals(destination.getGridX(), 1);
            Assertions.assertEquals(destination.getGridY(), 1);
            Assertions.assertEquals(destination, port.getLocation());
        }


    }

}