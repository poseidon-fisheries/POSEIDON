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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
import java.util.logging.Logger;

import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.model.StepOrder.*;


public class FishStateTest {

    @Test
    public void testScheduleEveryYear() throws Exception {

        //steps every 365 steps starting from 365
        final Steppable steppable = mock(Steppable.class);

        final FishState state = new FishState(1L);
        final Scenario scenario = mock(Scenario.class);
        final ScenarioEssentials result = mock(ScenarioEssentials.class);
        when(result.getBiology()).thenReturn(mock(GlobalBiology.class));
        when(scenario.start(state)).thenReturn(result);
        final ScenarioPopulation mock = mock(ScenarioPopulation.class);
        when(mock.getNetwork()).thenReturn(mock(SocialNetwork.class));
        when(scenario.populateModel(state)).thenReturn(mock);
        final NauticalMap map = mock(NauticalMap.class);
        when(result.getMap()).thenReturn(map);
        when(map.getPorts()).thenReturn(new LinkedList<>());

        state.setScenario(scenario);
        state.start();
        state.scheduleEveryYear(steppable, StepOrder.POLICY_UPDATE);
        state.scheduleEveryStep(simState -> {
        }, StepOrder.AFTER_DATA);
        //should step twice
        for (int i = 0; i < 730; i++)
            state.schedule.step(state);
        verify(steppable, times(2)).step(state);

    }

    @Test
    public void testCreateFishers() throws Exception {

        Logger.getGlobal().info("Testing that fishers can be created and destroyed");
        final PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new FromLeftToRightFactory()); //faster
        final SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setWidth(new FixedDoubleParameter(20));
        mapInitializer.setHeight(new FixedDoubleParameter(5));
        scenario.setMapInitializer(mapInitializer);
        scenario.setFishers(4);

        final FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();

        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);

        Assertions.assertEquals(4, state.getFishers().size());
        Assertions.assertTrue(state.canCreateMoreFishers());
        state.createFisher(FishState.DEFAULT_POPULATION_NAME);
        state.createFisher(FishState.DEFAULT_POPULATION_NAME);
        state.createFisher(FishState.DEFAULT_POPULATION_NAME);
        state.schedule.step(state);
        Assertions.assertEquals(7, state.getFishers().size());
        state.schedule.step(state);
        Assertions.assertEquals(7, state.getFishers().size());
        state.killRandomFisher();
        state.killRandomFisher();
        Assertions.assertEquals(5, state.getFishers().size());
        state.schedule.step(state);
        Assertions.assertEquals(5, state.getFishers().size());

        Logger.getGlobal().info("Testing that new fishers collect data just like the old ones");
        final Fisher newguy = state.createFisher(FishState.DEFAULT_POPULATION_NAME);
        for (int i = 0; i < 10; i++)
            state.schedule.step(state);
        Assertions.assertEquals(10, newguy.getDailyData().numberOfObservations());


    }

    @Test
    public void hoursSinceStartWorks() throws Exception {


        final Scenario scenario = mock(Scenario.class, RETURNS_DEEP_STUBS);
        final FishState state = new FishState(System.currentTimeMillis(), 2);
        state.setScenario(scenario);

        state.start();
        while (state.getDay() < 366)
            state.schedule.step(state);
        Assertions.assertEquals(2, state.getDayOfTheYear());
        Assertions.assertEquals(1, state.getYear());
        Assertions.assertEquals(366, state.getDay(), .001d);
        //midnight is the previous day, so that the first valid step for day 366 will be at 12pm
        Assertions.assertEquals(8784 + 12, state.getHoursSinceStart(), 0.0001d);
    }

    @Test
    public void testSteppables() throws Exception {

        Logger.getGlobal().info("testing that the `scheduleOnce()` calls are actually just called once");
        final int[] stepCounter = new int[2];
        stepCounter[0] = 0;
        stepCounter[1] = 0;

        final FishState state = new FishState();
        state.setScenario(mock(Scenario.class, RETURNS_DEEP_STUBS));
        state.start();
        //increase array after 300 days
        state.scheduleOnceInXDays((Steppable) simState -> stepCounter[0]++, DAWN, 300);
        //increase array after 800 days
        state.scheduleOnceInXDays((Steppable) simState -> stepCounter[0]++, DAWN, 800);
        //increase array at the end of year 1
        state.scheduleOnceAtTheBeginningOfYear((Steppable) simState -> stepCounter[1]++, DAWN, 1);

        for (int day = 0; day < 350; day++) {
            state.schedule.step(state);
        }
        Assertions.assertEquals(stepCounter[0], 1);
        Assertions.assertEquals(stepCounter[1], 0);
        for (int day = 0; day < 350; day++) {
            state.schedule.step(state);
        }
        Assertions.assertEquals(stepCounter[0], 1);
        Assertions.assertEquals(stepCounter[1], 1);
        for (int day = 0; day < 350; day++) {
            state.schedule.step(state);
        }
        Assertions.assertEquals(stepCounter[0], 2);
        Assertions.assertEquals(stepCounter[1], 1);
    }

    @Test
    public void testScheduleEveryYearStepOrders() {
        final FishState fishState = new FishState();
        fishState.start();
        new ImmutableMap.Builder<StepOrder, String>()
            .put(DAWN, "DAWN(false)")
            .put(FISHER_PHASE, "FISHER_PHASE(true)")
            .put(BIOLOGY_PHASE, "BIOLOGY_PHASE(true)")
            .put(POLICY_UPDATE, "POLICY_UPDATE(true)")
            .put(DAILY_DATA_GATHERING, "DAILY_DATA_GATHERING(false)")
            .put(YEARLY_DATA_GATHERING, "YEARLY_DATA_GATHERING(false)")
            .put(AGGREGATE_DATA_GATHERING, "AGGREGATE_DATA_GATHERING(false)")
            .put(DATA_RESET, "DATA_RESET(false)")
            .put(AFTER_DATA, "AFTER_DATA(true)")
            .build()
            .forEach((order, name) -> {
                fishState.scheduleOnce(
                    simState -> {
                        System.out.printf("day %d: %s\n", fishState.getDay(), name);
                        fishState.scheduleEveryYear(
                            simState1 -> System.out.printf("day %d: %s\n", fishState.getDay(), name),
                            order
                        );
                    },
                    order
                );
            });
        do fishState.schedule.step(fishState); while (fishState.getDay() <= 900);
    }

    @Test
    public void testGetDate() {
        final FishState fishState = new FishState();
        final int daysFromNow = 5;
        fishState.start();
        fishState.scheduleOnce(
            simState1 -> {
                final FishState fs1 = (FishState) simState1;
                Assertions.assertEquals(fs1.getScenario().getStartDate(), fs1.getDate());
                fishState.scheduleOnceInXDays(
                    simState2 -> {
                        final FishState fs2 = (FishState) simState2;
                        Assertions.assertEquals(fs2.getScenario().getStartDate().plusDays(daysFromNow), fs2.getDate());
                    },
                    DAWN,
                    daysFromNow
                );
            },
            DAWN
        );
        for (int i = 0; i < daysFromNow; i++) {
            fishState.schedule.step(fishState);
        }
    }
}