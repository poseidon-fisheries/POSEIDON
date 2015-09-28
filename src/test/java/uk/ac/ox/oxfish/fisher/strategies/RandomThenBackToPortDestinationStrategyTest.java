package uk.ac.ox.oxfish.fisher.strategies;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.strategies.destination.RandomThenBackToPortDestinationStrategy;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.EquirectangularDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.regs.Anarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RandomThenBackToPortDestinationStrategyTest {


    @Test
    public void neverPicksLand() throws Exception {


        FishState model = generateSimple2x2Map(1);
        NauticalMap map = model.getMap();


        //port at 1,1 corner
        Port port = new Port(map.getSeaTile(1, 1), mock(MarketMap.class), 0);
        map.addPort(port);
        //create fisher
        MersenneTwisterFast random = new MersenneTwisterFast();
        /*Fisher fisher = new Fisher(0, port,
                                     random, new Anarchy(),
                                     mock(DepartingStrategy.class),
                                     mock(DestinationStrategy.class), mock(FishingStrategy.class), mock(Boat.class),
                                     mock(Hold.class), mock(Gear.class));
*/
        FisherStatus status = new FisherStatus(random,new Anarchy(),port);

        //choose 100 times
        RandomThenBackToPortDestinationStrategy strategy = new RandomThenBackToPortDestinationStrategy();
        Action action = new AtPort();
        for(int i=0; i<100; i++)
        {
            SeaTile destination = strategy.chooseDestination(mock(FisherEquipment.class),
                    status,mock(FisherMemory.class),random, model, action);
            assertEquals(destination.getGridX(),0);
            assertTrue(destination.getAltitude() < 0);
        }


    }


    @Test
    public void keepsGoing() throws Exception {


        FishState model = generateSimple2x2Map(1);
        NauticalMap map = model.getMap();


        //port at 1,1 corner
        Port port = new Port(map.getSeaTile(1, 1),mock(MarketMap.class), 0);
        map.addPort(port);
        //create fisher
        MersenneTwisterFast random = new MersenneTwisterFast();
        FisherStatus status = mock(FisherStatus.class);
        //FISHER IS AT SEA
        when(status.getDestination()).thenReturn(map.getSeaTile(0,0)); //he's going to 0,0
        when(status.getLocation()).thenReturn(map.getSeaTile(0,1)); //he's at 0,1
        when(status.getHomePort()).thenReturn(port);

        //he should decide to keep going 0,0

        //choose 20 times
        RandomThenBackToPortDestinationStrategy strategy = new RandomThenBackToPortDestinationStrategy();
        for(int i=0; i<520; i++)
        {
            SeaTile destination = strategy.chooseDestination(mock(FisherEquipment.class),
                    status,mock(FisherMemory.class), random, model, null);
            assertEquals(destination.getGridX(),0);
            assertEquals(destination.getGridY(),0);
            assertEquals(destination, status.getDestination());
        }


    }


    //reaches destination, should choose to go to port
    @Test
    public void goBack() throws Exception {


        FishState model = generateSimple2x2Map(1);
        NauticalMap map = model.getMap();


        //port at 1,1 corner
        Port port = new Port(map.getSeaTile(1, 1),mock(MarketMap.class), 0);
        map.addPort(port);
        //create fisher
        MersenneTwisterFast random = new MersenneTwisterFast();
        FisherStatus status = mock(FisherStatus.class);
        //FISHER IS AT SEA
        when(status.getDestination()).thenReturn(map.getSeaTile(0,0)); //he's going to 0,0
        when(status.getLocation()).thenReturn(map.getSeaTile(0,0)); //he's arrived
        when(status.getHomePort()).thenReturn(port);

        //he should decide to keep going 0,0

        //choose 20 times
        RandomThenBackToPortDestinationStrategy strategy = new RandomThenBackToPortDestinationStrategy();
        for(int i=0; i<520; i++)
        {
            SeaTile destination = strategy.chooseDestination(mock(FisherEquipment.class),
                                                             status,mock(FisherMemory.class), random, model, null);
            assertEquals(destination.getGridX(),1);
            assertEquals(destination.getGridY(),1);
            assertEquals(destination,port.getLocation());
        }


    }
    public static FishState generateSimple2x2Map(final int distancePerCell) {
        ObjectGrid2D grid2D = new ObjectGrid2D(2,2);
        //2x2, first column sea, second  column land
        grid2D.field[0][0] = new SeaTile(0,0,-100, new TileHabitat(0d));
        grid2D.field[0][1] = new SeaTile(0,1,-100, new TileHabitat(0d));
        grid2D.field[1][0] = new SeaTile(1,0,100, new TileHabitat(0d));
        grid2D.field[1][1] = new SeaTile(1,1,100, new TileHabitat(0d));
        //great
        NauticalMap map = new NauticalMap(new GeomGridField(grid2D),new GeomVectorField(),
                                          new CartesianDistance(1));
        FishState model = mock(FishState.class);
        when(model.getMap()).thenReturn(map);
        when(model.getStepsPerDay()).thenReturn(distancePerCell);
        when(model.getHoursPerStep()).thenReturn(24d);
        return model;
    }

    public static FishState generateSimpleSquareMap(int size) {
        ObjectGrid2D grid2D = new ObjectGrid2D(size,size);

        for(int i=0; i<size; i++)
            for(int j=0; j<size; j++)
            {
                grid2D.field[i][j] = new SeaTile(i,j,-100, new TileHabitat(0d));
            }

        NauticalMap map = new NauticalMap(new GeomGridField(grid2D),new GeomVectorField(),
                                          new EquirectangularDistance(0.0,1));
        FishState model = mock(FishState.class);
        when(model.getMap()).thenReturn(map);
        when(model.getStepsPerDay()).thenReturn(1);
        when(model.getHoursPerStep()).thenReturn(24d);
        return model;
    }

}