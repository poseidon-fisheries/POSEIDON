/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2024-2024 CoHESyS Lab cohesys.lab@gmail.com
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.utility.MTFApache;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.*;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.ac.ox.poseidon.common.core.Entry.entry;

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

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int MAX_FAILED_ATTEMPTS_WITH_OVERRIDE = 500;
    /**
     * need this to generate your budget in hours
     */
    private final DoubleParameter maxHoursPerTripGenerator;
    /**
     * mapping that for each action returns the probability of it occurring
     */
    final private Map<ActionType, Double> actionPreferences;
    /**
     * mapping that for each action returns the planning module giving us one candidate action to take
     */
    final private Map<ActionType, PlanningModule> planningModules;

    /**
     * Actions of types listed here will be added to the plan first, before preferences are taken into consideration.
     * The first use case for this is forcing fishers to deploy FADs until they reach their active-FADs limits.
     */
    private List<ActionType> actionPreferenceOverrides = ImmutableList.of();
    private Fisher fisher;
    private FishState fishState;
    private double thisTripTargetHours = 0;

    public DrawThenCheapestInsertionPlanner(
        final DoubleParameter maxHoursPerTripGenerator,
        final Map<ActionType, Double> actionPreferences,
        final Map<ActionType, PlanningModule> planningModules
    ) {
        this.maxHoursPerTripGenerator = maxHoursPerTripGenerator;
        this.actionPreferences = actionPreferences;
        this.planningModules = planningModules;
    }

    public Map<ActionType, PlanningModule> getPlanningModules() {
        return planningModules;
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
        this.fishState = model;
    }

    @Override
    public void turnOff(final Fisher fisher) {
        this.fisher = null;
        this.fishState = null;
    }

    public Plan planNewTrip() {
        final SeaTile portLocation = fisher.getHomePort().getLocation();
        assert fisher.isAtPort();
        assert fisher.isAllowedAtSea();
        assert fisher.getLocation() == portLocation;
        thisTripTargetHours = maxHoursPerTripGenerator.applyAsDouble(fishState.getRandom());
        return makePlan(portLocation, portLocation, thisTripTargetHours);
    }

    private Plan makePlan(
        final SeaTile initialPosition,
        final SeaTile finalPosition,
        final double hoursAvailable
    ) {
        this.planningModules.values().forEach(module -> module.prepareForReplanning(fishState, fisher));
        final Map<ActionType, MutableLong> permittedActions = initPermittedActions();
        final Plan plan = new Plan(initialPosition, finalPosition);
        double hoursUsed =
            fishState.getMap().distance(initialPosition, finalPosition) /
                fisher.getBoat().getSpeedInKph();
        int failedAttempts = 0;
        final int maxFailedAttempts =
            actionPreferenceOverrides.isEmpty()
                ? MAX_FAILED_ATTEMPTS
                : MAX_FAILED_ATTEMPTS_WITH_OVERRIDE;
        while (
            failedAttempts < maxFailedAttempts &&
                hoursUsed < hoursAvailable &&
                sumValues(permittedActions) > 0
        ) {
            final Set<ActionType> permittedActionTypes =
                permittedActions
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().longValue() > 0)
                    .map(Entry::getKey)
                    .collect(toSet());

            // If we have preference overrides in place, pick the first permitted one;
            // otherwise draw a random next action.
            final ActionType nextActionType =
                actionPreferenceOverrides
                    .stream()
                    .filter(permittedActionTypes::contains)
                    .findAny()
                    .orElse(drawNextAction(permittedActionTypes));

            final double hoursCurrentlyAvailable = hoursAvailable - hoursUsed;

            // the planning modules might return null if they can't generate an action
            final PlanningModule planningModule = planningModules.get(nextActionType);
            final PlannedAction action = planningModule.chooseNextAction(plan);

            // we should never be exceeding the number of available actions,
            // but there are other reasons why a disallowed action might be picked
            // (e.g., a new MPA covering locations valued by the fisher)
            Optional<Double> insertionCost = Optional.empty();
            if (action != null && action.isAllowedNow(fisher)) {
                insertionCost = cheapestInsert(
                    plan,
                    action,
                    hoursCurrentlyAvailable,
                    fisher.getBoat().getSpeedInKph(),
                    fishState.getMap(),
                    true
                );
            }

            if (insertionCost.isPresent()) {
                hoursUsed = hoursUsed + insertionCost.get();
                permittedActions.get(nextActionType).decrement();
            } else {
                // we might fail because we don't have enough hours left to insert
                // new actions or because we keep on generating illegal actions.
                failedAttempts += 1;
                // If we have an action but were unable to insert it into the plan
                // remove its location from the planning module to prevent trying it again
                if (action != null && planningModule instanceof LocationValuePlanningModule) {
                    ((LocationValuePlanningModule) planningModule).removeLocation(action.getLocation());
                }
            }
        }
        return plan;
    }

    /**
     * Returns a map from action types to number of permitted actions, taking into action preferences and regulations.
     * Only action types with one or more permitted actions are included in the map.
     */
    private Map<ActionType, MutableLong> initPermittedActions() {
        return actionPreferences
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() > 0)
            .map(Entry::getKey)
            .map(actionType -> entry(
                actionType,
                new MutableLong(
                    planningModules
                        .get(actionType)
                        .numberOfPermittedActions(fisher, fishState.getRegulations())
                )
            ))
            .filter(entry -> entry.getValue().longValue() > 0)
            .collect(toImmutableMap(Entry::getKey, Entry::getValue));
    }

    private static long sumValues(final Map<?, ? extends Number> numberMap) {
        return numberMap.values().stream().mapToLong(Number::longValue).sum();
    }

    private ActionType drawNextAction(
        final Collection<ActionType> permittedActionTypes
    ) {
        if (permittedActionTypes.size() == 1)
            return permittedActionTypes.iterator().next();
        final List<Pair<ActionType, Double>> pmf =
            permittedActionTypes
                .stream()
                .map(actionType ->
                    new Pair<>(actionType, actionPreferences.get(actionType))
                )
                .collect(toList());
        return new EnumeratedDistribution<>(new MTFApache(fishState.getRandom()), pmf).sample();
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
    static Optional<Double> cheapestInsert(
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
            return Optional.of(totalCostInHours);
        } else {
            return Optional.empty();
        }

    }

    public Plan replan(final double hoursAlreadySpent) {

        Preconditions.checkState(
            fisher.getLocation() != fisher.getHomePort().getLocation() ||
                fisher.getHoursAtSea() == 0
        );

        return makePlan(
            fisher.getLocation(),
            fisher.getHomePort().getLocation(),
            thisTripTargetHours - hoursAlreadySpent
        );
    }

    double getThisTripTargetHours() {
        return thisTripTargetHours;
    }

}
