package uk.ac.ox.oxfish.fisher.actions;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Gear;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.RandomThenBackToPortDestinationStrategyTest;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.Markets;
import uk.ac.ox.oxfish.model.regs.Anarchy;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class FishingTest {


    @Test
    public void simpleVerify() throws Exception {

        Action fishing = new Fishing();

        Fisher agent = mock(Fisher.class);
        when(agent.isAtDestination()).thenReturn(true); when(agent.getLocation()).thenReturn(new SeaTile(0,0,-1));
        fishing.act(mock(FishState.class), agent, new Anarchy() );
        verify(agent).fishHere(any());
    }


    @Test
    public void integrationTest() throws Exception {

        FishState fishState = RandomThenBackToPortDestinationStrategyTest.generateSimple2x2Map();


        Specie specie = new Specie("pippo");
        GlobalBiology biology = new GlobalBiology(specie);
        when(fishState.getBiology()).thenReturn(biology);

        Port port = new Port(fishState.getMap().getSeaTile(1,1),mock(Markets.class)  );

        Gear gear = mock(Gear.class);
        when(gear.fish(any(),any(),any())).thenReturn(new Catch(specie, 50.0, biology));
        Fisher fisher = new Fisher(port, new MersenneTwisterFast(), new Anarchy(), new FixedProbabilityDepartingStrategy(1.0),
                                   new FavoriteDestinationStrategy(fishState.getMap().getSeaTile(0, 0)),
                                   (fisher1, random, model) -> true,
                                   new Boat(100.0),
                                   new Hold(100.0,1),
                                   gear);
        fisher.step(fishState);

        //should have fished 50 pounds
        assertEquals(50.0,fisher.getPoundsCarried(),.001);

        //step again and it will fish 50 more!
        fisher.step(fishState);
        assertEquals(100.0, fisher.getPoundsCarried(), .001);

        //fish again does nothing because it's full
        fisher.step(fishState);
        assertEquals(100.0, fisher.getPoundsCarried(), .001);
        verify(gear,times(3)).fish(any(),any(),any());


    }
}