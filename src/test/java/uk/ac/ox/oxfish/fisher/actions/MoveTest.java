package uk.ac.ox.oxfish.fisher.actions;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.strategies.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.DestinationStrategy;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.EquirectangularDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Queue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class MoveTest
{



    //path from A to A is empty


    @Test
    public void pathToItselfIsEmpty() throws Exception {

        FishState simple = generateSimple4x4Map();
        Move move = new Move();

        NauticalMap map = simple.getMap();
        Queue<SeaTile> route = move.getRoute(map, map.getSeaTile(2, 2), map.getSeaTile(2, 2));
        assertTrue(route.isEmpty());


    }

    @Test
    public void moveInPlace() throws Exception {

        FishState simple = generateSimple4x4Map();
        Move move = new Move();
        NauticalMap map = simple.getMap();

        Fisher fisher = mock(Fisher.class);
        when(fisher.getDestination()).thenReturn(map.getSeaTile(0,0));
        when(fisher.getLocation()).thenReturn(map.getSeaTile(0, 0));

        ActionResult result = move.act(simple, fisher);
        verify(fisher,never()).move(any(),any()); //never moved
        assertTrue(result.isActAgainThisTurn()); //think he has arrived
        assertTrue(result.getNextState() instanceof Arrived);


    }

    @Test
    public void moveAllTheWay() throws Exception {
        FishState simple = generateSimple4x4Map();
        Move move = new Move();
        NauticalMap map = simple.getMap();
        map.setDistance(new CartesianDistance(1.0));

        //lots of crap to initialize.
        Port port = mock(Port.class); when(port.getLocation()).thenReturn(map.getSeaTile(0, 0));
        DestinationStrategy strategy = mock(DestinationStrategy.class);
        when(strategy.chooseDestination(any(),any(),any(),any())).thenReturn(map.getSeaTile(2, 0));

        Fisher fisher = new Fisher(port,new MersenneTwisterFast(),
                                   mock(DepartingStrategy.class),
                                   strategy,
                                   new Boat(0.1) );

        //should move and spend 20 hours doing so
        move.act(simple, fisher);
        assertEquals(fisher.getHoursTravelledToday(), 20, .001);
        assertEquals(fisher.getLocation(), map.getSeaTile(2, 0));

    }


    @Test
    public void movePartially() throws Exception {
        FishState simple = generateSimple4x4Map();
        Move move = new Move();
        NauticalMap map = simple.getMap();
        map.setDistance(new CartesianDistance(2.0));

        //lots of crap to initialize.
        Port port = mock(Port.class); when(port.getLocation()).thenReturn(map.getSeaTile(0, 0));
        DestinationStrategy strategy = mock(DestinationStrategy.class);
        when(strategy.chooseDestination(any(), any(), any(), any())).thenReturn(map.getSeaTile(2, 0));
        Fisher fisher = new Fisher(port,new MersenneTwisterFast(), null, strategy,new Boat(0.1) );


        //should move and spend 20 hours doing so
        move.act(simple,fisher);
        assertEquals(fisher.getHoursTravelledToday(), 20, .001);
        assertEquals(fisher.getLocation(),map.getSeaTile(1, 0));

    }

    @Test
    public void simpleHorizontalPath() throws Exception {

        FishState simple = generateSimple4x4Map();
        Move move = new Move();

        NauticalMap map = simple.getMap();
        Queue<SeaTile> route = move.getRoute(map, map.getSeaTile(0, 2), map.getSeaTile(2, 2));
        assertEquals(route.size(),2);
        assertEquals(route.poll(),map.getSeaTile(1,2));
        assertEquals(route.poll(), map.getSeaTile(2, 2));


    }


    @Test
    public void simpleVerticalPath() throws Exception {

        FishState simple = generateSimple4x4Map();
        Move move = new Move();

        NauticalMap map = simple.getMap();
        Queue<SeaTile> route = move.getRoute(map, map.getSeaTile(2, 0), map.getSeaTile(2, 2));
        assertEquals(route.size(),2);
        assertEquals(route.poll(),map.getSeaTile(2, 1));
        assertEquals(route.poll(),map.getSeaTile(2, 2));


    }


    @Test
    public void simpleDiagonalPath() throws Exception {

        FishState simple = generateSimple4x4Map();
        Move move = new Move();

        NauticalMap map = simple.getMap();
        Queue<SeaTile> route = move.getRoute(map, map.getSeaTile(0, 0), map.getSeaTile(2, 2));
        assertEquals(route.size(),2);
        assertEquals(route.poll(),map.getSeaTile(1, 1));
        assertEquals(route.poll(),map.getSeaTile(2, 2));


    }

    @Test
    public void diagonalFirstPath(){
        FishState simple = generateSimple4x4Map();
        Move move = new Move();

        NauticalMap map = simple.getMap();
        Queue<SeaTile> route = move.getRoute(map, map.getSeaTile(0, 0), map.getSeaTile(2, 3));
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
                grid2D.field[i][j] = new SeaTile(i,j,-100);

        //great
        NauticalMap map = new NauticalMap(new GeomGridField(grid2D),new GeomVectorField(),
                                          new EquirectangularDistance(0.0,1));
        FishState model = mock(FishState.class);
        when(model.getMap()).thenReturn(map);
        return model;
    }



}