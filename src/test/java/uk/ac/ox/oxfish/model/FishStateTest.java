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

package uk.ac.ox.oxfish.model;

import com.esotericsoftware.minlog.Log;
import org.junit.Test;
import org.mockito.MockSettings;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.ScenarioEssentials;
import uk.ac.ox.oxfish.model.scenario.ScenarioPopulation;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class FishStateTest {

    @Test
    public void testScheduleEveryYear() throws Exception {

        //steps every 365 steps starting from 365
        Steppable steppable = mock(Steppable.class);

        FishState state = new FishState(1L);
        Scenario scenario = mock(Scenario.class);
        ScenarioEssentials result = mock(ScenarioEssentials.class);
        when(result.getBiology()).thenReturn(mock(GlobalBiology.class));
        when(scenario.start(state)).thenReturn(result);
        final ScenarioPopulation mock = mock(ScenarioPopulation.class);
        when(mock.getNetwork()).thenReturn(mock(SocialNetwork.class));
        when(scenario.populateModel(state)).thenReturn(mock);
        NauticalMap map = mock(NauticalMap.class); when(result.getMap()).thenReturn(map);
        when(map.getPorts()).thenReturn(new LinkedList<>());

        state.setScenario(scenario);
        state.start();
        state.scheduleEveryYear(steppable, StepOrder.POLICY_UPDATE);
        state.scheduleEveryStep(simState -> {},StepOrder.AFTER_DATA);
        //should step twice
        for(int i=0; i<730; i++)
            state.schedule.step(state);
        verify(steppable,times(2)).step(state);

    }

    @Test
    public void testCreateFishers() throws Exception {

        Log.info("Testing that fishers can be created and destroyed");
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new FromLeftToRightFactory()); //faster
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setWidth(new FixedDoubleParameter(20));
        mapInitializer.setHeight(new FixedDoubleParameter(5));
        scenario.setMapInitializer(mapInitializer);
        scenario.setFishers(4);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();

        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);

        assertEquals(4,state.getFishers().size());
        assertTrue(state.canCreateMoreFishers());
        state.createFisher(FishState.DEFAULT_POPULATION_NAME);
        state.createFisher(FishState.DEFAULT_POPULATION_NAME);
        state.createFisher(FishState.DEFAULT_POPULATION_NAME);
        state.schedule.step(state);
        assertEquals(7,state.getFishers().size());
        state.schedule.step(state);
        assertEquals(7,state.getFishers().size());
        state.killRandomFisher();
        state.killRandomFisher();
        assertEquals(5,state.getFishers().size());
        state.schedule.step(state);
        assertEquals(5,state.getFishers().size());

        Log.info("Testing that new fishers collect data just like the old ones");
        Fisher newguy = state.createFisher(FishState.DEFAULT_POPULATION_NAME);
        for(int i=0; i<10; i++)
            state.schedule.step(state);
        assertEquals(10,newguy.getDailyData().numberOfObservations());



    }

    @Test
    public void hoursSinceStartWorks() throws Exception {


        Scenario scenario = mock(Scenario.class, RETURNS_DEEP_STUBS);
        FishState state = new FishState(System.currentTimeMillis(),2);
        state.setScenario(scenario);

        state.start();
        while(state.getDay()<366)
            state.schedule.step(state);
        assertEquals(2,state.getDayOfTheYear());
        assertEquals(1,state.getYear());
        assertEquals(366,state.getDay(),.001d);
        //midnight is the previous day, so that the first valid step for day 366 will be at 12pm
        assertEquals(8784+12,state.getHoursSinceStart(),0.0001d);
    }

    @Test
    public void testSteppables() throws Exception {

        Log.info("testing that the `scheduleOnce()` calls are actually just called once");
        final int stepCounter[] = new int[2];
        stepCounter[0]=0;
        stepCounter[1]=0;

        FishState state = new FishState();
        state.setScenario(mock(Scenario.class,RETURNS_DEEP_STUBS));
        state.start();
        //increase array after 300 days
        state.scheduleOnceInXDays(new Steppable() {
            @Override
            public void step(SimState simState) {
                stepCounter[0]++;
            }
        },StepOrder.DAWN,300);
        //increase array after 800 days
        state.scheduleOnceInXDays(new Steppable() {
            @Override
            public void step(SimState simState) {
                stepCounter[0]++;
            }
        },StepOrder.DAWN,800);
        //increase array at the end of year 1
        state.scheduleOnceAtTheBeginningOfYear(new Steppable() {
            @Override
            public void step(SimState simState) {
                stepCounter[1]++;
            }
        },StepOrder.DAWN,1);

        for(int day=0;day<350; day++)
        {
            state.schedule.step(state);
        }
        assertEquals(stepCounter[0],1);
        assertEquals(stepCounter[1],0);
        for(int day=0;day<350; day++)
        {
            state.schedule.step(state);
        }
        assertEquals(stepCounter[0],1);
        assertEquals(stepCounter[1],1);
        for(int day=0;day<350; day++)
        {
            state.schedule.step(state);
        }
        assertEquals(stepCounter[0],2);
        assertEquals(stepCounter[1],1);
    }
}