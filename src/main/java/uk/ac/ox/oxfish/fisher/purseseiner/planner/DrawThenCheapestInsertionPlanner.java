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
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.TunaEvaluator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.MTFApache;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private double thisTripTargetHours = 0;

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
        //it is possible that even though it's the first time we try this action, we actually can't do
        //if so star over
        if(stillAllowedActionsInPlan.get(nextActionType).intValue()<=0)
            return planRecursively(currentPlan, hoursLeftInBudget, model, fisher);

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
                stillAllowedActionsInPlan.get(nextActionType).decrement();
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
        thisTripTargetHours = maxHoursPerTripGenerator.apply(model.getRandom());
        currentPlan = planRecursively(currentPlan, thisTripTargetHours ,
                model, fisher);

        return currentPlan;
    }

    public Plan replan(double hoursAlreadySpent){
        Preconditions.checkArgument(fisher.getLocation() != fisher.getHomePort().getLocation() || fisher.getHoursAtSea()==0);

        //length of the trip is reduced by how much we have already spent outside
        double hoursAvailable = getThisTripTargetHours();
        hoursAvailable = hoursAvailable - hoursAlreadySpent;

        //the new plan will remove all previous actions except for DPLs (if any)
        //which are constraints we don't want to move
        SeaTile lastPlanLocation = fisher.getLocation();
        NauticalMap map = model.getMap();
        double speed = fisher.getBoat().getSpeedInKph();
        Plan newPlan = new Plan(fisher.getLocation(),
                fisher.getHomePort().getLocation());
        //now take into consideration the very last step (return to port)
        double lastStepCost = map.distance(lastPlanLocation, newPlan.peekLastAction().getLocation()) / speed;
        hoursAvailable -= lastStepCost;
        newPlan.addHoursEstimatedItWillTake(lastStepCost);

        if(hoursAvailable>=0) {
            for (PlannedAction plannedAction : currentPlan.lookAtPlan()) {
                if (plannedAction instanceof PlannedAction.Deploy) {
                    double hoursConsumed = cheapestInsert(newPlan, plannedAction, hoursAvailable, speed, map);
                    if (!Double.isFinite(hoursConsumed))
                        break;
                    hoursAvailable -= hoursConsumed;
                    assert hoursAvailable >= 0;
                    if (hoursAvailable <= 0)
                        break;

                }
            }
        }
        //reset valid actions
        for (Map.Entry<ActionType, MutableInt> allowedActions : stillAllowedActionsInPlan.entrySet()) {
            PlanningModule planningModule = planModules.get(allowedActions.getKey());
            if(planningModule!=null)
                allowedActions.getValue().setValue(
                        planningModule.maximumActionsInAPlan(model,fisher)
                );
        }
        //do not allow more DPL
        stillAllowedActionsInPlan.put(ActionType.DeploymentAction,new MutableInt(0));

        //random delays (chasing FADs off course, for example), it can happen to be completely off
        //assert hoursAvailable>=-FishStateUtilities.EPSILON : hoursAvailable;

        //add more events now.
        currentPlan = newPlan;
        if(hoursAvailable>0) {
            for (PlanningModule module : planModules.values()) {
                module.prepareForReplanning(model, fisher);
            }
            currentPlan = planRecursively(currentPlan, hoursAvailable,
                    model, fisher);
        }
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
            //tricky here because when you add the approximations from the curvature distance, if you are going on a straight line
            //in the projected map, you may actually be violating by tiny amounts the triangle inequality constraint
            //but we choose to ignore this
            //assert insertionCost >= -5 : "triangle inequality does not seem to hold here. Bizarre " + insertionCost;

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

    public double getThisTripTargetHours() {
        return thisTripTargetHours;
    }

    public void setThisTripTargetHours(double thisTripTargetHours) {
        this.thisTripTargetHours = thisTripTargetHours;
    }

//    public static void main(String[] args) throws IOException {
//        GenericOptimization.buildLocalCalibrationProblem(
//                Paths.get("docs/20220223 tuna_calibration/pathfinder3/zapperAge_local/calibration_logisticbeta_fd_zapperAge.yaml"),
//                new double[]{-4.700,-4.317,-9.726, 10.000,-5.696,-10.000,-3.364, 9.002,-4.908,-3.535,-10.000, 2.033, 5.465,-8.304,-2.033,-6.016,-1.467,-5.014,-4.505, 2.120, 6.360, 3.727, 10.000,-5.952,-7.052,-2.532},
//                "zapperAge_local",.2
//        );
//        GenericOptimization.buildLocalCalibrationProblem(
//                Paths.get("docs/20220223 tuna_calibration/pathfinder3/zapperAge_local/calibration_logisticbeta_fd_zapper.yaml"),
//                new double[]{
//                        -0.244,-5.551,-9.394, 1.927,-1.066,-10.000, 9.442, 8.502,-7.710,-3.389, 2.360, 6.431, 10.000,-0.977,-0.218, 6.725, 5.352,-2.716,-7.049, 2.578, 10.000, 4.774,-3.275,-0.764, 0.055,-8.100
//                },
//                "zapper_local",.3
//        );
//        GenericOptimization.buildLocalCalibrationProblem(
//                Paths.get("docs/20220223 tuna_calibration/pathfinder3/zapperlocal2/original.yaml"),
//                new double[]{
//                        5.928, 10.000,-4.629,-1.743,-7.331, 7.256, 4.467, 8.542, 2.461, 6.235,-0.453, 10.000,-0.762, 1.933, 10.000,-2.604, 3.901, 8.284,-5.620, 2.860, 1.831, 5.348,-10.000,-7.821, 2.593,-0.502
//                },
//                "zapper_local_again",.3
//        );
//        GenericOptimization.buildLocalCalibrationProblem(
//                Paths.get("docs/20220223 tuna_calibration/pathfinder3/zapper_expired/original.yaml"),
//                new double[]{
//                        3.553,-5.394,-10.000,-0.396,-6.772, 10.000, 3.984, 9.114,-5.820, 0.723,-0.827,-5.182,-4.600,-4.504, 2.559, 10.000, 4.596,-10.000, 10.000, 4.050, 10.000, 5.830, 3.143,-4.421, 3.034, 0.999, 1.545
//                },
//                "zapper_local_expired",.2
//        );
  //  }

    public static void main(String[] args) throws IOException {

//        double[] solution = {-3.498,-0.431,-5.375,-3.236,-1.976,-3.991,-6.029, 1.675,-5.013, 0.085, 2.348, 6.974,-6.651, 0.070, 7.658, 1.313,-6.153,-6.742, 5.033, 3.401, 2.288,-0.401, 4.435, 0.906,-5.929, 5.521,-2.117, 5.730};
//        Path calibrationFile = Paths.get("/home/carrknight/code/oxfish/docs/20220223 tuna_calibration/pathfinder3/local_experiment/temp/powpointone/local_1000_forceddiscretization.yaml");
//
//


//        double[] solution = {-1.732, 5.637,-0.049, 1.983,-0.496, 4.536,-5.810,-3.894,-7.138, 5.626, 5.600, 0.594, 4.095, 2.608, 1.965,-3.078,-4.655, 5.206, 5.751,-3.062, 6.612, 4.448, 4.792, 1.511,-6.183,-7.019, 1.016};
//        Path calibrationFile = Paths.get(
//                "docs/20220223 tuna_calibration/pathfinder3/local_experiment/fd/local_fd_125.yaml"
//        );


//        double[] solution = {0.562, 4.100,-4.186,-1.756, 3.606,-5.027, 0.635,-2.266, 1.438, 2.350,-0.368,-3.393,-2.957, 1.256, 4.433,-4.830,-2.005,-2.589,-0.782, 0.178, 0.110, 1.421, 0.591,-1.358, 2.359, 4.308};
//        Path calibrationFile = Paths.get(
//                "/home/carrknight/code/oxfish/docs/20220223 tuna_calibration/pathfinder3/local_experiment/fd/carrknight/2022-04-20_07.33.02_local1000/local_fd_125.yaml"
//        );

//        double[] solution = {5.928, 10.000,-4.629,-1.743,-7.331, 7.256, 4.467, 8.542, 2.461, 6.235,-0.453, 10.000,-0.762, 1.933, 10.000,-2.604, 3.901, 8.284,-5.620, 2.860, 1.831, 5.348,-10.000,-7.821, 2.593,-0.502};
//        Path calibrationFile = Paths.get(
//                "docs/20220223 tuna_calibration/pathfinder3/zapperAge_local/carrknight/2022-04-22_16.51.48_zapper_local/zapper_local.yaml"
//        );

        double[] solution =
              //  {-50.830, 22.467, 20.911,-64.333,-3.872,-71.828, 17.786, 635.530, 0.775, 33.997, 14.841,-2.065,-24.428, 1.164, 25.175, 38.979,-24.874, 13.277, 37.779,-1.461, 3.092, 4.066, 3.735, 34.309, 145.886,-13.260, 16.731};
                {-51.724, 22.532, 21.767,-63.344,-3.947,-72.007, 25.182, 695.676, 0.686, 33.917, 14.578, 0.479,-25.794, 0.229, 25.667, 38.437,-27.530, 13.663, 37.790,-1.460, 3.542, 4.050,-0.639, 35.812, 118.978,-15.255, 24.295};

        Path calibrationFile = Paths.get(
                "docs/20220223 tuna_calibration/pathfinder3/zapper_expired/zapper_local_expired.yaml"
        );


        TunaEvaluator evaluator = new TunaEvaluator(calibrationFile,solution);
        evaluator.setNumRuns(5);
        evaluator.run();

    }


}
