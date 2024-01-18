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
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.*;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishUntilFullStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

/**
 * a combined destination and fishing strategy: it uses a planner to produce a full path of actions to take then walks
 * over this path. It has a planning horizon so that it can re-plan at regular intervals to avoid the plan from getting
 * stale.
 */
public class PlannedStrategy implements DestinationStrategy, FishingStrategy {

    final FishUntilFullStrategy delegate = new FishUntilFullStrategy(1.0);
    private final double minimumValueOfSetOnOwnFad;
    /**
     * this guy draws the plans when asked and holds the plan
     */
    final private DrawThenCheapestInsertionPlanner planner;
    /**
     * how many hours do we want to wait before calling a replan?
     */
    final private double planningHorizonInHours;
    /**
     * the plan we are currently taking
     */
    private Plan currentPlan;
    /**
     * timestamp ( in how many hours had passed since this trip began) of last time we planned
     */
    private double timestampInHoursWhenTripStarted = 0;
    /**
     * timestamp ( in how many hours had passed since this trip began) of last time we planned
     */
    private double hoursInTheTripSinceWeLastReplanned = 0;
    // this get activated when we finished our last action
    private PlannedAction actionInProgress;
    // these get activated when we are at location and are performing whatever we said we were going to perform
    private Action[] actionQueueInProgress;
    private int actionQueueIndex = -1;

    public PlannedStrategy(
        final DrawThenCheapestInsertionPlanner planner,
        final double planningHorizonInHours,
        final double minimumValueOfSetOnOwnFad
    ) {
        this.planner = planner;
        this.planningHorizonInHours = planningHorizonInHours;
        this.minimumValueOfSetOnOwnFad = minimumValueOfSetOnOwnFad;
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
        planner.start(model, fisher);

    }

    @Override
    public void turnOff(final Fisher fisher) {
        planner.turnOff(fisher);
    }

    /**
     * This is called by Arriving.act to decide whether or not to fish up arrival. Most fishing strategies should use
     * this default implementation, but FAD fishing strategies are expected to override this method and result in action
     * types other than `Fishing`.
     */
    @Override
    public ActionResult act(
        final FishState model,
        final Fisher agent,
        final Regulation regulation,
        final double hoursLeft
    ) {

        // should not be called when going home is an override
        assert !doIJustWantToGoHome(agent);

        // this gets called by arrival, so we have to make sure we are where we want to be
        assert agent.getLocation() == actionInProgress.getLocation() ||
            // there is an exception here when we have just finished setting on a fad
            // which gets then destroyed: then the action location is null
            actionQueueInProgress[actionQueueIndex] instanceof FadSetAction &&
                actionInProgress instanceof PlannedAction.FadSet;

        // we may have just arrived, if so get the queue of actions we need to take
        if (actionQueueInProgress == null) {
            if (actionInProgress.isAllowedNow(agent))
                actionQueueInProgress = actionInProgress.actuate(agent);
            else
                actionQueueInProgress = new Action[]{};
        }
        actionQueueIndex++;
        // okay, are there still actions to take? if so take it!
        if (actionQueueIndex < actionQueueInProgress.length) {
            final Action actionToTake = actionQueueInProgress[actionQueueIndex];
            return agent.getGear().isSafe(actionToTake) && isStillDesirable(actionToTake)
                // If the action is safe to take, take it
                ? new ActionResult(actionToTake, hoursLeft)
                // Otherwise move on to next action
                : act(model, agent, regulation, hoursLeft);
        } else {
            // you have finished the queue!
            resetActionQueue();
            // is it time for a replan?
            if (
                model.getHoursSinceStart() - hoursInTheTripSinceWeLastReplanned > planningHorizonInHours &&
                    agent.getLocation() != agent.getHomePort().getLocation()
            ) {
                replan(agent, model); // this will automatically move to new next action
            } else {
                // move to next action
                actionInProgress = currentPlan.pollNextAction();
            }
            // if it's here, start over
            if (actionInProgress.getLocation() == agent.getLocation()) {
                return act(model, agent, regulation, hoursLeft);
            } else {
                // otherwise move
                return new ActionResult(new Moving(), hoursLeft);
            }
        }

    }

    private boolean isStillDesirable(final Action actionToTake) {
        if (actionToTake instanceof FadSetAction) {
            final FadSetAction fadSetAction = (FadSetAction) actionToTake;
            final Fad fad = fadSetAction.getFad();
            final Fisher fisher = fadSetAction.getFisher();
            final double[] prices = fisher.getHomePort().getMarketMap(fisher).getPrices();
            final double fadValue = getFadManager(fisher).getFishValueCalculator().valueOf(fad.getBiology(), prices);
            return fadValue >= minimumValueOfSetOnOwnFad;
        } else {
            return true;
        }
    }

