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

package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionGearFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HoldLimitingDecoratorFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyTimeSeries;
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
        when(delegate.fish(any(), any(), any(), anyInt(), any())).thenReturn(
                new Catch(new double[]{70,30})
        );

        HoldLimitingDecoratorGear gear = new HoldLimitingDecoratorGear(delegate);
        SeaTile tile = mock(SeaTile.class);
        Catch haul = gear.fish(fisher, tile,tile , 100, mock(GlobalBiology.class));
        assertEquals(haul.getTotalWeight(),50d,.001);
        assertEquals(haul.getWeightCaught(0),35d,.001);
        assertEquals(haul.getWeightCaught(1),15d,.001);

        assertFalse(haul.hasAbundanceInformation());

    }


    @Test
    public void limitsWithAbundance() throws Exception {

        //set up copied from the holdsize test
        Meristics first = new FromListMeristics(new double[]{100,100,100}, 1);
        Meristics second = new FromListMeristics(new double[]{100,100},1);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second",second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);



        Fisher fisher = mock(Fisher.class);
        //only 200 units left!
        when(fisher.getMaximumHold()).thenReturn(300d);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(100d);
        //caught 500kg in total
        StructuredAbundance firstCatch = new StructuredAbundance(new double[]{1, 1, 1});
        StructuredAbundance secondCatch = new StructuredAbundance(new double[]{1, 1});
        Gear delegate = mock(Gear.class);
        when(delegate.fish(any(), any(), any(), anyInt(), any())).thenReturn(
                new Catch(
                        new StructuredAbundance[]{firstCatch,secondCatch},
                        bio
                )
        );

        HoldLimitingDecoratorGear gear = new HoldLimitingDecoratorGear(delegate);
        SeaTile tile = mock(SeaTile.class);
        Catch haul = gear.fish(fisher, tile, tile, 100, bio);
        assertTrue(haul.hasAbundanceInformation());
        assertEquals(haul.getTotalWeight(),200d,.001);
        assertEquals(haul.getWeightCaught(0),120d,.001);
        assertEquals(haul.getWeightCaught(1),80d,.001);

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
                state.getYearlyDataSet().getLatestObservation("Species 0 "+ FisherDailyTimeSeries.CATCHES_COLUMN_NAME);
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
                state.getYearlyDataSet().getLatestObservation("Species 0 "+ FisherDailyTimeSeries.CATCHES_COLUMN_NAME);
        System.out.println(discardRate);
        assertTrue(discardRate <= .01);

    }
}