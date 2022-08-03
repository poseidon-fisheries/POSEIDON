package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.*;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * an issue with Fad related strategies is that ideally you are supposed to have strategies
 * who don't have fisher specific parameters until start(..) is called but because a lot of files
 * are read when the factory is called you get files with fisher specific parameters fed into
 * strategies that do not know who their fisher is. <br>
 * You can solve this by having strategies that depend on some form of loader/builder design
 * pattern, but that makes testing them quite a roundabout factor and makes them impossible to use
 * in the non-FAD scenarios. <br>
 * Here what we do is simply creating a proxy; this decorates a plannedstrategy but manages its
 * life cycle so that everything is loaded from files only when start is called
 */
public class PlannedStrategyProxy implements FishingStrategy, DestinationStrategy {

    private PlannedStrategy delegate;


    /**
     * object used to draw catches for DEL and NOA
     */
    private final Map<Class<? extends AbstractSetAction<?>>,
            ? extends CatchSampler<? extends LocalBiology>> catchSamplers;

    /**
     * probability of any of these actions taking place next in a plan
     */
    private final Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>>
            attractionWeightsPerFisher;

    /**
     * function that returns max travel time
     */
    private final ToDoubleFunction<Fisher> maxTravelTimeLoader;

    /**
     * hours wasted after each DEL set
     */
    private final double additionalHourlyDelayDolphinSets;
    /**
     * hours wasted after every DPL
     */
    private final double additionalHourlyDelayDeployment;
    /**
     * hours wasted after every NOA
     */
    private final double additionalHourlyDelayNonAssociatedSets;

    /**
     *  $ a fad needs to have accumulated before we even try to target it
     */
    private final double minimumValueFadSets;

    /**
     * a multiplier to the data-read action weight for own fad
     */
    private final double ownFadActionWeightBias;

    /**
     * a multiplier to the DPL actions weight (makes it more or less common than what the data may suggest)
     */
    private final double deploymentBias;

    /**
     * a multiplier to the NOA actions weight (makes it more or less common than what the data may suggest)
     */
    private final double nonAssociatedBias;

    /**
     *  $ a fad needs to have accumulated before we even try to target it when stealing in
     *  an area
     */
    private final double minimumValueOpportunisticFadSets;
    /**
     * higher this is, the more a fisher will prefer to target FADs closer to the centroid
     * of the path
     */
    private final double distancePenaltyFadSets;

    /**
     * discretizes map so that when it is time to target FADs you just
     * go through a few relevant ones
     */
    private final MapDiscretization mapDiscretizationFadSets;

    /**
     * if you tried to steal and failed, how many hours does it take for you to fish this out
     */
    private final double hoursWastedOnFailedSearches;


    /**
     * how many hours does it take for a plan to go stale and need replanning
     */
    private final double planningHorizonInHours;

    /**
     * the trip duration will be uniformly distributed between data max_trip_duration and max_trip_duration * this parameter (which is expected to be less or = 1)
     */
    private final double minimumPercentageOfTripDurationAllowed;

    /**
     * when this is set to true, NOA sets can "steal" biomass from under the fad
     */
    private final boolean noaSetsCanPoachFads;
    /**
     * when this is set to true you cannot put an action in the plan if it looks illegal now.
     * When this is not true, illegal actions stay in the plan until it's time to execute them. If they didn't become legal
     * then, they will trigger a replan
     */
    private final boolean doNotWaitToPurgeIllegalActions;

    /**
     * if this is is above 0, NOA sets can fish out of the seatile they actually happen in
     */
    private final int noaSetsRangeInSeatiles;

    /**
     * if this is is above 0, DEL sets can fish out
     */
    private final int delSetsRangeInSeatiles;

    private double[] impossibleFadAreaXBounds;
    private double[] impossibleFadAreaYBounds;


