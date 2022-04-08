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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.MTFApache;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.*;

/**
 * A simple planner where given a budget of time you can spend out:
 * 1 - Draw the next action to take from random distribution
 * 2 - Use a generator to turn that action into a planned action (or a set of possible choices)
 * 3 - If you have multiple choices (as for example with setting on FADs) pick the one whose profit/distance(centroid) is maximum
 * 4 - Add that planned action in your path so that it's the cheapest insertion possible
 * 5 - Keep doing this until you run out of budget
 *
 *
 * <br>
 * hardcoded in many parts, but get it to work first and judge computational complexity first
 */
public class DrawThenCheapestInsertionPlanner implements FisherStartable {

    private Plan currentPlan;

    /**
     * deployment location values is needed if the planner needs to do DPL actions; otherwise don't bother
     */
    private DeploymentLocationValues deploymentLocationValues;

    /**
     * need this to generate your budget in hours
     */
    private final DoubleParameter maxHoursPerTripGenerator;

    /**
     * mapping that for each action returns the probability of it occurring
     */
    final private Map<ActionType,Double> plannableActionWeights;

    /**
     * mapping that for each action returns the planning module giving us one candidate action to take
     */
    final private Map<ActionType,PlanningModule> planModules;

    /**
     * maximum number of times an action is still allowed to be added to the plan!
     */
    final private Map<ActionType, MutableInt> stillAllowedActionsInPlan = new HashMap<>();

    private Fisher fisher;

    private FishState model;

    public DrawThenCheapestInsertionPlanner(
            DoubleParameter maxHoursPerTripGenerator,
            Map<ActionType, Double> plannableActionWeights,
            Map<ActionType, PlanningModule> planModules) {
        this.maxHoursPerTripGenerator = maxHoursPerTripGenerator;
        this.plannableActionWeights = plannableActionWeights;
        this.planModules = planModules;
    }


    @Override
    public void start(FishState model, Fisher fisher) {
        this.fisher = fisher;
        this.model = model;
    }


    /**
     * checks that the module has started, and that the "stillAllowedActionsInPlan" is filled correctly, etc...
     * @param action
     */
    private void readyPlanningModule(ActionType action, Fisher fisher, FishState model){

        assert plannableActionWeights.get(action)>0;

        //has it been initialized?
        if(!stillAllowedActionsInPlan.containsKey(action)){

            //find the module
            PlanningModule planningModule = planModules.get(action);
            Preconditions.checkArgument(planningModule!=null,
                                        "You have assigned weight to " + action.toString()+ " without any module associated to it");
            //start the planning module
            planningModule.start(model,fisher);
            //set maximum actions
            stillAllowedActionsInPlan.put(action,new MutableInt(planningModule.maximumActionsInAPlan(model,fisher)));
            //done!
        }


    }

    /**
     * returns true as long as at least one of the planning modules has not been started or there are still allowedActions for that type
     * @return
     */
    private boolean isAnyActionEvenPossible(){
        for (Map.Entry<ActionType, Double> actionTypeAndProbability : plannableActionWeights.entrySet()) {

            //can this action type be drawn?
            if(actionTypeAndProbability.getValue()>0)
            {
                //if it is drawn is it allowed? (it is also possible that this has never been started, so let's assume it is valid)
                MutableInt allowedActions = stillAllowedActionsInPlan.get(actionTypeAndProbability.getKey());

                if(allowedActions == null || allowedActions.intValue()>0)
                    return true;
            }
            else{
                assert actionTypeAndProbability.getValue() == 0;

            }


        }
        return false;

    }

    @Override
    public void turnOff(Fisher fisher) {
        this.fisher=null;
        model=null;
    }


    public DeploymentLocationValues getDeploymentLocationValues() {
        return deploymentLocationValues;
    }

    public void setDeploymentLocationValues(
            DeploymentLocationValues deploymentLocationValues) {
        this.deploymentLocationValues = deploymentLocationValues;
    }

