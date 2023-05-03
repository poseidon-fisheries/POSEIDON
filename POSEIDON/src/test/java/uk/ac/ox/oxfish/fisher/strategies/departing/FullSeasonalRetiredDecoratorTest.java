/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import com.beust.jcommander.internal.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FullSeasonalRetiredDecoratorFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.MaxHoursPerYearDepartingFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FullSeasonalRetiredDecoratorTest {


    @Test
    public void updatesStatusCorrectly() {


        FullSeasonalRetiredDecorator decorator = new FullSeasonalRetiredDecorator(
                EffortStatus.FULLTIME,100,10,0,
                new FixedRestTimeDepartingStrategy(0),
                "Average Cash-Flow");

        Fisher fisher = mock(Fisher.class);
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(200d);
        FishState model = mock(FishState.class);
        when(model.getDay()).thenReturn(1000); //pass the test

        decorator.start(model, fisher);
        verify(fisher,times(1)).getAdditionalVariables();

        //
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);
        //you are making more than target; you won't change
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);
        //you are making more than minimum; you won't change
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(20d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);


        //switch to retired
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(2d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        //between minimum and target; you stay seasonal
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(20d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        //back above target: you go back full time!
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(200d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);



        //you can retire if you do badly often
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(2d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.RETIRED);


        //at which point you are stuck in retired forever without friends!
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(9999d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.RETIRED);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.RETIRED);



        verify(fisher,times(11)).getAdditionalVariables();

    }



    @Test
    public void inertia() {


        FullSeasonalRetiredDecorator decorator = new FullSeasonalRetiredDecorator(
                EffortStatus.FULLTIME,100,10,0,
                new FixedRestTimeDepartingStrategy(0),
                "Average Cash-Flow",-1,3, true);

        Fisher fisher = mock(Fisher.class);
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(200d);
        FishState model = mock(FishState.class);
        when(model.getDay()).thenReturn(1000); //pass the test

        decorator.start(model, fisher);
        verify(fisher,times(1)).getAdditionalVariables();

        //
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);
        //you are making more than target; you won't change
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);
        //you are making more than minimum; you won't change
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(20d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);


        //switch to retired after 3
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(2d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        //between minimum and target; you stay seasonal
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(20d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        //back above target: you go back full time!
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(200d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);



        //you can retire if you do badly often
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(2d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.FULLTIME);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.RETIRED);


        //at which point you are stuck in retired forever without friends!
        when(fisher.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(9999d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.RETIRED);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.RETIRED);




    }

    /**
     * make sure you can come back from retirement if others are making money!
     */
    @Test
    public void withFriendsLikeThese() {


        FullSeasonalRetiredDecorator decorator = new FullSeasonalRetiredDecorator(
                EffortStatus.RETIRED,100,10,0,
                new FixedRestTimeDepartingStrategy(0),
                "Average Cash-Flow");

        Fisher fisher = mock(Fisher.class,  RETURNS_DEEP_STUBS);

        Fisher friend1 = mock(Fisher.class,  RETURNS_DEEP_STUBS);
        Fisher friend2 = mock(Fisher.class,  RETURNS_DEEP_STUBS);
        FishState model = mock(FishState.class);
        when(model.getDay()).thenReturn(1000); //pass the test



        when(fisher.getDirectedFriends()).thenReturn(Lists.newArrayList(friend1,friend2));
        //friends make no money; you won't come out of retirement
        when(friend1.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(2d);
        when(friend2.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(-2d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.RETIRED);

        //both make more than minimumIncome; still not good enough!
        when(friend1.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(20d);
        when(friend2.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(32d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.RETIRED);

        //but if one makes more than target; you come back
        when(friend1.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(-20d);
        when(friend2.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(320d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.SEASONAL);

        verify(fisher,times(3)).getAdditionalVariables();

    }


    /**
     * flag prevents you from retiring
     */
    @Test
    public void withFriendsLikeTheseButBlocked() {

        FullSeasonalRetiredDecoratorFactory factory = new FullSeasonalRetiredDecoratorFactory();
        factory.setCanReturnFromRetirement(false);
        final FullSeasonalRetiredDecorator apply = factory.apply(new FishState());
        assertFalse(apply.isCanReturnFromRetirement());

        FullSeasonalRetiredDecorator decorator = new FullSeasonalRetiredDecorator(
                EffortStatus.RETIRED,100,10,0,
                new FixedRestTimeDepartingStrategy(0),
                "Average Cash-Flow",-1,1,false);

        Fisher fisher = mock(Fisher.class,  RETURNS_DEEP_STUBS);

        Fisher friend1 = mock(Fisher.class,  RETURNS_DEEP_STUBS);
        Fisher friend2 = mock(Fisher.class,  RETURNS_DEEP_STUBS);
        FishState model = mock(FishState.class);
        when(model.getDay()).thenReturn(1000); //pass the test



        when(fisher.getDirectedFriends()).thenReturn(Lists.newArrayList(friend1,friend2));
        //friends make no money; you won't come out of retirement
        when(friend1.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(2d);
        when(friend2.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(-2d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.RETIRED);

        //both make more than minimumIncome; still not good enough!
        when(friend1.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(20d);
        when(friend2.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(32d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.RETIRED);

        //but if one makes more than target; you come back
        when(friend1.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(-20d);
        when(friend2.getLatestYearlyObservation("Average Cash-Flow")).thenReturn(320d);
        decorator.updateEffortLevel(fisher,model);
        assertEquals(decorator.getStatus(),EffortStatus.RETIRED);

        verify(fisher,times(3)).getAdditionalVariables();

    }

    @Test
    public void itActuallyReducesEffort(){

        //should switch to seasonal after a year and should go out less!
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(10);
        FullSeasonalRetiredDecoratorFactory seasonal = new FullSeasonalRetiredDecoratorFactory();
        seasonal.setDecorated(new MaxHoursPerYearDepartingFactory(5000));
        seasonal.setMaxHoursOutWhenSeasonal(new FixedDoubleParameter(50));
        seasonal.setMinimumVariable(new FixedDoubleParameter(Double.MAX_VALUE)); //going to fail!
        seasonal.setTargetVariable(new FixedDoubleParameter(Double.MAX_VALUE)); //going to fail!
        scenario.setDepartingStrategy(seasonal);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        while(state.getYear()<2)
            state.schedule.step(state);


        assertTrue(state.getYearlyDataSet().getColumn("Average Number of Trips").get(0)>
                10 *
                        state.getYearlyDataSet().getColumn("Average Number of Trips").get(1));




    }
}