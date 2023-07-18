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

package uk.ac.ox.oxfish.fisher.actions;

import com.vividsolutions.jts.geom.Envelope;
import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.RandomThenBackToPortDestinationStrategyTest;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscarding;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.FixedGearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.IgnoreWeatherStrategy;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.EquirectangularDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.StraightLinePathfinder;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;

import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class MovingTest {
    //all sea tiles!
    public static FishState generateSimple50x50Map() {
        final ObjectGrid2D grid2D = new ObjectGrid2D(50, 50);
        //2x2, first column sea, second  column land
        for (int i = 0; i < 50; i++)
            for (int j = 0; j < 50; j++)
                grid2D.field[i][j] = new SeaTile(i, j, -100, new TileHabitat(0d));

        //great
        final NauticalMap map = new NauticalMap(new GeomGridField(grid2D), new GeomVectorField(),
            new EquirectangularDistance(0.0, 1), new StraightLinePathfinder()
        );
        return initModel(map);
    }


//path from A to A is empty

    private static FishState initModel(final NauticalMap map) {
        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getMap()).thenReturn(map);
        when(model.getStepsPerDay()).thenReturn(1);
        when(model.getHoursPerStep()).thenReturn(24d);
        return model;
    }

    //all sea tiles!
    public static FishState generateSimple10x10MapWithVaryingDepth() {
        final ObjectGrid2D grid2D = new ObjectGrid2D(10, 10);
        //2x2, first column sea, second  column land
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                grid2D.field[i][j] = new SeaTile(i, j, -100 - i - j, new TileHabitat(0d));

        //great
        final NauticalMap map = new NauticalMap(new GeomGridField(grid2D), new GeomVectorField(),
            new EquirectangularDistance(0.0, 1), new StraightLinePathfinder()
        );
        return initModel(map);
    }

    @Test
    public void movingOverMultipleSteps() {


        //2 by 2 map:
        final FishState fishState = RandomThenBackToPortDestinationStrategyTest.generateSimple2x2Map(1);
        when(fishState.getRandom()).thenReturn(new MersenneTwisterFast());

        fishState.getMap().setDistance(new CartesianDistance(3)); //3 km per map
        //1 hour step
        when(fishState.getHoursPerStep()).thenReturn(1d);
        //fake port at 1,1
        final Port port = new Port("Port 0", fishState.getMap().getSeaTile(1, 1), mock(MarketMap.class), 0);

        //create fisher, it wants to go to 0,1 from 1,1
        //but it only goes at 1km per hour
        //so it should take 3 steps
        final Gear gear = mock(Gear.class);
        final Fisher fisher = new Fisher(0, port,
            new MersenneTwisterFast(),
            new AnarchyFactory().apply(fishState),
            new FixedProbabilityDepartingStrategy(1.0, false),
            new FavoriteDestinationStrategy(fishState.getMap().getSeaTile(0, 1)),
            new FishingStrategy() {
                @Override
                public boolean shouldFish(
                    final Fisher fisher,
                    final MersenneTwisterFast random,
                    final FishState model,
                    final TripRecord currentTrip
                ) {
                    return true;
                }

                @Override
                public void start(final FishState model, final Fisher fisher) {

                }

                @Override
                public void turnOff(final Fisher fisher) {

                }
            }, new FixedGearStrategy(),
            new NoDiscarding(),
            new IgnoreWeatherStrategy(),
            new Boat(1, 1, new Engine(1, 1, 1),
                new FuelTank(1000000)
            ),
            new Hold(100.0, new GlobalBiology(mock(Species.class))), gear,
            1
        );
        fisher.start(mock(FishState.class));
        //starts at port!
        assertEquals(fishState.getMap().getSeaTile(1, 1), fisher.getLocation());

        fisher.step(fishState);
        //still at port
        assertEquals(fishState.getMap().getSeaTile(1, 1), fisher.getLocation());

        //one more step, still at port!
        fisher.step(fishState);
        assertEquals(fishState.getMap().getSeaTile(1, 1), fisher.getLocation());

        //final step, gooone!
        fisher.step(fishState);
        assertEquals(fishState.getMap().getSeaTile(0, 1), fisher.getLocation());


    }

    @Test
    public void pathToItselfIsOne() {
        final FishState simple = generateSimple4x4Map();
        final NauticalMap map = simple.getMap();
        final Queue<SeaTile> route = map.getRoute(map.getSeaTile(2, 2), map.getSeaTile(2, 2));
        assertEquals(1, route.size());
    }

    //all sea tiles!
    public static FishState generateSimple4x4Map() {
        final ObjectGrid2D grid2D = new ObjectGrid2D(4, 4);
        //2x2, first column sea, second  column land
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                grid2D.field[i][j] = new SeaTile(i, j, -100, new TileHabitat(0d));

        final GeomGridField rasterBathymetry = new GeomGridField(grid2D);
        rasterBathymetry.setMBR(new Envelope(0, 1, 0, 1));

        //great
        final NauticalMap map = new NauticalMap(rasterBathymetry, new GeomVectorField(),
            new EquirectangularDistance(0.0, 1), new StraightLinePathfinder()
        );
        return initModel(map);
    }

    @Test
    public void moveInPlace() {

        final FishState simple = generateSimple4x4Map();
        final Moving move = new Moving();
        final NauticalMap map = simple.getMap();

        final Fisher fisher = mock(Fisher.class);
        when(fisher.getDestination()).thenReturn(map.getSeaTile(0, 0));
        when(fisher.getLocation()).thenReturn(map.getSeaTile(0, 0));

        final ActionResult result = move.act(simple, fisher, new Anarchy(), 24);
        verify(fisher, never()).move(any(), any(), any(), anyDouble()); //never moved
        assertTrue(result.isActAgainThisTurn()); //think he has arrived
        assertTrue(result.getNextState() instanceof Arriving);


    }

    @Test
    public void moveAllTheWay() {
        final FishState simple = generateSimple4x4Map();
        final Moving move = new Moving();
        final NauticalMap map = simple.getMap();
        map.setDistance(new CartesianDistance(1.0));
        final Fisher fisher = initFisher(map);

        //should move and spend 20 hours doing so
        fisher.undock();
        move.act(simple, fisher, new Anarchy(), 24);
        assertEquals(fisher.getHoursTravelledToday(), 20, .001);
        assertEquals(fisher.getLocation(), map.getSeaTile(2, 0));

    }

    private Fisher initFisher(final NauticalMap map) {
        //lots of crap to initialize.
        final Port port = mock(Port.class);
        when(port.getLocation()).thenReturn(map.getSeaTile(0, 0));
        final DestinationStrategy strategy = mock(DestinationStrategy.class);
        when(strategy.chooseDestination(any(),
            any(), any(), any()
        )).thenReturn(map.getSeaTile(2, 0));

        final Fisher fisher = new Fisher(0, port,
            new MersenneTwisterFast(), new Anarchy(),
            mock(DepartingStrategy.class),
            strategy, mock(FishingStrategy.class),
            mock(GearStrategy.class),
            mock(DiscardingStrategy.class),
            new IgnoreWeatherStrategy(),
            new Boat(1, 1, new Engine(1, 1, .1),
                new FuelTank(1000000)
            ), mock(Hold.class),
            mock(Gear.class), 1
        );
        fisher.start(mock(FishState.class));
        return fisher;
    }

    @Test
    public void movePartially() {
        final FishState simple = generateSimple4x4Map();
        final Moving move = new Moving();
        final NauticalMap map = simple.getMap();
        map.setDistance(new CartesianDistance(2.0));

        final Fisher fisher = initFisher(map);
        fisher.undock();

        //should move and spend 20 hours doing so
        move.act(simple, fisher, new Anarchy(), 24);
        assertEquals(fisher.getHoursTravelledToday(), 20, .001);
        assertEquals(fisher.getLocation(), map.getSeaTile(1, 0));

    }

    @Test
    public void simpleHorizontalPath() {

        final FishState simple = generateSimple4x4Map();
        final NauticalMap map = simple.getMap();
        final Queue<SeaTile> route = map.getRoute(map.getSeaTile(0, 2), map.getSeaTile(2, 2));
        route.poll(); //ignore start

        assertEquals(route.size(), 2);
        assertEquals(route.poll(), map.getSeaTile(1, 2));
        assertEquals(route.poll(), map.getSeaTile(2, 2));
    }

    @Test
    public void simpleVerticalPath() {

        final FishState simple = generateSimple4x4Map();
        final NauticalMap map = simple.getMap();
        final Queue<SeaTile> route = map.getRoute(map.getSeaTile(2, 0), map.getSeaTile(2, 2));
        route.poll(); //ignore start

        assertEquals(route.size(), 2);
        assertEquals(route.poll(), map.getSeaTile(2, 1));
        assertEquals(route.poll(), map.getSeaTile(2, 2));


    }

    @Test
    public void simpleDiagonalPath() {

        final FishState simple = generateSimple4x4Map();
        final NauticalMap map = simple.getMap();
        final Queue<SeaTile> route = map.getRoute(map.getSeaTile(0, 0), map.getSeaTile(2, 2));
        route.poll(); //ignore start

        assertEquals(route.size(), 2);
        assertEquals(route.poll(), map.getSeaTile(1, 1));
        assertEquals(route.poll(), map.getSeaTile(2, 2));


    }

    @Test
    public void diagonalFirstPath() {
        final FishState simple = generateSimple4x4Map();
        final NauticalMap map = simple.getMap();
        final Queue<SeaTile> route = map.getRoute(map.getSeaTile(0, 0), map.getSeaTile(2, 3));
        route.poll(); //ignore start
        assertEquals(route.size(), 3);
        assertEquals(route.poll(), map.getSeaTile(1, 1));
        assertEquals(route.poll(), map.getSeaTile(2, 2));
        assertEquals(route.poll(), map.getSeaTile(2, 3));
    }

}