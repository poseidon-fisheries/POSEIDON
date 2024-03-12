/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeCornerPortMap;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class DrawThenCheapestInsertionPlannerTest {

    @Test
    public void plansTheRightAmountOfFishing() {

        // you have a plan where you have only 100 hours of budget
        // the numbers are such that you will only draw fishing at location
        // 5,0; 1,1; 2,2; 1,1; before running out of budget
        // you should be able to
        // (1) get them in right order in the plan path (port --> 1,1 --> 1,1 ---> 2,2 ---> 5,0 --> port
        // (2) not exceed your budget and add more actions

        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        final FishState fishState = mock(FishState.class);
        when(fishState.getRandom()).thenReturn(new MersenneTwisterFast());
        final NauticalMap map = makeMap(10, 10);
        when(fishState.getMap()).thenReturn(map);
        map.setDistance(new ManhattanDistance());
        when(fisher.grabState()).thenReturn(fishState);

        // fisher lives at position 0,0
        when(fisher.getLocation()).thenReturn(map.getSeaTile(0, 0));
        when(fisher.getHomePort().getLocation()).thenReturn(map.getSeaTile(0, 0));
        when(fisher.isAtPort()).thenReturn(true);
        when(fisher.isAllowedAtSea()).thenReturn(true);
        when(fisher.getBoat().getSpeedInKph()).thenReturn(0.29); // takes about 50 hours to do 14 steps

        final PlanningModule fakeModule = mock(PlanningModule.class);
        // you can take 1000 actions
        when(fakeModule.maximumActionsInAPlan(any(), any())).thenReturn(1000);
        // you will plan to go 5,0; 1,1; 2,2; 1,1 and from then 3,3 (which should not be selected)
        when(fakeModule.chooseNextAction(any())).thenReturn(
            new PlannedAction.Fishing(map.getSeaTile(5, 0), 9),
            new PlannedAction.Fishing(map.getSeaTile(1, 1), 9),
            new PlannedAction.Fishing(map.getSeaTile(2, 2), 9),
            new PlannedAction.Fishing(map.getSeaTile(1, 1), 9),
            new PlannedAction.Fishing(map.getSeaTile(3, 3), 9)
        );

        final Map<ActionType, Double> plannableActionWeights = new HashMap<>();
        plannableActionWeights.put(
            ActionType.FishingOnTile,
            100d
        ); // should normalize (actually probably ignore it altogether)
        final HashMap<ActionType, PlanningModule> planModules = new HashMap<>();
        planModules.put(ActionType.FishingOnTile, fakeModule);

        final DrawThenCheapestInsertionPlanner planner = new DrawThenCheapestInsertionPlanner(
            new FixedDoubleParameter(100), // 100hr
            plannableActionWeights,
            planModules,
            false
        );
        planner.start(fishState, fisher);
        final Plan plan = planner.planNewTrip();

        System.out.println(plan);
        final List<PlannedAction> plannedActions = plan.plannedActions();
        // should have ordered it right
        assertEquals(plannedActions.get(1).getLocation().getGridX(), 1);
        assertEquals(plannedActions.get(1).getLocation().getGridY(), 1);
        assertEquals(plannedActions.get(2).getLocation().getGridX(), 1);
        assertEquals(plannedActions.get(2).getLocation().getGridY(), 1);
        assertEquals(plannedActions.get(3).getLocation().getGridX(), 2);
        assertEquals(plannedActions.get(3).getLocation().getGridY(), 2);
        assertEquals(plannedActions.get(4).getLocation().getGridX(), 5);
        assertEquals(plannedActions.get(4).getLocation().getGridY(), 0);
        assertEquals(plannedActions.get(5).getLocation().getGridX(), 0);
        assertEquals(plannedActions.get(5).getLocation().getGridY(), 0);

    }

    @Test
    public void replan() {

        // mix of deploys and fish but then you are forced to replan: deploys stay and don't increase in number while
        // the location of the FSH changes
        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        final FishState fishState = mock(FishState.class);
        final MersenneTwisterFast rng = new MersenneTwisterFast();
        when(fishState.getRandom()).thenReturn(rng);
        final NauticalMap map = makeMap(11, 11);
        when(fishState.getMap()).thenReturn(map);
        map.setDistance(new ManhattanDistance());
        when(fisher.grabState()).thenReturn(fishState);

        // fisher lives at position 0,0
        when(fisher.getLocation()).thenReturn(map.getSeaTile(0, 0));
        when(fisher.getHomePort().getLocation()).thenReturn(map.getSeaTile(0, 0));
        when(fisher.isAtPort()).thenReturn(true);
        when(fisher.isAllowedAtSea()).thenReturn(true);
        when(fisher.getBoat().getSpeedInKph()).thenReturn(1d); // takes about 1 hours to do 1 steps

        // the fishing module always wants to fish at 10,10 (takes one hour)
        final PlanningModule fakeModule = mock(PlanningModule.class);
        when(fakeModule.maximumActionsInAPlan(any(), any())).thenReturn(1000);
        when(fakeModule.chooseNextAction(any())).thenReturn(
            new PlannedAction.Fishing(map.getSeaTile(10, 10), 0));

        // the deployment module always wants to deploy at 1,1
        //(here we make it take an hour to deploy)
        final ImmutableMap<Int2D, Double> initialValues = ImmutableMap.of(
            new Int2D(1, 1), 6.9
        );
        final DeploymentLocationValues dplValues =
            new DeploymentLocationValues(__ -> initialValues, 1.0);
        final DeploymentFromLocationValuePlanningModule deploymentModule =
            new DeploymentFromLocationValuePlanningModule(
                dplValues, map, rng, 1.0);
        final PurseSeineGear gear = mock(PurseSeineGear.class);
        when(fisher.getGear()).thenReturn(gear);
        final FadManager fadmanager = mock(FadManager.class);
        when(gear.getFadManager()).thenReturn(fadmanager);
        when(fadmanager.getNumFadsInStock()).thenReturn(1000000); // no problem with the fads

        // 50% chance of planning for either fishing or not
        final Map<ActionType, Double> plannableActionWeights = new HashMap<>();
        plannableActionWeights.put(ActionType.FishingOnTile, 100d); // should normalize
        plannableActionWeights.put(ActionType.DeploymentAction, 100d); // should normalize
        final HashMap<ActionType, PlanningModule> planModules = new HashMap<>();
        planModules.put(ActionType.FishingOnTile, fakeModule);
        planModules.put(ActionType.DeploymentAction, deploymentModule);

        final DrawThenCheapestInsertionPlanner planner = new DrawThenCheapestInsertionPlanner(
            new FixedDoubleParameter(100), // 100hr max!
            plannableActionWeights,
            planModules,
            false
        );
        planner.start(fishState, fisher);
        Plan plan = planner.planNewTrip();

        int numberOfDPLBeforeReplan = 0;
        int otherActions = 0;
        for (final PlannedAction step : plan.plannedActions()) {
            if (step instanceof PlannedAction.Deploy)
                numberOfDPLBeforeReplan++;
            else {
                otherActions++;
            }
        }
        System.out.println(numberOfDPLBeforeReplan);
        System.out.println(otherActions);
        // now let's say we moved to position 5,5 and we are forced to replan
        when(fisher.getLocation()).thenReturn(map.getSeaTile(5, 5));
        // only wasted 5 hours, there should be plenty of time for stuff
        plan = planner.replan(5d);
        int numberOfDPLAfterReplan = 0;
        int otherActionsAfterReplan = 0;
        for (final PlannedAction step : plan.plannedActions()) {
            if (step instanceof PlannedAction.Deploy)
                numberOfDPLAfterReplan++;
            else {
                otherActionsAfterReplan++;
            }
        }
        System.out.println(numberOfDPLAfterReplan);
        System.out.println(otherActionsAfterReplan);
        assertEquals(numberOfDPLAfterReplan, numberOfDPLBeforeReplan);
        // you should also go to 10,10 before doing those DPLs
        final List<PlannedAction> plannedActions = plan.plannedActions();
        assertEquals(plannedActions.get(2).getLocation().getGridX(), 10);
        assertEquals(plannedActions.get(2).getLocation().getGridY(), 10);
        System.out.println(plan);

        // because we teleported you at 5,5 and consumed only 5 hours (rather than the 10 it takes)
        // and because you can only take FSH and not DPL; you should have increased the amount of FSH actions
        // in the replan
        assertTrue(otherActionsAfterReplan > otherActions);
        // in fact it should increase by precisely 5 times (but rounding may screw this up)
    }

    // todo plan test where you have some nulls

    @Test
    public void actionPreferenceOverrides() {

        final NauticalMap map = makeCornerPortMap(1, 2);
        final Port port = map.getPorts().get(0);
        final Boat boat = mock(Boat.class);
        when(boat.getSpeedInKph()).thenReturn(100.0);

        final Fisher fisher = mock(Fisher.class);
        when(fisher.getLocation()).thenReturn(port.getLocation());
        when(fisher.getHomePort()).thenReturn(port);
        when(fisher.isAtPort()).thenReturn(true);
        when(fisher.isAllowedAtSea()).thenReturn(true);
        when(fisher.getBoat()).thenReturn(boat);

        final FishState fishState = mock(FishState.class);
        when(fishState.getRandom()).thenReturn(new MersenneTwisterFast());
        when(fishState.getMap()).thenReturn(map);
        when(fisher.grabState()).thenReturn(fishState);

        final FadManager fadmanager = mock(FadManager.class);
        when(fadmanager.numberOfPermissibleActions(eq(ActionClass.DPL), anyInt(), any())).thenReturn(99);

        final PurseSeineGear gear = mock(PurseSeineGear.class);
        when(fisher.getGear()).thenReturn(gear);
        when(gear.getFadManager()).thenReturn(fadmanager);

        final PlanningModule deploymentModule =
            new DeploymentFromLocationValuePlanningModule(
                new DeploymentLocationValues(
                    __ -> ImmutableMap.of(new Int2D(0, 1), 1.0),
                    0.0
                ),
                map,
                new MersenneTwisterFast(0),
                1.0
            );

        final PlanningModule fishingModule = mock(PlanningModule.class);
        when(fishingModule.maximumActionsInAPlan(any(), any())).thenReturn(50);
        when(fishingModule.chooseNextAction(any())).thenReturn(
            new PlannedAction.Fishing(map.getSeaTile(0, 1), 1)
        );

        final DrawThenCheapestInsertionPlanner planner =
            new DrawThenCheapestInsertionPlanner(
                new FixedDoubleParameter(100), // 100hr
                // 50% chance of planning for either fishing or not
                ImmutableMap.of(
                    ActionType.FishingOnTile, 1.0,
                    ActionType.DeploymentAction, 1.0
                ),
                ImmutableMap.of(
                    ActionType.FishingOnTile, fishingModule,
                    ActionType.DeploymentAction, deploymentModule
                ),
                false
            );
        planner.start(fishState, fisher);

        final Map<? extends Class<? extends PlannedAction>, Long> actionClassCountsBeforeOverride =
            planner.planNewTrip()
                .plannedActions()
                .stream()
                .collect(groupingBy(PlannedAction::getClass, counting()));

        planner.setActionPreferenceOverrides(ImmutableList.of(ActionType.DeploymentAction));
        final Map<? extends Class<? extends PlannedAction>, Long> actionClassCountsAfterOverride =
            planner.planNewTrip()
                .plannedActions()
                .stream()
                .collect(groupingBy(PlannedAction::getClass, counting()));

        // before the override, we should have enough actions of both types
        assertTrue(actionClassCountsBeforeOverride.get(PlannedAction.Fishing.class) > 10);
        assertTrue(actionClassCountsBeforeOverride.get(PlannedAction.Deploy.class) > 10);

        // after the override, the plan should be filled with deployments
        assertNull(actionClassCountsAfterOverride.get(PlannedAction.Fishing.class));
        assertEquals(99, actionClassCountsAfterOverride.get(PlannedAction.Deploy.class));
    }

}