    private Plan planRecursively(Plan currentPlan, double hoursLeftInBudget,
                                FishState model, Fisher fisher){

        //if there are no possible actions, stop
        if(!isAnyActionEvenPossible())
            return currentPlan;
        //if there are some possible actions, do them

        //pick at random next action!

        //prepare pair list
        ActionType nextActionType = drawNextAction(model.getRandom());

        //you can't draw any actions, the plan is over!
        if(nextActionType==null)
            return currentPlan;

        //ask the planning module for an action to add to the path
        PlanningModule planningModule = planModules.get(nextActionType);
        //might be the first time you call it, so get it ready
        readyPlanningModule(nextActionType,fisher,model);
        PlannedAction plannedAction = planningModule.chooseNextAction(currentPlan);

        //if the planning module cannot propose more actions, ignore them for this plan
        if(plannedAction==null)
        {
            stillAllowedActionsInPlan.get(nextActionType).setValue(0);
            //try planning more
            return planRecursively(currentPlan, hoursLeftInBudget, model, fisher);
        }
        else{
            //there is an action and we need to take it
            double hoursConsumed =
                    cheapestInsert(currentPlan,plannedAction,hoursLeftInBudget,fisher.getBoat().getSpeedInKph(),
                           model.getMap());
            if(Double.isNaN(hoursConsumed))
                //went overbudget! our plan is complete
                return currentPlan;
            else
            {
                hoursLeftInBudget =  hoursLeftInBudget-hoursConsumed;
                assert hoursLeftInBudget >= -FishStateUtilities.EPSILON;
                if(hoursLeftInBudget<=0)
                    return currentPlan;
                else
                    return planRecursively(currentPlan, hoursLeftInBudget,
                                           model, fisher);
            }
        }



    }

    private ActionType drawNextAction(MersenneTwisterFast random) {
        List<Pair<ActionType, Double>> toDraw = new ArrayList<>(plannableActionWeights.size());
        for (Map.Entry<ActionType, Double> actionsAvailable : plannableActionWeights.entrySet()) {
            boolean allowed = stillAllowedActionsInPlan.getOrDefault(
                    actionsAvailable.getKey(),
                    new MutableInt(1)).intValue()>0;
            if(!allowed)
                continue;
            toDraw.add(new Pair<>(actionsAvailable.getKey(),actionsAvailable.getValue()));
        }
        //feed it to the enumerated distribution
        ActionType nextAction = null;
        if(toDraw.size()==1)
            nextAction = toDraw.get(0).getKey();
        else{
            nextAction = new EnumeratedDistribution<ActionType>(new MTFApache(random), toDraw).sample();
        }
        return nextAction;
    }

    public Plan planNewTrip(){
        assert fisher.isAtPort();
        assert fisher.isAllowedAtSea();
        assert fisher.getLocation() == fisher.getHomePort().getLocation();

        //create an empty plan (circling back home)
        currentPlan = new Plan(fisher.getLocation(),
                               fisher.getLocation());
        //start planning
        stillAllowedActionsInPlan.clear();
        currentPlan = planRecursively(currentPlan, maxHoursPerTripGenerator.apply(model.getRandom()),
                                      model, fisher);

        return currentPlan;
    }

    public Plan replan(){
        Preconditions.checkArgument(fisher.getLocation() != fisher.getHomePort().getLocation());

        //length of the trip is reduced by how much we have already spent outside
        double hoursAvailable =maxHoursPerTripGenerator.apply(model.getRandom());
        hoursAvailable = hoursAvailable - fisher.getCurrentTrip().getDurationInHours();

        //the new plan will remove all previous actions except for DPLs (if any)
        //which are constraints we don't want to move
        SeaTile lastPlanLocation = fisher.getLocation();
        NauticalMap map = model.getMap();
        double speed = fisher.getBoat().getSpeedInKph();
        Plan newPlan = new Plan(fisher.getLocation(),
                                fisher.getHomePort().getLocation());

        for (PlannedAction plannedAction : currentPlan.lookAtPlan()) {
            if(plannedAction instanceof PlannedAction.Deploy) {
                double hoursConsumedTravelling = map.distance(lastPlanLocation,plannedAction.getLocation()) / speed;
                double actionDuration = plannedAction.hoursItTake();
                if(hoursAvailable>= hoursConsumedTravelling+actionDuration){
                    newPlan.insertAction(plannedAction, newPlan.numberOfStepsInPath()-1,
                            hoursConsumedTravelling+actionDuration);
                    hoursAvailable -=  hoursConsumedTravelling+actionDuration;
                    lastPlanLocation = plannedAction.getLocation();
                }

            }
        }
        //now take into consideration the very last step (return to port)
        double lastStepCost = map.distance(lastPlanLocation, newPlan.peekLastAction().getLocation()) / speed;
        hoursAvailable -= lastStepCost;
        newPlan.addHoursSpent(lastStepCost);
        //do not allow more DPL
        stillAllowedActionsInPlan.put(ActionType.DeploymentAction,new MutableInt(0));
        assert hoursAvailable>=0;

        //add more events now.
        currentPlan = newPlan;
        currentPlan = planRecursively(currentPlan, hoursAvailable,
                                      model, fisher);

        return currentPlan;

    }

