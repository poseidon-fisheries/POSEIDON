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
import org.apache.commons.lang3.mutable.MutableInt;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

/**
 * A simple planner where given a budget of time you can spend out:
 * 1 - Draw the next action to take from random distribution
 * 2 - Use a generator to turn that action into a planned action (or a set of possible choices)
 * 3 - If you have multiple choices (as for example with setting on FADs) pick the one whose profit/distance(centroid) is maximum
 * 4 - Add that planned action in your path so that it's the cheapest insertion possible
 * 5 - Keep doing this until you run out of budget
 *
 *
 * //todo if this works, we need replanning
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
        this.model=null;
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

    public Plan keepPlanning(Plan currentPlan, double hoursLeftInBudget){

        //if there are no possible actions, stop
        if(!isAnyActionEvenPossible())
            return currentPlan;
        //if there are some possible actions, do them
        throw new RuntimeException("not coded yet");


    }

    public Plan planNewTrip(){
        assert fisher.isAtPort();
        assert fisher.isAllowedAtSea();

        //todo

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

        //go through all options
        double bestInsertionCost = Double.MAX_VALUE;
        int bestIndex = -1;
        //you don't want to insert it in the beginning and you don't want to insert it at the end
        ListIterator<PlannedAction> iterator = currentPlan.lookAtPlan().listIterator(1);
        //for each possible insertion point, adding point C between A and B has insertion cost equal to d(A,C)+d(C,B)-d(A,B);
        //this distance ought to be positive due to triangle inequality
        for (int index = 1; index <= currentPlan.numberOfStepsInPath(); index++) {

            PlannedAction from = iterator.previous();
            PlannedAction to = iterator.next();
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
            iterator.next(); //move to the next
        }
        assert bestIndex>0;
        double totalCostInHours = bestInsertionCost + actionToAddToPath.hoursItTake();
        if(totalCostInHours <= hoursAvailable)
        {
            currentPlan.insertAction(actionToAddToPath,bestIndex);
            return totalCostInHours;
        }
        else{
            return Double.NaN;
        }

    }

}
