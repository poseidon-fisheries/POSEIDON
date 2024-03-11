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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.MTFApache;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * A simple planner where given a budget of time you can spend out: 1 - Draw the next action to take from random
 * distribution 2 - Use a generator to turn that action into a planned action (or a set of possible choices) 3 - If you
 * have multiple choices (as for example with setting on FADs) pick the one whose profit/distance(centroid) is maximum 4
 * - Add that planned action in your path so that it's the cheapest insertion possible 5 - Keep doing this until you run
 * out of budget
 * <br>
 * hardcoded in many parts, but get it to work first and judge computational complexity first
 */
public class DrawThenCheapestInsertionPlanner implements FisherStartable {

    /**
     * need this to generate your budget in hours
     */
    private final DoubleParameter maxHoursPerTripGenerator;
    /**
     * mapping that for each action returns the probability of it occurring
     */
    final private Map<ActionType, Double> plannableActionWeights;
    /**
     * mapping that for each action returns the planning module giving us one candidate action to take
     */
    final private Map<ActionType, PlanningModule> planModules;
    /**
     * maximum number of times an action is still allowed to be added to the plan!
     */
    final private Map<ActionType, MutableInt> stillAllowedActionsInPlan = new HashMap<>();
    /**
     * when this is set to true you cannot put an action in the plan if it looks illegal now. When this is not true,
     * illegal actions stay in the plan until it's time to execute them. If they didn't become legal then, they will
     * trigger a re-plan
     */
    private final boolean doNotWaitToPurgeIllegalActions;
    private List<ActionType> actionPreferenceOverrides = ImmutableList.of();
    private Plan currentPlan;
    private Fisher fisher;
    private FishState model;
    private double thisTripTargetHours = 0;

    public DrawThenCheapestInsertionPlanner(
        final DoubleParameter maxHoursPerTripGenerator,
        final Map<ActionType, Double> plannableActionWeights,
        final Map<ActionType, PlanningModule> planModules,
        final boolean doNotWaitToPurgeIllegalActions
    ) {
        this.maxHoursPerTripGenerator = maxHoursPerTripGenerator;
        this.plannableActionWeights = plannableActionWeights;
        this.planModules = planModules;
        this.doNotWaitToPurgeIllegalActions = doNotWaitToPurgeIllegalActions;
    }

    /**
     * adds the path to the current plan and returns the hours it takes (duration + movement); returns NaN if we go over
     * budget! As a <b>side effect</b> it will <b>add the action to the plan</b> if it is within budget! <br>
     * <p>
     * No real reason to keep it static except that it is easier to test that way.
     *
     * @param currentPlan                      the plan as it is now
     * @param actionToAddToPath                the planned action to add to the plan
     * @param hoursAvailable                   how many hours we still have available for this trip
     * @param speed                            the speed of the boat (distance/time)
     * @param map                              nautical map for distance calculation
     * @param insertActionInPlanIfHoursAllowIt if true, the action is added to the plan with the cheapest possible
     *                                         insert. when this is set to false this method only computes the
     *                                         hypothetical hourly costs but does not insert the action in the given
     *                                         plan
     * @return the hours we consumed adding the action to the plan (or NAN if there are not enough hours)
     */
    @VisibleForTesting
    public static double cheapestInsert(
        final Plan currentPlan,
        final PlannedAction actionToAddToPath,
        final double hoursAvailable,
        final double speed,
        final NauticalMap map,
        final boolean insertActionInPlanIfHoursAllowIt
    ) {
        Preconditions.checkArgument(hoursAvailable > 0);
        Preconditions.checkArgument(speed > 0);
        Preconditions.checkArgument(
            currentPlan.numberOfStepsInPath() >= 2,
            "the path is too short, I'd expect here to be at least two steps"
        );
        assert (actionToAddToPath.getLocation() != null) : "Action " + actionToAddToPath + " has no path!";

        // go through all options
        double bestInsertionCost = Double.MAX_VALUE;
        int bestIndex = -1;
        // you don't want to insert it in the beginning, and you don't want to insert it at the end
        final ListIterator<PlannedAction> iterator = currentPlan.plannedActions().listIterator(1);
        // for each possible insertion point, adding point C between A and B has insertion cost equal to d(A,C)+d(C,B)
        // -d(A,B);
        // this distance ought to be positive due to triangle inequality
        for (int index = 1; index < currentPlan.numberOfStepsInPath(); index++) {

            final PlannedAction from = iterator.previous();
            iterator.next(); // this brings you back to start
            final PlannedAction to = iterator.next();

            // it is never optimal to squeeze yourself between two actions that take place in the same spot unless you
            // are also in that spot
            if (currentPlan.numberOfStepsInPath() > 2 &&
                from.getLocation() == to.getLocation() &&
                from.getLocation() != actionToAddToPath.getLocation())
                continue;

            final double firstSegment = map.distance(from.getLocation(), actionToAddToPath.getLocation());
            if (firstSegment == 0) {
                // if you are trying to insert in the same cell, this is surely the cheapest insert!
                bestInsertionCost = 0;
                bestIndex = index;
                break;
            }
            final double secondSegment = map.distance(actionToAddToPath.getLocation(), to.getLocation());
            final double replacedSegment = map.distance(from.getLocation(), to.getLocation());

            final double insertionCost =
                (firstSegment + secondSegment - replacedSegment) / speed; // turn insertion cost into time
            // tricky here because when you add the approximations from the curvature distance, if you are going on a
            // straight line
            // in the projected map, you may actually be violating by tiny amounts the triangle inequality constraint,
            // but we choose to ignore this
            // assert insertionCost >= -5 : "triangle inequality does not seem to hold here. Bizarre " + insertionCost;

            if (insertionCost < bestInsertionCost) {
                bestInsertionCost = insertionCost;
                bestIndex = index;
            }

        }
        final double totalCostInHours = bestInsertionCost + actionToAddToPath.hoursItTake();
        if (totalCostInHours <= hoursAvailable) {
            assert bestIndex > 0;
            if (insertActionInPlanIfHoursAllowIt)
                currentPlan.insertAction(actionToAddToPath, bestIndex);
            return totalCostInHours;
        } else {
            return Double.NaN;
        }

    }

