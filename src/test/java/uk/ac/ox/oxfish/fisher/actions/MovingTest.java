package uk.ac.ox.oxfish.fisher.actions;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.fisher.*;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.strategies.RandomThenBackToPortDestinationStrategyTest;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
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
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;

import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class MovingTest
{
    @Test
    public void movingOverMultipleSteps() throws Exception {


        //2 by 2 map:
        FishState fishState = RandomThenBackToPortDestinationStrategyTest.generateSimple2x2Map(1);
        fishState.getMap().setDistance(new CartesianDistance(3)); //3 km per map
        //1 hour step
        when(fishState.getHoursPerStep()).thenReturn(1d);
        //fake port at 1,1
        Port port = new Port("Port 0", fishState.getMap().getSeaTile(1, 1), mock(MarketMap.class), 0);

        //create fisher, it wants to go to 0,1 from 1,1
        //but it only goes at 1km per hour
        //so it should take 3 steps
        Gear gear = mock(Gear.class);
        Fisher fisher = new Fisher(0, port,
                                   new MersenneTwisterFast(),
                                   new AnarchyFactory().apply(fishState),
                                   new FixedProbabilityDepartingStrategy(1.0, false),
                                   new FavoriteDestinationStrategy(fishState.getMap().getSeaTile(0, 1)),
                                   new FishingStrategy() {
                                       @Override
                                       public boolean shouldFish(
                                               FisherEquipment equipment, FisherStatus status,
                                               FisherMemory memory, MersenneTwisterFast random,
                                               FishState model) {
                                           return true;
                                       }

                                       @Override
                                       public void start(FishState model,Fisher fisher) {

                                       }

                                       @Override
                                       public void turnOff(Fisher fisher) {

                                       }
                                   }, new FixedGearStrategy(),
                                   new IgnoreWeatherStrategy(),
                                   new Boat(1,1,new Engine(1,1,1),new FuelTank(1000000)), new Hold(100.0, 1), gear, 1);
        fisher.start(mock(FishState.class));
        //starts at port!
        assertEquals(fishState.getMap().getSeaTile(1, 1), fisher.getLocation());

        fisher.step(fishState);
        //still at port
        assertEquals(fishState.getMap().getSeaTile(1,1),fisher.getLocation());

        //one more step, still at port!
        fisher.step(fishState);
        assertEquals(fishState.getMap().getSeaTile(1,1),fisher.getLocation());

        //final step, gooone!
        fisher.step(fishState);
        assertEquals(fishState.getMap().getSeaTile(0,1),fisher.getLocation());


    }




//path from A to A is empty


    @Test
    public void pathToItselfIsOne() throws Exception {

        FishState simple = generateSimple4x4Map();
        Moving move = new Moving();

        NauticalMap map = simple.getMap();
        Queue<SeaTile> route = map.getRoute(map.getSeaTile(2, 2), map.getSeaTile(2, 2));
        assertTrue(route.size()==1);


    }

    @Test
    public void moveInPlace() throws Exception {

        FishState simple = generateSimple4x4Map();
        Moving move = new Moving();
        NauticalMap map = simple.getMap();

        Fisher fisher = mock(Fisher.class);
        when(fisher.getDestination()).thenReturn(map.getSeaTile(0,0));
        when(fisher.getLocation()).thenReturn(map.getSeaTile(0, 0));

        ActionResult result = move.act(simple, fisher, new Anarchy(),24 );
        verify(fisher,never()).move(any(),any(),any(), anyDouble()); //never moved
        assertTrue(result.isActAgainThisTurn()); //think he has arrived
        assertTrue(result.getNextState() instanceof Arriving);


    }

    @Test
    public void moveAllTheWay() throws Exception {
        FishState simple = generateSimple4x4Map();
        Moving move = new Moving();
        NauticalMap map = simple.getMap();
        map.setDistance(new CartesianDistance(1.0));

        //lots of crap to initialize.
        Port port = mock(Port.class); when(port.getLocation()).thenReturn(map.getSeaTile(0, 0));
        DestinationStrategy strategy = mock(DestinationStrategy.class);
        when(strategy.chooseDestination( any(),
                                         any(), any(), any())).thenReturn(map.getSeaTile(2, 0));

        Fisher fisher = new Fisher(0, port,
                                   new MersenneTwisterFast(), new Anarchy(),
                                   mock(DepartingStrategy.class),
                                   strategy, mock(FishingStrategy.class),
                                   mock(GearStrategy.class),


                                   new IgnoreWeatherStrategy(),


                                   new Boat(1,1,new Engine(1,1,.1),new FuelTank(1000000)),
                                   mock(Hold.class), mock(Gear.class), 1);
        fisher.start(mock(FishState.class));

        //should move and spend 20 hours doing so
        fisher.undock();
        move.act(simple, fisher, new Anarchy(),24);
        assertEquals(fisher.getHoursTravelledToday(), 20, .001);
        assertEquals(fisher.getLocation(), map.getSeaTile(2, 0));

    }


    @Test
    public void movePartially() throws Exception {
        FishState simple = generateSimple4x4Map();
        Moving move = new Moving();
        NauticalMap map = simple.getMap();
        map.setDistance(new CartesianDistance(2.0));

        //lots of crap to initialize.
        Port port = mock(Port.class); when(port.getLocation()).thenReturn(map.getSeaTile(0, 0));
        DestinationStrategy strategy = mock(DestinationStrategy.class);
        when(strategy.chooseDestination(any(), any(), any(), any())).thenReturn(map.getSeaTile(2, 0));
        Fisher fisher = new Fisher(0, port, new MersenneTwisterFast(), new Anarchy(),
                                   mock(DepartingStrategy.class), strategy,
                                   mock(FishingStrategy.class),  mock(GearStrategy.class),
                                   new IgnoreWeatherStrategy(),
                                   new Boat(1,1,new Engine(1,1,.1),new FuelTank(1000000)), mock(Hold.class),
                                   mock(Gear.class), 1);

        fisher.start(mock(FishState.class));
        fisher.undock();

        //should move and spend 20 hours doing so
        move.act(simple,fisher,new Anarchy() ,24);
        assertEquals(fisher.getHoursTravelledToday(), 20, .001);
        assertEquals(fisher.getLocation(),map.getSeaTile(1, 0));

    }

    @Test
    public void simpleHorizontalPath() throws Exception {

        FishState simple = generateSimple4x4Map();
        Moving move = new Moving();

        NauticalMap map = simple.getMap();
        Queue<SeaTile> route = map.getRoute(map.getSeaTile(0, 2), map.getSeaTile(2, 2));
        route.poll(); //ignore start

        assertEquals(route.size(),2);
        assertEquals(route.poll(),map.getSeaTile(1,2));
        assertEquals(route.poll(), map.getSeaTile(2, 2));


    }


    @Test
    public void simpleVerticalPath() throws Exception {

        FishState simple = generateSimple4x4Map();
        Moving move = new Moving();

        NauticalMap map = simple.getMap();
        Queue<SeaTile> route = map.getRoute(map.getSeaTile(2, 0), map.getSeaTile(2, 2));
        route.poll(); //ignore start

        assertEquals(route.size(),2);
        assertEquals(route.poll(),map.getSeaTile(2, 1));
        assertEquals(route.poll(),map.getSeaTile(2, 2));


    }


    @Test
    public void simpleDiagonalPath() throws Exception {

        FishState simple = generateSimple4x4Map();
        Moving move = new Moving();

        NauticalMap map = simple.getMap();
        Queue<SeaTile> route = map.getRoute(map.getSeaTile(0, 0), map.getSeaTile(2, 2));
        route.poll(); //ignore start

        assertEquals(route.size(),2);
        assertEquals(route.poll(),map.getSeaTile(1, 1));
        assertEquals(route.poll(),map.getSeaTile(2, 2));


    }

    @Test
    public void diagonalFirstPath(){
        FishState simple = generateSimple4x4Map();
        Moving move = new Moving();

        NauticalMap map = simple.getMap();
        Queue<SeaTile> route = map.getRoute( map.getSeaTile(0, 0), map.getSeaTile(2, 3));
        route.poll(); //ignore start
        assertEquals(route.size(),3);
        assertEquals(route.poll(),map.getSeaTile(1, 1));
        assertEquals(route.poll(),map.getSeaTile(2, 2));
        assertEquals(route.poll(),map.getSeaTile(2, 3));
    }

    //all sea tiles!
    public static FishState generateSimple4x4Map() {
        ObjectGrid2D grid2D = new ObjectGrid2D(4,4);
        //2x2, first column sea, second  column land
        for(int i=0;i<4;i++)
            for(int j=0;j<4;j++)
                grid2D.field[i][j] = new SeaTile(i,j,-100, new TileHabitat(0d));

        //great
        NauticalMap map = new NauticalMap(new GeomGridField(grid2D),new GeomVectorField(),
                                          new EquirectangularDistance(0.0,1), new StraightLinePathfinder());
        FishState model = mock(FishState.class);
        when(model.getMap()).thenReturn(map);
        when(model.getStepsPerDay()).thenReturn(1);
        when(model.getHoursPerStep()).thenReturn(24d);
        return model;
    }

    //all sea tiles!
    public static FishState generateSimple50x50Map() {
        ObjectGrid2D grid2D = new ObjectGrid2D(50,50);
        //2x2, first column sea, second  column land
        for(int i=0;i<50;i++)
            for(int j=0;j<50;j++)
                grid2D.field[i][j] = new SeaTile(i,j,-100, new TileHabitat(0d));

        //great
        NauticalMap map = new NauticalMap(new GeomGridField(grid2D),new GeomVectorField(),
                                          new EquirectangularDistance(0.0,1), new StraightLinePathfinder());
        FishState model = mock(FishState.class);
        when(model.getMap()).thenReturn(map);
        when(model.getStepsPerDay()).thenReturn(1);
        when(model.getHoursPerStep()).thenReturn(24d);
        return model;
    }


    //all sea tiles!
    public static FishState generateSimple10x10Map() {
        ObjectGrid2D grid2D = new ObjectGrid2D(10,10);
        //2x2, first column sea, second  column land
        for(int i=0;i<10;i++)
            for(int j=0;j<10;j++)
                grid2D.field[i][j] = new SeaTile(i,j,-100, new TileHabitat(0d));

        //great
        NauticalMap map = new NauticalMap(new GeomGridField(grid2D),new GeomVectorField(),
                                          new EquirectangularDistance(0.0,1), new StraightLinePathfinder());
        FishState model = mock(FishState.class);
        when(model.getMap()).thenReturn(map);
        when(model.getStepsPerDay()).thenReturn(1);
        when(model.getHoursPerStep()).thenReturn(24d);
        return model;
    }



}