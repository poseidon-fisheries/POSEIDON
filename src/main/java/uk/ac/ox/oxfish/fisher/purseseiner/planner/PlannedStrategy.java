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
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.actions.Moving;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishUntilFullStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

/**
 * a combined destination and fishing strategy: it uses a planner to produce a full path of actions to take
 * then walks over this path.
 * It has a planning horizon so that it can re-plan at regular intervals to avoid the plan from getting stale.
 */
public class PlannedStrategy implements DestinationStrategy, FishingStrategy {


    /**
     * this guy draws the plans when asked and holds the plan
     */
    final private DrawThenCheapestInsertionPlanner planner;

    /**
     * the plan we are currently taking
     */
    private Plan currentPlan;

    /**
     * timestamp ( in how many hours had passed since this trip began) of last time we planned
     */
    private double hoursInTheTripSinceWeLastReplanned = 0;

    /**
     * how many hours do we want to wait before calling a replan?
     */
    final private double planningHorizonInHours;


    final FishUntilFullStrategy delegate = new FishUntilFullStrategy(1.0);

    //this get activated when we finished our last action
    private PlannedAction actionInProgress;
    //these get activated when we are at location and are performing whatever we said we were going to perform
    private Action[] actionQueueInProgress;
    private int actionQueueIndex = -1;

    public PlannedStrategy(DrawThenCheapestInsertionPlanner planner, double planningHorizonInHours) {
        this.planner = planner;
        this.planningHorizonInHours = planningHorizonInHours;
    }


    private void planNewTrip(){
        hoursInTheTripSinceWeLastReplanned = 0;
        currentPlan = planner.planNewTrip();
        assert currentPlan.numberOfStepsInPath()>=2;
        //the first step is always just "beginning of the trip"
        //so we don't need to act on it
        assert currentPlan.peekNextAction() instanceof PlannedAction.Arrival;
        assert !((PlannedAction.Arrival)currentPlan.peekNextAction()).isEndOfTrip();
        currentPlan.pollNextAction();//skip the start

        //set yourself for an action in progress
        actionInProgress = currentPlan.pollNextAction();
        resetActionQueue();
    }


    private void replan(Fisher fisher, FishState state){
        assert fisher.getCurrentTrip() != null;
        assert fisher.getCurrentTrip().getDurationInHours() > 0;
        assert !fisher.getCurrentTrip().isCompleted();

        //asked to replan, let's do it
        hoursInTheTripSinceWeLastReplanned = fisher.getCurrentTrip().getDurationInHours();
        currentPlan = planner.replan();

        //again, the first action is just a marker so we can safely skip it
        assert currentPlan.peekNextAction() instanceof PlannedAction.Arrival;
        assert !((PlannedAction.Arrival)currentPlan.peekNextAction()).isEndOfTrip();
        currentPlan.pollNextAction();//skip the start

        //set yourself for an action in progress
        actionInProgress = currentPlan.pollNextAction();
        resetActionQueue();


    }

    private void resetActionQueue() {
        actionQueueInProgress = null;
        actionQueueIndex = -1;
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        planner.start(model, fisher);

    }

    @Override
    public void turnOff(Fisher fisher) {
        planner.turnOff(fisher);
    }

    /**
     * This is called by Arriving.act to decide whether or not to fish up arrival. Most fishing
     * strategies should use this default implementation, but FAD fishing strategies are expected to
     * override this method and result in action types other than `Fishing`.
     *
     * @param model
     * @param agent
     * @param regulation
     * @param hoursLeft
     */
    @Override
    public ActionResult act(
            FishState model, Fisher agent, Regulation regulation, double hoursLeft) {

        //this gets called by arrival, so we have to make sure we are where we want to be
        assert agent.getLocation() == actionInProgress.getLocation();
        //we may have just arrived, if so get the queue of actions we need to take
        if(actionQueueInProgress == null)
            actionQueueInProgress = actionInProgress.actuate(agent);
        actionQueueIndex++;
        //okay, are there still actions to take? if so take it!
        if(actionQueueIndex<actionQueueInProgress.length)
            return
                    new ActionResult(actionQueueInProgress[actionQueueIndex],hoursLeft);
        else{
            //you have finished the queue!
            resetActionQueue();
            //is it time for a replan?
            if((model.getHoursSinceStart()-this.hoursInTheTripSinceWeLastReplanned)>planningHorizonInHours)
            {
                replan(agent,model); //this will automatically move to new next action
            }
            else {
                //move to next action
                actionInProgress = currentPlan.pollNextAction();
            }
            //if it's here, start over
            if(actionInProgress.getLocation() == agent.getLocation())
                return act(model, agent, regulation, hoursLeft);
            //otherwise move
            return
                    new ActionResult(new Moving(), hoursLeft);
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
            Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {
        //are we just departing? make new plan!
        if(currentAction instanceof AtPort)
            planNewTrip();
        else
        if(
                !actionInProgress.isAllowedNow(fisher) ||
                        actionInProgress.getLocation() == null) {
            //if the action is now not allowed or invalid, replan
            replan(fisher,model);
        }
        //otherwise, you are going where the plan tells you to
        return actionInProgress.getLocation();
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
            Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip) {
        return delegate.shouldFish(fisher, random, model, currentTrip);
    }

    //todo use this
    public boolean doIJustWantToGoHome(Fisher fisher){
        return fisher.getTotalWeightOfCatchInHold() + EPSILON <
                fisher.getMaximumHold();
    }




}