    /**
     * adds the path to the current plan and returns the hours it takes (duration + movement); returns NaN if we go overbudget!
     * As a <b>side effect</b> it will <b>add the action to the plan</b> if it is within budget! <br>
     *
     * No real reason to keep it static except that it is easier to test that way.
     * @param currentPlan the plan as it is now
     * @param actionToAddToPath the planned action to add to the plan
     * @param hoursAvailable how many hours we still have available for this trip
     * @param speed the speed of the boat (distance/time)
     * @param map nautical map for distance calculation
     * @return the hours we consumed adding the action to the plan
     */
    @VisibleForTesting
    public static double cheapestInsert(
            Plan currentPlan,
            PlannedAction actionToAddToPath,
            double hoursAvailable,
            double speed,
            NauticalMap map){
        Preconditions.checkArgument(hoursAvailable>0);
        Preconditions.checkArgument(speed>0);
        Preconditions.checkArgument(currentPlan.numberOfStepsInPath()>=2, "the path is too short, I'd expect here to be at least two steps");
        assert  (actionToAddToPath.getLocation()!=null) : "Action " + actionToAddToPath + " has no path!";

        //go through all options
        double bestInsertionCost = Double.MAX_VALUE;
        int bestIndex = -1;
        //you don't want to insert it in the beginning and you don't want to insert it at the end
        ListIterator<PlannedAction> iterator = currentPlan.lookAtPlan().listIterator(1);
        //for each possible insertion point, adding point C between A and B has insertion cost equal to d(A,C)+d(C,B)-d(A,B);
        //this distance ought to be positive due to triangle inequality
        for (int index = 1; index < currentPlan.numberOfStepsInPath(); index++) {


            PlannedAction from = iterator.previous();
            iterator.next(); //this bring you back to start
            PlannedAction to = iterator.next();

            //it is never optimal to squeeze yourself between two actions that take place in the same spot unless you are also in that spot
            if(currentPlan.numberOfStepsInPath() > 2 && from.getLocation() == to.getLocation() && from.getLocation() != actionToAddToPath.getLocation())
                continue;

            double firstSegment = map.distance(from.getLocation(),actionToAddToPath.getLocation());
            if(firstSegment == 0){
                //if you are trying to insert in the same cell, this is surely the cheapest insert!
                bestInsertionCost = 0;
                bestIndex = index;
                break;
            }
            double secondSegment = map.distance(actionToAddToPath.getLocation(),to.getLocation());
            double replacedSegment = map.distance(from.getLocation(),to.getLocation());

            double insertionCost = (firstSegment+secondSegment-replacedSegment)/speed; //turn insertion cost into time
            assert insertionCost >= 0 : "triangle inequality does not seem to hold here. Bizarre";

            if(insertionCost<bestInsertionCost) {
                bestInsertionCost = insertionCost;
                bestIndex = index;
            }

        }
        double totalCostInHours = bestInsertionCost + actionToAddToPath.hoursItTake();
        if(totalCostInHours <= hoursAvailable)
        {
            assert bestIndex>0;
            currentPlan.insertAction(actionToAddToPath,bestIndex,totalCostInHours );
            return totalCostInHours;
        }
        else{
            return Double.NaN;
        }

    }

}
