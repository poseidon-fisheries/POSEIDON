/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class DrawThenCheapestInsertionPlannerTest {


    @Test
    public void plansTheRightAmountOfFishing() {

        //you have a plan where you have only 100 hours of budget
        //the numbers are such that you will only draw fishing at location
        //5,0; 1,1; 2,2; 1,1; before running out of budget
        //you should be able to
        // (1) get them in right order in the plan path (port --> 1,1 --> 1,1 ---> 2,2 ---> 5,0 --> port
        // (2) not exceed your budget and add more actions

        final Fisher fisher = mock(Fisher.class,RETURNS_DEEP_STUBS);
        final FishState fishState = mock(FishState.class);
        when(fishState.getRandom()).thenReturn(new MersenneTwisterFast());
        final NauticalMap map = makeMap(10, 10);
        when(fishState.getMap()).thenReturn(map);
        map.setDistance(new ManhattanDistance());
        when(fisher.grabState()).thenReturn(fishState);

        //fisher lives at position 0,0
        when(fisher.getLocation()).thenReturn(map.getSeaTile(0,0));
        when(fisher.getHomePort().getLocation()).thenReturn(map.getSeaTile(0,0));
        when(fisher.isAtPort()).thenReturn(true);
        when(fisher.isAllowedAtSea()).thenReturn(true);
        when(fisher.getBoat().getSpeedInKph()).thenReturn(0.29); //takes about 50 hours to do 14 steps


        PlanningModule fakeModule = mock(PlanningModule.class);
        //you can take 1000 actions
        when(fakeModule.maximumActionsInAPlan(any(),any())).thenReturn(1000);
        //you will plan to go 5,0; 1,1; 2,2; 1,1 and from then 3,3 (which should not be selected)
        when(fakeModule.chooseNextAction(any())).thenReturn(
                new PlannedAction.Fishing(map.getSeaTile(5,0),9),
                new PlannedAction.Fishing(map.getSeaTile(1,1),9),
                new PlannedAction.Fishing(map.getSeaTile(2,2),9),
                new PlannedAction.Fishing(map.getSeaTile(1,1),9),
                new PlannedAction.Fishing(map.getSeaTile(3,3),9)
        );

        Map<ActionType, Double> plannableActionWeights = new HashMap<>();
        plannableActionWeights.put(ActionType.FishingOnTile,100d); //should normalize (actually probably ignore it altogether)
        HashMap<ActionType, PlanningModule> planModules = new HashMap<>();
        planModules.put(ActionType.FishingOnTile,fakeModule);

        DrawThenCheapestInsertionPlanner planner = new DrawThenCheapestInsertionPlanner(
                new FixedDoubleParameter(100), //100hr
                plannableActionWeights,
                planModules
        );
        planner.start(fishState,fisher);
        Plan plan = planner.planNewTrip();

        System.out.println(plan);
        List<PlannedAction> plannedActions = plan.lookAtPlan();
        //should have ordered it right
        Assert.assertEquals(plannedActions.get(1).getLocation().getGridX(),1);
        Assert.assertEquals(plannedActions.get(1).getLocation().getGridY(),1);
        Assert.assertEquals(plannedActions.get(2).getLocation().getGridX(),1);
        Assert.assertEquals(plannedActions.get(2).getLocation().getGridY(),1);
        Assert.assertEquals(plannedActions.get(3).getLocation().getGridX(),2);
        Assert.assertEquals(plannedActions.get(3).getLocation().getGridY(),2);
        Assert.assertEquals(plannedActions.get(4).getLocation().getGridX(),5);
        Assert.assertEquals(plannedActions.get(4).getLocation().getGridY(),0);
        Assert.assertEquals(plannedActions.get(5).getLocation().getGridX(),0);
        Assert.assertEquals(plannedActions.get(5).getLocation().getGridY(),0);

    }

    //todo plan test where you have some nulls
}