    public List<ActionType> getActionPreferenceOverrides() {
        return actionPreferenceOverrides;
    }

    public void setActionPreferenceOverrides(final Collection<ActionType> actionPreferenceOverrides) {
        this.actionPreferenceOverrides = ImmutableList.copyOf(actionPreferenceOverrides);
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
        this.fisher = fisher;
        this.model = model;
    }

    /**
     * checks that the module has started, and that the "stillAllowedActionsInPlan" is filled correctly, etc...
     */
    private void readyPlanningModule(
        final ActionType action,
        final Fisher fisher,
        final FishState model
    ) {

        assert plannableActionWeights.get(action) > 0;

        // has it been initialized?
        if (!stillAllowedActionsInPlan.containsKey(action)) {

            // find the module
            final PlanningModule planningModule = planModules.get(action);
            Preconditions.checkArgument(
                planningModule != null,
                "You have assigned weight to " + action.toString() + " without any module associated to it"
            );
            // start the planning module
            planningModule.start(model, fisher);
            // set maximum actions
            stillAllowedActionsInPlan.put(action, new MutableInt(planningModule.maximumActionsInAPlan(model, fisher)));
            // done!
        }

    }

    /**
     * returns true as long as at least one of the planning modules has not been started or there are still
     * allowedActions for that type
     */
    private boolean isAnyActionEvenPossible() {
        return plannableActionWeights
            .entrySet()
            .stream()
            // can this action type be drawn?
            .filter(entry -> entry.getValue() > 0)
            // if it is drawn is it allowed? (it is also possible that this has never been started, so let's assume
            // it is valid)
            .map(entry -> stillAllowedActionsInPlan.get(entry.getKey()))
            .anyMatch(allowedActions -> allowedActions == null || allowedActions.intValue() > 0);
    }

    @Override
    public void turnOff(final Fisher fisher) {
        this.fisher = null;
        model = null;
    }

    /**
     * Returns whether the action is still allowed according to the counters in the {@code stillAllowedActionsInPlan}
     * map. If there is no entry for this type of action, we assume it's allowed, which is arguably not the most
     * intuitive way of doing things, so perhaps we could consider changing it if there ever is an opportunity.
     */
    private boolean stillAllowedInPlan(final ActionType actionType) {
        return stillAllowedActionsInPlan
            .getOrDefault(actionType, new MutableInt(1))
            .intValue() > 0;
    }

    private Plan planRecursively(
        final Plan currentPlan,
        double hoursLeftInBudget,
        final FishState model,
        final Fisher fisher
    ) {

        // if there are no possible actions, stop
        if (!isAnyActionEvenPossible())
            return currentPlan;

        // If we have preference overrides in place, pick the first legal one;
        // otherwise draw a random next action.
        final ActionType nextActionType = actionPreferenceOverrides
            .stream()
            .filter(this::stillAllowedInPlan)
            .findAny()
            .orElse(drawNextAction(model.getRandom()));

        // you can't draw any actions, the plan is over!
        if (nextActionType == null)
            return currentPlan;

        // ask the planning module for an action to add to the path
        final PlanningModule planningModule = planModules.get(nextActionType);
        // might be the first time you call it, so get it ready
        readyPlanningModule(nextActionType, fisher, model);
        // it is possible that even though it's the first time we try this action, we actually can't do
        // if so star over
        if (stillAllowedActionsInPlan.get(nextActionType).intValue() <= 0)
            return planRecursively(currentPlan, hoursLeftInBudget, model, fisher);

        final PlannedAction plannedAction = planningModule.chooseNextAction(currentPlan);

        // if the planning module cannot propose more actions, ignore them for this plan
        if (plannedAction == null || (doNotWaitToPurgeIllegalActions && !plannedAction.isAllowedNow(fisher))) {
            // If there is an attempt to add an illegal action to the plan, we don't allow any more actions of
            // that type to be added. We're leaving that bit of code in for now, but we're questioning whether
            // it's necessary. It caused problem when attempts to add FAD sets in, e.g., EEZs; preventing further
            // FAD sets to be added. We fixed that by filtering out illegal FAD sets "upstream" (in the
            // OwnFadSetDiscretizedActionGenerator) but we should be careful with this -- NP 2023-11-14.
            stillAllowedActionsInPlan.get(nextActionType).setValue(0);
            // try planning more
            return planRecursively(currentPlan, hoursLeftInBudget, model, fisher);
        } else {
            // there is an action and we need to take it
            final double hoursConsumed =
                cheapestInsert(
                    currentPlan,
                    plannedAction,
                    hoursLeftInBudget,
                    fisher.getBoat().getSpeedInKph(),
                    model.getMap(),
                    true
                );
            if (Double.isNaN(hoursConsumed))
                // went over budget! our plan is complete
                return currentPlan;
            else {
                stillAllowedActionsInPlan.get(nextActionType).decrement();
                hoursLeftInBudget = hoursLeftInBudget - hoursConsumed;
                assert hoursLeftInBudget >= -FishStateUtilities.EPSILON;
                if (hoursLeftInBudget <= 0)
                    return currentPlan;
                else
                    return planRecursively(currentPlan, hoursLeftInBudget, model, fisher);
            }
        }

    }

