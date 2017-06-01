package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionGearFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HoldLimitingDecoratorFactory;
import uk.ac.ox.oxfish.fisher.strategies.gear.FixedGearStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/1/17.
 */
public class HoldLimitingDecoratorGearTest {


    @Test
    public void limits() throws Exception {


        Fisher fisher = mock(Fisher.class);
        //only 50 units left!
        when(fisher.getMaximumHold()).thenReturn(100d);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(50d);

        //catches 100 units
        Gear delegate = mock(Gear.class);
        when(delegate.fish(any(),any(),anyInt(),any())).thenReturn(
                new Catch(new double[]{70,30})
        );

        HoldLimitingDecoratorGear gear = new HoldLimitingDecoratorGear(delegate);
        Catch haul = gear.fish(fisher, mock(SeaTile.class), 100, mock(GlobalBiology.class));
        assertEquals(haul.getTotalWeight(),50d,.001);
        assertEquals(haul.getWeightCaught(0),35d,.001);
        assertEquals(haul.getWeightCaught(1),15d,.001);

    }


    @Test
    public void noDiscards() throws Exception
    {

        //in the default case, there is a bunch of discard due to fish being caught above the hold limits
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(5);
        FixedProportionGearFactory gear = new FixedProportionGearFactory();
        gear.setCatchabilityPerHour(new FixedDoubleParameter(.01));
        scenario.setGear(gear);

        long seed = System.currentTimeMillis();
        FishState state = new FishState(seed);
        state.setScenario(scenario);
        state.start();
        while(state.getYear()<1)
            state.schedule.step(state);
        state.schedule.step(state);

        double discardRate = 1d- state.getYearlyDataSet().getLatestObservation("Species 0 Landings") /
                state.getYearlyDataSet().getLatestObservation("Species 0 Catches");
        System.out.println(discardRate);
        assertTrue(discardRate >= .05);


        //same scenario, but with limiting holding gear
        HoldLimitingDecoratorFactory limiting = new HoldLimitingDecoratorFactory();
        limiting.setDelegate(gear);
        scenario.setGear(limiting);
        state = new FishState(seed);
        state.setScenario(scenario);
        state.start();
        while(state.getYear()<1)
            state.schedule.step(state);
        state.schedule.step(state);

        discardRate = 1d- state.getYearlyDataSet().getLatestObservation("Species 0 Landings") /
                state.getYearlyDataSet().getLatestObservation("Species 0 Catches");
        System.out.println(discardRate);
        assertTrue(discardRate <= .01);

    }
}