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
     * when this is set to true you cannot put an action in the plan if it looks illegal now.
     * When this is not true, illegal actions stay in the plan until it's time to execute them. If they didn't become legal
     * then, they will trigger a replan
     */
    private final boolean doNotWaitToPurgeIllegalActions;
    private Plan currentPlan;
    /**
     * deployment location values is needed if the planner needs to do DPL actions; otherwise don't bother
     */
    private DeploymentLocationValues deploymentLocationValues;
    private Fisher fisher;
    private FishState model;
    private double thisTripTargetHours = 0;

    public DrawThenCheapestInsertionPlanner(
        final DoubleParameter maxHoursPerTripGenerator,
        final Map<ActionType, Double> plannableActionWeights,
        final Map<ActionType, PlanningModule> planModules, final boolean doNotWaitToPurgeIllegalActions
    ) {
        this.maxHoursPerTripGenerator = maxHoursPerTripGenerator;
        this.plannableActionWeights = plannableActionWeights;
        this.planModules = planModules;
        this.doNotWaitToPurgeIllegalActions = doNotWaitToPurgeIllegalActions;
    }

    /**
     * adds the path to the current plan and returns the hours it takes (duration + movement); returns NaN if we go overbudget!
     * As a <b>side effect</b> it will <b>add the action to the plan</b> if it is within budget! <br>
     * <p>
     * No real reason to keep it static except that it is easier to test that way.
     *
     * @param currentPlan                      the plan as it is now
     * @param actionToAddToPath                the planned action to add to the plan
     * @param hoursAvailable                   how many hours we still have available for this trip
     * @param speed                            the speed of the boat (distance/time)
     * @param map                              nautical map for distance calculation
     * @param insertActionInPlanIfHoursAllowIt if true, the action is added to the plan with the cheapest possible insert. when this is set to false this method only computes the hypothetical hourly costs but does not insert the action in the given plan
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

        //go through all options
        double bestInsertionCost = Double.MAX_VALUE;
        int bestIndex = -1;
        //you don't want to insert it in the beginning and you don't want to insert it at the end
        final ListIterator<PlannedAction> iterator = currentPlan.lookAtPlan().listIterator(1);
        //for each possible insertion point, adding point C between A and B has insertion cost equal to d(A,C)+d(C,B)-d(A,B);
        //this distance ought to be positive due to triangle inequality
        for (int index = 1; index < currentPlan.numberOfStepsInPath(); index++) {


            final PlannedAction from = iterator.previous();
            iterator.next(); //this bring you back to start
            final PlannedAction to = iterator.next();

            //it is never optimal to squeeze yourself between two actions that take place in the same spot unless you are also in that spot
            if (currentPlan.numberOfStepsInPath() > 2 && from.getLocation() == to.getLocation() && from.getLocation() != actionToAddToPath.getLocation())
                continue;

            final double firstSegment = map.distance(from.getLocation(), actionToAddToPath.getLocation());
            if (firstSegment == 0) {
                //if you are trying to insert in the same cell, this is surely the cheapest insert!
                bestInsertionCost = 0;
                bestIndex = index;
                break;
            }
            final double secondSegment = map.distance(actionToAddToPath.getLocation(), to.getLocation());
            final double replacedSegment = map.distance(from.getLocation(), to.getLocation());

            final double insertionCost = (firstSegment + secondSegment - replacedSegment) / speed; //turn insertion cost into time
            //tricky here because when you add the approximations from the curvature distance, if you are going on a straight line
            //in the projected map, you may actually be violating by tiny amounts the triangle inequality constraint
            //but we choose to ignore this
            //assert insertionCost >= -5 : "triangle inequality does not seem to hold here. Bizarre " + insertionCost;

            if (insertionCost < bestInsertionCost) {
                bestInsertionCost = insertionCost;
                bestIndex = index;
            }

        }
        final double totalCostInHours = bestInsertionCost + actionToAddToPath.hoursItTake();
        if (totalCostInHours <= hoursAvailable) {
            assert bestIndex > 0;
            if (insertActionInPlanIfHoursAllowIt)
                currentPlan.insertAction(actionToAddToPath, bestIndex, totalCostInHours);
            return totalCostInHours;
        } else {
            return Double.NaN;
        }

    }

    ////
    public static void main(final String[] args) throws IOException {
////

//        double[] solution =
//                {
//                        -3.917,-13.419,-11.295, 7.326,-6.007, 18.535,-8.852, 1.824, 3.505, 4.512, 4.229,-3.035,-2.948, 42.966, 4.175, 3.672,-4.506, 6.922,-11.295,-21.336,-36.316,-21.747                               };
//        Path calibrationFile = Paths.get(
//                "/home/carrknight/Dropbox/oxfish_docs/20220223 tuna_calibration/clorophill/neweez/manual1_fixed_tripleweight/carrknight/2022-10-12_22.41.44_manual1/manual.yaml"
//        );

        final double[] solution =
            {
                2.161, -29.924, -48.141, -6.032, 0.175, 4.096, -2.428, -15.746, 7.684, 9.575, 5.658, 14.134, -0.885, -4.833, 5.226, 10.053, 9.452, -2.723, 12.029, -0.410, 13.310, -7.526, 5.416
            };
        final Path calibrationFile = Paths.get(
            "/home/carrknight/code/oxfish/docs/20220223 tuna_calibration/clorophill/environmental/skjincluded/weibull_value/more/carrknight/2022-10-27_11.26.13_manual1/more.yaml"
        );


        final TunaEvaluator evaluator = new TunaEvaluator(calibrationFile, solution);
        evaluator.setNumRuns(1);
        evaluator.run();
////
////    }
//    }
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        this.fisher = fisher;
        this.model = model;
    }

    /**
     * checks that the module has started, and that the "stillAllowedActionsInPlan" is filled correctly, etc...
     *
     * @param action
     */
    private void readyPlanningModule(final ActionType action, final Fisher fisher, final FishState model) {

        assert plannableActionWeights.get(action) > 0;

        //has it been initialized?
        if (!stillAllowedActionsInPlan.containsKey(action)) {

            //find the module
            final PlanningModule planningModule = planModules.get(action);
            Preconditions.checkArgument(
                planningModule != null,
                "You have assigned weight to " + action.toString() + " without any module associated to it"
            );
            //start the planning module
            planningModule.start(model, fisher);
            //set maximum actions
            stillAllowedActionsInPlan.put(action, new MutableInt(planningModule.maximumActionsInAPlan(model, fisher)));
            //done!
        }


    }

    /**
     * returns true as long as at least one of the planning modules has not been started or there are still allowedActions for that type
     *
     * @return
     */
    private boolean isAnyActionEvenPossible() {
        return plannableActionWeights
            .entrySet()
            .stream()
            // can this action type be drawn?
            .filter(entry -> entry.getValue() > 0)
            // if it is drawn is it allowed? (it is also possible that this has never been started, so let's assume it is valid)
            .map(entry -> stillAllowedActionsInPlan.get(entry.getKey()))
            .anyMatch(allowedActions -> allowedActions == null || allowedActions.intValue() > 0);
    }

    @Override
    public void turnOff(final Fisher fisher) {
        this.fisher = null;
        model = null;
    }

    public DeploymentLocationValues getDeploymentLocationValues() {
        return deploymentLocationValues;
    }

    public void setDeploymentLocationValues(
        final DeploymentLocationValues deploymentLocationValues
    ) {
        this.deploymentLocationValues = deploymentLocationValues;
    }

    private Plan planRecursively(
        final Plan currentPlan, double hoursLeftInBudget,
        final FishState model, final Fisher fisher
    ) {

        //if there are no possible actions, stop
        if (!isAnyActionEvenPossible())
            return currentPlan;
        //if there are some possible actions, do them

        //pick at random next action!

        //prepare pair list
        final ActionType nextActionType = drawNextAction(model.getRandom());

        //you can't draw any actions, the plan is over!
        if (nextActionType == null)
            return currentPlan;

        //ask the planning module for an action to add to the path
        final PlanningModule planningModule = planModules.get(nextActionType);
        //might be the first time you call it, so get it ready
        readyPlanningModule(nextActionType, fisher, model);
        //it is possible that even though it's the first time we try this action, we actually can't do
        //if so star over
        if (stillAllowedActionsInPlan.get(nextActionType).intValue() <= 0)
            return planRecursively(currentPlan, hoursLeftInBudget, model, fisher);

        final PlannedAction plannedAction = planningModule.chooseNextAction(currentPlan);

        //if the planning module cannot propose more actions, ignore them for this plan
        if (plannedAction == null || (doNotWaitToPurgeIllegalActions && !plannedAction.isAllowedNow(fisher))) {
            // If there is an attempt to add an illegal action to the plan, we don't allow any more actions of
            // that type to be added. We're leaving that bit of code in for now, but we're questioning whether
            // it's necessary. It caused problem when attempts to add FAD sets in, e.g., EEZs; preventing further
            // FAD sets to be added. We fixed that by filtering out illegal FAD sets "upstream" (in the
            // OwnFadSetDiscretizedActionGenerator) but we should be careful with this -- NP 2023-11-14.
            stillAllowedActionsInPlan.get(nextActionType).setValue(0);
            //try planning more
            return planRecursively(currentPlan, hoursLeftInBudget, model, fisher);
        } else {
            //there is an action and we need to take it
            final double hoursConsumed =
                cheapestInsert(currentPlan, plannedAction, hoursLeftInBudget, fisher.getBoat().getSpeedInKph(),
                    model.getMap(), true
                );
            if (Double.isNaN(hoursConsumed))
                //went overbudget! our plan is complete
                return currentPlan;
            else {
                stillAllowedActionsInPlan.get(nextActionType).decrement();
                hoursLeftInBudget = hoursLeftInBudget - hoursConsumed;
                assert hoursLeftInBudget >= -FishStateUtilities.EPSILON;
                if (hoursLeftInBudget <= 0)
                    return currentPlan;
                else
                    return planRecursively(currentPlan, hoursLeftInBudget,
                        model, fisher
                    );
            }
        }


    }

    private ActionType drawNextAction(final MersenneTwisterFast random) {
        final List<Pair<ActionType, Double>> toDraw = new ArrayList<>(plannableActionWeights.size());
        for (final Map.Entry<ActionType, Double> actionsAvailable : plannableActionWeights.entrySet()) {
            final boolean allowed = stillAllowedActionsInPlan.getOrDefault(
                actionsAvailable.getKey(),
                new MutableInt(1)
            ).intValue() > 0;
            if (!allowed)
                continue;
            toDraw.add(new Pair<>(actionsAvailable.getKey(), actionsAvailable.getValue()));
        }
        //feed it to the enumerated distribution
        ActionType nextAction = null;
        if (toDraw.size() == 1)
            nextAction = toDraw.get(0).getKey();
        else {
            nextAction = new EnumeratedDistribution<ActionType>(new MTFApache(random), toDraw).sample();
        }
        return nextAction;
    }

    public Plan planNewTrip() {
        assert fisher.isAtPort();
        assert fisher.isAllowedAtSea();
        assert fisher.getLocation() == fisher.getHomePort().getLocation();

        //create an empty plan (circling back home)
        currentPlan = new Plan(
            fisher.getLocation(),
            fisher.getLocation()
        );
        //start planning
        stillAllowedActionsInPlan.clear();
        thisTripTargetHours = maxHoursPerTripGenerator.applyAsDouble(model.getRandom());
        currentPlan = planRecursively(currentPlan, thisTripTargetHours,
            model, fisher
        );

        return currentPlan;
    }

    public Plan replan(final double hoursAlreadySpent) {
        Preconditions.checkArgument(fisher.getLocation() != fisher.getHomePort()
            .getLocation() || fisher.getHoursAtSea() == 0);

        //length of the trip is reduced by how much we have already spent outside
        double hoursAvailable = getThisTripTargetHours();
        hoursAvailable = hoursAvailable - hoursAlreadySpent;

        //the new plan will remove all previous actions except for DPLs (if any)
        //which are constraints we don't want to move
        final SeaTile lastPlanLocation = fisher.getLocation();
        final NauticalMap map = model.getMap();
        final double speed = fisher.getBoat().getSpeedInKph();
        final Plan newPlan = new Plan(
            fisher.getLocation(),
            fisher.getHomePort().getLocation()
        );
        //now take into consideration the very last step (return to port)
        final double lastStepCost = map.distance(lastPlanLocation, newPlan.peekLastAction().getLocation()) / speed;
        hoursAvailable -= lastStepCost;
        newPlan.addHoursEstimatedItWillTake(lastStepCost);

        if (hoursAvailable >= 0) {
            for (final PlannedAction plannedAction : currentPlan.lookAtPlan()) {
                if (plannedAction instanceof PlannedAction.Deploy) {
                    final double hoursConsumed = cheapestInsert(
                        newPlan,
                        plannedAction,
                        hoursAvailable,
                        speed,
                        map,
                        true
                    );
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
        for (final Map.Entry<ActionType, MutableInt> allowedActions : stillAllowedActionsInPlan.entrySet()) {
            final PlanningModule planningModule = planModules.get(allowedActions.getKey());
            if (planningModule != null)
                allowedActions.getValue().setValue(
                    planningModule.maximumActionsInAPlan(model, fisher)
                );
        }
        //do not allow more DPL
        stillAllowedActionsInPlan.put(ActionType.DeploymentAction, new MutableInt(0));

        //random delays (chasing FADs off course, for example), it can happen to be completely off
        //assert hoursAvailable>=-FishStateUtilities.EPSILON : hoursAvailable;

        //add more events now.
        currentPlan = newPlan;
        if (hoursAvailable > 0) {
            for (final PlanningModule module : planModules.values()) {
                module.prepareForReplanning(model, fisher);
            }
            currentPlan = planRecursively(currentPlan, hoursAvailable,
                model, fisher
            );
        }
        return currentPlan;

    }

    public double getThisTripTargetHours() {
        return thisTripTargetHours;
    }

//    public static void main(String[] args) throws IOException {
////
////
//////        GenericOptimization.buildLocalCalibrationProblem(
//////                Paths.get(
//////                        "docs/20220223 tuna_calibration/pathfinder_julydata/carrknight/2022-07-13_12.05.49_catchability_shorttrips_yearlyreset/local/original.yaml"),
//////                new double[]{
//////                        -7.282,-1.293,-15.000, 10.947,-4.055,-6.807, 2.555,-5.752, 10.731,-15.000, 12.893,-2.702,-6.366, 7.338,-5.381, 14.320,-3.892,-0.708, 9.456, 13.734,-9.291,-0.551,-12.106,-9.638, 6.994                },
//////                "local.yaml", .2
//////        );
////
//////        GenericOptimization.buildLocalCalibrationProblem(
//////                Paths.get(
//////                        "docs/20220223 tuna_calibration/pathfinder_julydata/cenv0477/2022-07-13_14.22.39_yearlyreset2/local/original.yaml"),
//////                new double[]{
//////                        14.995,-15.000, 15.000,-14.468,-8.013, 12.966,-6.046, 6.462,-15.000, 9.943,-15.000,-11.069,-4.121, 15.000,-8.018,-11.889, 1.401, 1.426, 15.000,-8.334,-9.198, 1.559,-12.341, 4.376 },
//////                "local.yaml", .2
//////        );
//////
//        GenericOptimization.buildLocalCalibrationProblem(
//                Paths.get(
//                        "/home/carrknight/code/oxfish/docs/20220223 tuna_calibration/clorophill/environmental/skjincluded/weibull_value/more/original_calibration.yaml"),
//                new double[]{
//                        1.6296715072412908,-86.7293032265776,-29.71955183248816,-23.301130791889246,147.75630814989503,-2.1424338866954464,-25.718407690183653,18.800744639090695,10.570074492531134,-27.154560249134107,-118.11745092021698,17.77260734197482,9.965487262463206,-74.17039002034682,16.302644722665246,-54.543593904725434,-7.646861465532954,-8.648538634304845,2.6422097519329606,14.154899325110927,-2.6680704569728784,2.323597481426466,53.33190429315141,-14.358622067490462,20.7872931058517,20.620703701194767
//                },
//                "more.yaml", .05
//        );
//}

    public void setThisTripTargetHours(final double thisTripTargetHours) {
        this.thisTripTargetHours = thisTripTargetHours;
    }

}