    private ActionType drawNextAction(final MersenneTwisterFast random) {
        final List<Pair<ActionType, Double>> pmf =
            plannableActionWeights
                .entrySet()
                .stream()
                .filter(entry -> stillAllowedInPlan(entry.getKey()))
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .collect(toList());
        return pmf.size() == 1
            ? pmf.get(0).getKey()
            : new EnumeratedDistribution<>(new MTFApache(random), pmf).sample();
    }

    public Plan planNewTrip() {
        assert fisher.isAtPort();
        assert fisher.isAllowedAtSea();
        assert fisher.getLocation() == fisher.getHomePort().getLocation();

        // create an empty plan (circling back home)
        currentPlan = new Plan(fisher.getLocation(), fisher.getLocation());

        // start planning
        stillAllowedActionsInPlan.clear();
        thisTripTargetHours = maxHoursPerTripGenerator.applyAsDouble(model.getRandom());
        currentPlan = planRecursively(currentPlan, thisTripTargetHours, model, fisher);

        return currentPlan;
    }

    public Plan replan(final double hoursAlreadySpent) {

        Preconditions.checkState(
            fisher.getLocation() != fisher.getHomePort().getLocation() ||
                fisher.getHoursAtSea() == 0
        );

        // the new plan will remove all previous actions except for DPLs (if any)
        // which are constraints we don't want to move
        final SeaTile lastPlanLocation = fisher.getLocation();
        final NauticalMap map = model.getMap();
        final double speed = fisher.getBoat().getSpeedInKph();
        final Plan newPlan = new Plan(fisher.getLocation(), fisher.getHomePort().getLocation());

        // now take into consideration the very last step (return to port)
        final double lastStepCost = map.distance(lastPlanLocation, newPlan.peekLastAction().getLocation()) / speed;

        // length of the trip is reduced by how much we have already spent outside and duration of that last step
        double hoursAvailable = getThisTripTargetHours() - hoursAlreadySpent - lastStepCost;

        if (hoursAvailable > 0) {

            // Reset the number of allowed actions of each type
            planModules.forEach((actionType, planningModule) -> {
                final MutableInt actionsAllowed = stillAllowedActionsInPlan.get(actionType);
                if (actionsAllowed == null) {
                    // if no action of that type has ever been drawn, the planning module has not been started,
                    // so we let readyPlanningModule initialise everything
                    readyPlanningModule(actionType, fisher, model);
                } else {
                    // otherwise we just mutate the existing entry in actionsAllowed
                    actionsAllowed.setValue(planningModule.maximumActionsInAPlan(model, fisher));
                }
            });

            final Iterator<PlannedAction> previouslyPlannedDeployments = currentPlan
                .plannedActions()
                .stream()
                .filter(PlannedAction.Deploy.class::isInstance)
                .iterator();
            final MutableInt deploymentsAllowed = stillAllowedActionsInPlan.get(ActionType.DeploymentAction);
            while (
                hoursAvailable > 0 &&
                    deploymentsAllowed != null &&
                    deploymentsAllowed.getValue() > 0 &&
                    previouslyPlannedDeployments.hasNext()
            ) {
                final double hoursConsumed = cheapestInsert(
                    newPlan,
                    previouslyPlannedDeployments.next(),
                    hoursAvailable,
                    speed,
                    map,
                    true
                );
                if (!Double.isFinite(hoursConsumed)) {
                    hoursAvailable = 0;
                } else {
                    deploymentsAllowed.decrement();
                    hoursAvailable -= hoursConsumed;
                }
            }
        }

        // add more events now.
        currentPlan = newPlan;
        if (hoursAvailable > 0) {
            planModules.values().forEach(module -> module.prepareForReplanning(model, fisher));
            currentPlan = planRecursively(currentPlan, hoursAvailable, model, fisher);
        }
        return currentPlan;
    }

    public double getThisTripTargetHours() {
        return thisTripTargetHours;
    }

}
