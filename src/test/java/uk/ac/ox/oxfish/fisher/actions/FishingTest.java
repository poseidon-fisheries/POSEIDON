package uk.ac.ox.oxfish.fisher.actions;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.equipment.*;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.RandomThenBackToPortDestinationStrategyTest;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.FixedGearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.IgnoreWeatherStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class FishingTest {


    @Test
    public void simpleVerify() throws Exception {

        Action fishing = new Fishing();

        Fisher agent = mock(Fisher.class);
        when(agent.isAtDestination()).thenReturn(true); when(agent.getLocation()).thenReturn(new SeaTile(0,0,-1,
                                                                                                         new TileHabitat(
                                                                                                                 0d)));
        fishing.act(mock(FishState.class), agent, new Anarchy(),1d );
        verify(agent).fishHere(any(),anyInt(),any() );
    }


    @Test
    public void integrationTest() throws Exception {

        FishState fishState = RandomThenBackToPortDestinationStrategyTest.generateSimple2x2Map(1);
        when(fishState.getRandom()).thenReturn(new MersenneTwisterFast());
        when(fishState.getHoursPerStep()).thenReturn(1d);


        Species species = new Species("pippo");
        GlobalBiology biology = new GlobalBiology(species);
        when(fishState.getBiology()).thenReturn(biology);

        Port port = new Port("Port 0", fishState.getMap().getSeaTile(1, 1), mock(MarketMap.class), 0);

        Gear gear = mock(Gear.class);
        when(gear.fish(any(),any(),anyInt(),any())).thenReturn(new Catch(species, 50.0, biology));
        Fisher fisher = new Fisher(0, port,
                                   new MersenneTwisterFast(),
                                   new AnarchyFactory().apply(fishState),
                                   new FixedProbabilityDepartingStrategy(1.0, false),
                                   new FavoriteDestinationStrategy(fishState.getMap().getSeaTile(0, 1)),
                                   new FishingStrategy() {
                                       @Override
                                       public boolean shouldFish(
                                               Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip) {
                                           return true;
                                       }

                                       @Override
                                       public void start(FishState model,Fisher fisher) {

                                       }

                                       @Override
                                       public void turnOff(Fisher fisher) {

                                       }
                                   },
                                   new FixedGearStrategy(),
                                   new IgnoreWeatherStrategy(),
                                   new Boat(1,1,new Engine(1,1,1),new FuelTank(1000000)), new Hold(100.0, 1), gear, 1);
        fisher.start(fishState);
        fisher.step(fishState);
        assertEquals(0,fisher.getPoundsCarried(),.001);


        //should have fished 50 pounds
        fisher.step(fishState);
        assertEquals(50.0,fisher.getPoundsCarried(),.001);

        //step again and it will fish 50 more!
        fisher.step(fishState);
        assertEquals(100.0, fisher.getPoundsCarried(), .001);

        //fish again does nothing because it's full
        fisher.step(fishState);
        assertEquals(100.0, fisher.getPoundsCarried(), .001);
        verify(gear,times(3)).fish(any(),any(),anyInt(), any());


    }
}