    public PlannedStrategyProxy(
            Map<Class<? extends AbstractSetAction<?>>,
                    ? extends CatchSampler<? extends LocalBiology>> catchSamplers,
            Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>> attractionWeightsPerFisher,
            ToDoubleFunction<Fisher> maxTravelTimeLoader,
            double additionalHourlyDelayDolphinSets,
            double additionalHourlyDelayDeployment,
            double additionalHourlyDelayNonAssociatedSets,
            double minimumValueFadSets,
            double ownFadActionWeightBias, double deploymentBias, double nonAssociatedBias, double minimumValueOpportunisticFadSets, double distancePenaltyFadSets,
            MapDiscretization mapDiscretizationFadSets,
            double hoursWastedOnFailedSearches,
            double planningHorizonInHours, double minimumPercentageOfTripDurationAllowed,
            boolean noaSetsCanPoachFads, boolean doNotWaitToPurgeIllegalActions,
            int noaSetsRangeInSeatiles, int delSetsRangeInSeatiles) {
        this.catchSamplers = catchSamplers;
        this.attractionWeightsPerFisher = attractionWeightsPerFisher;
        this.maxTravelTimeLoader = maxTravelTimeLoader;
        this.additionalHourlyDelayDolphinSets = additionalHourlyDelayDolphinSets;
        this.additionalHourlyDelayDeployment = additionalHourlyDelayDeployment;
        this.additionalHourlyDelayNonAssociatedSets = additionalHourlyDelayNonAssociatedSets;
        this.minimumValueFadSets = minimumValueFadSets;
        this.ownFadActionWeightBias = ownFadActionWeightBias;
        this.deploymentBias = deploymentBias;
        this.nonAssociatedBias = nonAssociatedBias;
        this.minimumValueOpportunisticFadSets = minimumValueOpportunisticFadSets;
        this.distancePenaltyFadSets = distancePenaltyFadSets;
        this.mapDiscretizationFadSets = mapDiscretizationFadSets;
        this.hoursWastedOnFailedSearches = hoursWastedOnFailedSearches;
        this.planningHorizonInHours = planningHorizonInHours;
        this.minimumPercentageOfTripDurationAllowed = minimumPercentageOfTripDurationAllowed;
        this.noaSetsCanPoachFads=noaSetsCanPoachFads;
        this.doNotWaitToPurgeIllegalActions = doNotWaitToPurgeIllegalActions;
        this.delSetsRangeInSeatiles = delSetsRangeInSeatiles;
        Preconditions.checkArgument(minimumPercentageOfTripDurationAllowed>=0);
        Preconditions.checkArgument(minimumPercentageOfTripDurationAllowed<=1);
        this.noaSetsRangeInSeatiles = noaSetsRangeInSeatiles;
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        Preconditions.checkState(delegate==null, "Already started!");
        Map<ActionType, Double> plannableActionWeights = new HashMap<>();
        HashMap<ActionType, PlanningModule> planModules = new HashMap<>();

        //grab location values
        PurseSeineGear<? extends LocalBiology,
                ? extends AbstractFad<? extends LocalBiology,? extends AbstractFad<?,?>>> gear =
                ((PurseSeineGear<? extends LocalBiology,
                        ? extends AbstractFad<? extends LocalBiology,? extends AbstractFad<?,?>>>) fisher.getGear());
        HashMap<Class<? extends LocationValues>,LocationValues> locationValues = new HashMap<>();
        for (AttractionField attractionField : gear.getAttractionFields()) {
            attractionField.start(model,fisher);
            locationValues.put(attractionField.getLocationValues().getClass(),
                    attractionField.getLocationValues());

        }




        //(1) build planners
        Map<Class<? extends PurseSeinerAction>, Double> fileAttractionWeights =
                attractionWeightsPerFisher.apply(fisher);
        for (Map.Entry<Class<? extends PurseSeinerAction>, Double> actionWeight : fileAttractionWeights.entrySet()) {
            if (actionWeight.getValue()<=0)
                continue;
            //big elif; this could have easily been cleaned up a bit with some
            //common factory method, but it works for now
            //DEL
            if(actionWeight.getKey().equals(DolphinSetAction.class)){
                //add
                DolphinSetLocationValues locations =
                        (DolphinSetLocationValues) locationValues.get(DolphinSetLocationValues.class);
                if(locations.getValues().size()==0)
                    System.out.println(fisher + " failed to create DEL location values, in spite of having" +
                                               "a weight of " + actionWeight.getValue());
                plannableActionWeights.put(ActionType.DolphinSets,
                        actionWeight.getValue());
                planModules.put(ActionType.DolphinSets,
                        new DolphinSetFromLocationValuePlanningModule(
                                locations,
                                model.getMap(),
                                model.getRandom(),
                                additionalHourlyDelayDolphinSets,
                                catchSamplers.get(DolphinSetAction.class),
                                model.getBiology(),
                                delSetsRangeInSeatiles)
                );
            }
            //DPL
            else if(actionWeight.getKey().equals(FadDeploymentAction.class))
            {

                DeploymentLocationValues locations =
                        (DeploymentLocationValues) locationValues.get(DeploymentLocationValues.class);
                if(locations.getValues().size()==0)
                    System.out.println(fisher + " failed to create DPL location values, in spite of having" +
                                               "a weight of " + actionWeight.getValue());
                plannableActionWeights.put(ActionType.DeploymentAction,
                        actionWeight.getValue()*deploymentBias);
                planModules.put(ActionType.DeploymentAction,
                        new DeploymentFromLocationValuePlanningModule(
                                locations,
                                model.getMap(),
                                model.getRandom(),
                                additionalHourlyDelayDeployment
                        )
                );
            }
            //NOA
            else if(actionWeight.getKey().equals(NonAssociatedSetAction.class))
            {

                NonAssociatedSetLocationValues locations =
                        (NonAssociatedSetLocationValues) locationValues.get(NonAssociatedSetLocationValues.class);
                plannableActionWeights.put(ActionType.NonAssociatedSets,
                        actionWeight.getValue() * nonAssociatedBias);
                planModules.put(ActionType.NonAssociatedSets,
                        new NonAssociatedSetFromLocationValuePlanningModule(
                                locations,
                                model.getMap(),
                                model.getRandom(),
                                additionalHourlyDelayNonAssociatedSets,
                                catchSamplers.get(NonAssociatedSetAction.class),
                                model.getBiology(),
                                noaSetsCanPoachFads,
                                noaSetsRangeInSeatiles
                        )
                );
            }
            //FAD
            else if(actionWeight.getKey().equals(FadSetAction.class))
            {
                mapDiscretizationFadSets.discretize(model.getMap());
                plannableActionWeights.put(ActionType.SetOwnFadAction,
                        actionWeight.getValue()*ownFadActionWeightBias);
                DiscretizedOwnFadCentroidPlanningModule module = new DiscretizedOwnFadCentroidPlanningModule(
                        mapDiscretizationFadSets,
                        minimumValueFadSets,
                        distancePenaltyFadSets
                );
                if(impossibleFadAreaXBounds != null){
                    assert impossibleFadAreaYBounds !=null;
                    module.setBannedGridBounds(impossibleFadAreaYBounds,impossibleFadAreaXBounds);
                }
                planModules.put(ActionType.SetOwnFadAction,
                        module
                );
            }
            //OFS
            else if(actionWeight.getKey().equals(OpportunisticFadSetAction.class)){
                //add
                OpportunisticFadSetLocationValues locations =
                        (OpportunisticFadSetLocationValues) locationValues.get(OpportunisticFadSetLocationValues.class);
                if(locations.getValues().size()==0) {
                    System.out.println(fisher + " failed to create OFS location values, in spite of having" +
                            "a weight of " + actionWeight.getValue());
                    continue;
                }
                plannableActionWeights.put(ActionType.OpportunisticFadSets,
                        actionWeight.getValue());
                planModules.put(ActionType.OpportunisticFadSets,
                        new FadStealingFromLocationValuePlanningModule(
                                locations,
                                model.getMap(),
                                model.getRandom(),
                                2.779,
                                hoursWastedOnFailedSearches,
                                minimumValueOpportunisticFadSets
                        )
                );
            }
            else{}

        }

        DrawThenCheapestInsertionPlanner planner =
                new DrawThenCheapestInsertionPlanner(
                        new UniformDoubleParameter(
                                minimumPercentageOfTripDurationAllowed * maxTravelTimeLoader.applyAsDouble(fisher),
                                maxTravelTimeLoader.applyAsDouble(fisher)
                        ),
                        plannableActionWeights,
                        planModules,
                        doNotWaitToPurgeIllegalActions);
        //(2) create the delegate
        delegate = new PlannedStrategy(
                planner,planningHorizonInHours);

        delegate.start(model, fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
    }

    @Override
    public ActionResult act(FishState model, Fisher agent, Regulation regulation, double hoursLeft) {
        return delegate.act(model, agent, regulation, hoursLeft);
    }

    @Override
    public SeaTile chooseDestination(Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
    }

    @Override
    public boolean shouldFish(Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip) {
        return delegate.shouldFish(fisher, random, model, currentTrip);
    }


    public double[] getImpossibleFadAreaXBounds() {
        return impossibleFadAreaXBounds;
    }

    public void setImpossibleFadAreaXBounds(double[] impossibleFadAreaXBounds) {
        this.impossibleFadAreaXBounds = impossibleFadAreaXBounds;
    }

    public double[] getImpossibleFadAreaYBounds() {
        return impossibleFadAreaYBounds;
    }

    public void setImpossibleFadAreaYBounds(double[] impossibleFadAreaYBounds) {
        this.impossibleFadAreaYBounds = impossibleFadAreaYBounds;
    }
}