    /**
     * decides where to go.
     *
     * @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination
     * @return the destination
     */
    @Override
    public SeaTile chooseDestination(
        final Fisher fisher,
        final MersenneTwisterFast random,
        final FishState model,
        final Action currentAction
    ) {
        // go home override
        if (fisher.getLocation() != fisher.getHomePort().getLocation() && doIJustWantToGoHome(fisher))
            return fisher.getHomePort().getLocation();

        // are we just departing? make new plan!
        if (currentAction == null || currentAction instanceof AtPort)
            planNewTrip(model);
        else if (
            // if the action is now not allowed or invalid, replan
            actionInProgress.getLocation() == null ||
                (!actionInProgress.isAllowedNow(fisher))
        ) {

            // check for the case when you just finished setting a fad that was destroyed (by you or others)
            // in that case it will say that the location is unknown, but it you just need to be here a second longer
            if (
                actionQueueInProgress != null &&
                    actionQueueInProgress[actionQueueIndex] instanceof FadSetAction &&
                    currentAction instanceof Arriving
            ) {
                return fisher.getLocation();
            }

            // unless you are at port (probably because you beelined here after you were told to go home)
            if (
                fisher.getLocation() != fisher.getHomePort().getLocation() ||
                    fisher.getHoursAtSea() <= 0
            ) {
                replan(fisher, model);
            } else {
                return fisher.getHomePort().getLocation();
            }
        }
        // otherwise, you are going where the plan tells you to
        return actionInProgress.getLocation();
    }

    /**
     * if the hold is full he plan is meaningless: go home
     *
     * @param fisher
     * @return
     */
    public boolean doIJustWantToGoHome(final Fisher fisher) {
        final boolean amIFull = fisher.getTotalWeightOfCatchInHold() + EPSILON >=
            fisher.getMaximumHold();
        final boolean isItTooLate =
            actionInProgress != null &&
                !(actionInProgress instanceof PlannedAction.Arrival) &&
                planner.getThisTripTargetHours() > 0 &&
                planner.getThisTripTargetHours() < computeCurrentTripDurationInHours(
                    fisher.grabState());
        return amIFull || isItTooLate;
    }

    private void planNewTrip(final FishState model) {
        hoursInTheTripSinceWeLastReplanned = model.getHoursSinceStart();
        timestampInHoursWhenTripStarted = model.getHoursSinceStart();
        currentPlan = planner.planNewTrip();
        assert currentPlan.numberOfStepsInPath() >= 2;
        // the first step is always just "beginning of the trip"
        // so we don't need to act on it
        assert currentPlan.peekNextAction() instanceof PlannedAction.Arrival;
        assert !((PlannedAction.Arrival) currentPlan.peekNextAction()).isEndOfTrip();
        currentPlan.pollNextAction();// skip the start

        // set yourself for an action in progress
        actionInProgress = currentPlan.pollNextAction();
        resetActionQueue();
    }

    private void replan(
        final Fisher fisher,
        final FishState state
    ) {
        assert fisher.getCurrentTrip() != null;
        assert fisher.getCurrentTrip().getTripDay() <= state.getDay();
        assert !fisher.getCurrentTrip().isCompleted();

        // asked to replan, let's do it
        final double computeCurrentTripDurationInHours = computeCurrentTripDurationInHours(state);
        hoursInTheTripSinceWeLastReplanned = state.getHoursSinceStart();
        currentPlan = planner.replan(computeCurrentTripDurationInHours);

        // again, the first action is just a marker so we can safely skip it
        assert currentPlan.peekNextAction() instanceof PlannedAction.Arrival;
        assert !((PlannedAction.Arrival) currentPlan.peekNextAction()).isEndOfTrip();
        currentPlan.pollNextAction();// skip the start

        // set yourself for an action in progress
        actionInProgress = currentPlan.pollNextAction();
        resetActionQueue();

    }

    private double computeCurrentTripDurationInHours(final FishState model) {
        return model.getHoursSinceStart() -
            this.timestampInHoursWhenTripStarted;
    }

    private void resetActionQueue() {
        actionQueueInProgress = null;
        actionQueueIndex = -1;
    }

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     *
     * @param fisher
     * @param random      the randomizer
     * @param model       the model itself
     * @param currentTrip
     * @return true if the fisher should fish here, false otherwise
     */
    @Override
    public boolean shouldFish(
        final Fisher fisher,
        final MersenneTwisterFast random,
        final FishState model,
        final TripRecord currentTrip
    ) {
        return delegate.shouldFish(fisher, random, model, currentTrip);
    }

}
