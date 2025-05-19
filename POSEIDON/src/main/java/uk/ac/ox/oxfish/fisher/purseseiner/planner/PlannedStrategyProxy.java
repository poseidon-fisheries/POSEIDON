/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSamplers;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValues;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * an issue with Fad related strategies is that ideally you are supposed to have strategies who don't have fisher
 * specific parameters until start(...) is called but because a lot of files are read when the factory is called you get
 * files with fisher specific parameters fed into strategies that do not know who their fisher is. <br> You can solve
 * this by having strategies that depend on some form of loader/builder design pattern, but that makes testing them
 * quite a roundabout factor and makes them impossible to use in the non-FAD scenarios. <br> Here what we do is simply
 * creating a proxy; this decorates a planned strategy but manages its life cycle so that everything is loaded from
 * files only when start is called
 */
@SuppressWarnings("unchecked")
public class PlannedStrategyProxy implements FishingStrategy, DestinationStrategy {

    private static final Logger logger = Logger.getLogger(PlannedStrategyProxy.class.getName());

    private static final double MIN_BIAS = 0.0001;
    private static final double MAX_BIAS = 0.9999;
    /**
     * object used to draw catches for DEL and NOA
     */
    private final CatchSamplers<? extends LocalBiology> catchSamplers;
    /**
     * probability of any of these actions taking place next in a plan
     */
    private final Function<? super Fisher, ? extends Map<Class<? extends PurseSeinerAction>, Double>>
        attractionWeightsPerFisher;
    /**
     * function that returns max travel time
     */
    private final ToDoubleFunction<? super Fisher> maxTravelTimeLoader;
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
     * a multiplier to the data-read action weight for own fad
     */
    private final double ownFadActionWeightBias;
    /**
     * a multiplier to the DPL actions weight (makes it more or less common than what the data may suggest) removed
     * deployment bias, added bias for del and ofs
     */
    private final double deploymentBias;
    /**
     * a multiplier to the NOA actions weight (makes it more or less common than what the data may suggest)
     */
    private final double nonAssociatedBias;
    /**
     * a multiplier to the DEL actions weight (makes it more or less common than what the data may suggest)
     */
    private final double dolphinBias;
    /**
     * a multiplier to the OFS actions weight (makes it more or less common than what the data may suggest)
     */
    private final double opportunisticBias;
    /**
     * $ a fad needs to have accumulated before we even try to target it when stealing in an area
     */
    private final double minimumValueOpportunisticFadSets;

    /**
     * The minimum monetary value one's own FAD need to have before setting on it.
     */
    private final double minimumValueOfSetOnOwnFad;

    /**
     * To probability of finding another vessel's FAD when you search for some.
     */
    private final double probabilityOfFindingOtherFads;

    /**
     * if you tried to steal and failed, how many hours does it take for you to fish this out
     */
    private final double hoursWastedOnFailedSearches;
    /**
     * how many hours does it take for a plan to go stale and need re-planning
     */
    private final double planningHorizonInHours;
    /**
     * the trip duration will be uniformly distributed between data max_trip_duration and max_trip_duration * this
     * parameter (which is expected to be less or = 1)
     */
    private final double minimumPercentageOfTripDurationAllowed;
    /**
     * when this is set to true, NOA sets can "steal" biomass from under the fad
     */
    private final boolean noaSetsCanPoachFads;
    /**
     * if this is above 0, NOA sets can fish out of the sea tile they actually happen in
     */
    private final int noaSetsRangeInSeaTiles;
    /**
     * if this is above 0, DEL sets can fish out
     */
    private final int delSetsRangeInSeaTiles;

    private final Map<Class<? extends PurseSeinerAction>, ? extends LocationValues> locationValues;

    private final AlgorithmFactory<? extends DiscretizedOwnFadPlanningModule> fadPlanningModule;
    private PlannedStrategy delegate;

    public PlannedStrategyProxy(
        final CatchSamplers<? extends LocalBiology> catchSamplers,
        final Function<? super Fisher, ? extends Map<Class<? extends PurseSeinerAction>, Double>> attractionWeightsPerFisher,
        final ToDoubleFunction<? super Fisher> maxTravelTimeLoader,
        final double additionalHourlyDelayDolphinSets,
        final double additionalHourlyDelayDeployment,
        final double additionalHourlyDelayNonAssociatedSets,
        final double ownFadActionWeightBias,
        final double deploymentBias,
        final double nonAssociatedBias,
        final double dolphinBias,
        final double opportunisticBias,
        final double minimumValueOpportunisticFadSets,
        final double minimumValueOfSetOnOwnFad,
        final double probabilityOfFindingOtherFads,
        final double hoursWastedOnFailedSearches,
        final double planningHorizonInHours,
        final double minimumPercentageOfTripDurationAllowed,
        final boolean noaSetsCanPoachFads,
        final int noaSetsRangeInSeaTiles,
        final int delSetsRangeInSeaTiles,
        final AlgorithmFactory<? extends DiscretizedOwnFadPlanningModule> fadPlanningModule,
        final Map<Class<? extends PurseSeinerAction>, ? extends LocationValues> locationValues
    ) {
        checkArgument(minimumPercentageOfTripDurationAllowed >= 0);
        checkArgument(minimumPercentageOfTripDurationAllowed <= 1);
        this.catchSamplers = catchSamplers;
        this.attractionWeightsPerFisher = attractionWeightsPerFisher;
        this.maxTravelTimeLoader = maxTravelTimeLoader;
        this.additionalHourlyDelayDolphinSets = additionalHourlyDelayDolphinSets;
        this.additionalHourlyDelayDeployment = additionalHourlyDelayDeployment;
        this.additionalHourlyDelayNonAssociatedSets = additionalHourlyDelayNonAssociatedSets;
        this.ownFadActionWeightBias = max(min(MAX_BIAS, ownFadActionWeightBias), MIN_BIAS);
        this.deploymentBias = max(min(MAX_BIAS, deploymentBias), MIN_BIAS);
        this.dolphinBias = max(min(MAX_BIAS, dolphinBias), MIN_BIAS);
        this.opportunisticBias = max(min(MAX_BIAS, opportunisticBias), MIN_BIAS);
        this.nonAssociatedBias = max(min(MAX_BIAS, nonAssociatedBias), MIN_BIAS);
        this.minimumValueOpportunisticFadSets = minimumValueOpportunisticFadSets;
        this.minimumValueOfSetOnOwnFad = minimumValueOfSetOnOwnFad;
        this.probabilityOfFindingOtherFads = probabilityOfFindingOtherFads;
        this.hoursWastedOnFailedSearches = hoursWastedOnFailedSearches;
        this.planningHorizonInHours = planningHorizonInHours;
        this.minimumPercentageOfTripDurationAllowed = minimumPercentageOfTripDurationAllowed;
        this.noaSetsCanPoachFads = noaSetsCanPoachFads;
        this.delSetsRangeInSeaTiles = delSetsRangeInSeaTiles;
        this.fadPlanningModule = fadPlanningModule;
        this.locationValues = locationValues;
        this.noaSetsRangeInSeaTiles = noaSetsRangeInSeaTiles;
    }

    public PlannedStrategy getDelegate() {
        return delegate;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
        checkState(delegate == null, "Already started!");
        final Map<ActionType, Double> plannableActionWeights = new HashMap<>();
        final HashMap<ActionType, PlanningModule> planModules = new HashMap<>();

        // Initializing these here locks us into abundance biology, which is not likely to be
        // a problem any time soon, but should eventually be made configurable
        final AbundanceCatchMaker catchMaker = new AbundanceCatchMaker(model.getBiology());
        final Class<? extends LocalBiology> localBiologyClass = AbundanceLocalBiology.class;

        // Start location value fields
        locationValues.values().forEach(lv -> lv.start(model, fisher));

        //(1) build planners
        final Map<Class<? extends PurseSeinerAction>, Double> fileAttractionWeights =
            attractionWeightsPerFisher.apply(fisher);
        for (final Map.Entry<Class<? extends PurseSeinerAction>, Double> actionWeight :
            fileAttractionWeights.entrySet()) {
            if (actionWeight.getValue() <= 0)
                continue;
            // big elif; this could have easily been cleaned up a bit with some
            // common factory method, but it works for now
            // DEL
            if (actionWeight.getKey().equals(DolphinSetAction.class)) {
                // add
                final LocationValues locations =
                    locationValues.get(DolphinSetAction.class);
                if (locations.getValues().isEmpty())
                    logger.warning(() ->
                        fisher + " failed to create DEL location values, in spite of having" +
                            "a weight of " + actionWeight.getValue()
                    );
                plannableActionWeights.put(
                    ActionType.DolphinSets,
                    actionWeight.getValue() * dolphinBias / (1 - dolphinBias)
                );
                planModules.put(
                    ActionType.DolphinSets,
                    new DolphinSetFromLocationValuePlanningModule(
                        fisher, locations,
                        model.getMap(),
                        model.getRandom(),
                        additionalHourlyDelayDolphinSets,
                        catchSamplers.get(DolphinSetAction.class),
                        catchMaker,
                        model.getBiology(),
                        localBiologyClass, delSetsRangeInSeaTiles
                    )
                );
            }
            // DPL
            else if (actionWeight.getKey().equals(FadDeploymentAction.class)) {

                final LocationValues locations =
                    locationValues.get(FadDeploymentAction.class);
                if (locations.getValues().isEmpty())
                    logger.warning(() ->
                        fisher + " failed to create DPL location values, in spite of having" +
                            "a weight of " + actionWeight.getValue()
                    );
                plannableActionWeights.put(
                    ActionType.DeploymentAction,
                    actionWeight.getValue() * deploymentBias / (1 - deploymentBias)
                );
                planModules.put(
                    ActionType.DeploymentAction,
                    new DeploymentFromLocationValuePlanningModule(
                        fisher, locations,
                        model.getMap(),
                        model.getRandom(),
                        additionalHourlyDelayDeployment
                    )
                );
            }
            // NOA
            else if (actionWeight.getKey().equals(NonAssociatedSetAction.class)) {

                final LocationValues locations =
                    locationValues.get(NonAssociatedSetAction.class);
                plannableActionWeights.put(
                    ActionType.NonAssociatedSets,
                    actionWeight.getValue() * nonAssociatedBias / (1 - nonAssociatedBias)
                );
                planModules.put(
                    ActionType.NonAssociatedSets,
                    new NonAssociatedSetFromLocationValuePlanningModule(
                        fisher, locations,
                        model.getMap(),
                        model.getRandom(),
                        additionalHourlyDelayNonAssociatedSets,
                        catchSamplers.get(NonAssociatedSetAction.class),
                        catchMaker,
                        model.getBiology(),
                        localBiologyClass,
                        noaSetsCanPoachFads,
                        noaSetsRangeInSeaTiles
                    )
                );
            }
            // FAD
            else if (actionWeight.getKey().equals(FadSetAction.class)) {
                plannableActionWeights.put(
                    ActionType.SetOwnFadAction,
                    actionWeight.getValue() * ownFadActionWeightBias / (1 - ownFadActionWeightBias)
                );
                planModules.put(
                    ActionType.SetOwnFadAction,
                    fadPlanningModule.apply(model)
                );
            }
            // OFS
            else if (actionWeight.getKey().equals(OpportunisticFadSetAction.class)) {
                // add
                final LocationValues locations =
                    locationValues.get(OpportunisticFadSetAction.class);
                if (locations.getValues().isEmpty()) {
                    logger.warning(() ->
                        fisher + " failed to create OFS location values, in spite of having" +
                            "a weight of " + actionWeight.getValue()
                    );
                    continue;
                }
                plannableActionWeights.put(
                    ActionType.OpportunisticFadSets,
                    actionWeight.getValue() * opportunisticBias / (1 - opportunisticBias)
                );
                planModules.put(
                    ActionType.OpportunisticFadSets,
                    new FadStealingFromLocationValuePlanningModule(
                        fisher, locations,
                        model.getMap(),
                        model.getRandom(),
                        2.779,
                        hoursWastedOnFailedSearches,
                        minimumValueOpportunisticFadSets,
                        probabilityOfFindingOtherFads
                    )
                );
            }

        }

        final DrawThenCheapestInsertionPlanner planner =
            new DrawThenCheapestInsertionPlanner(
                new UniformDoubleParameter(
                    minimumPercentageOfTripDurationAllowed * maxTravelTimeLoader.applyAsDouble(fisher),
                    maxTravelTimeLoader.applyAsDouble(fisher)
                ),
                plannableActionWeights,
                planModules
            );
        //(2) create the delegate
        delegate = new PlannedStrategy(planner, planningHorizonInHours, minimumValueOfSetOnOwnFad);

        delegate.start(model, fisher);
    }

    @Override
    public void turnOff(final Fisher fisher) {
        delegate.turnOff(fisher);
    }

    @Override
    public ActionResult act(
        final FishState model,
        final Fisher agent,
        final Regulation regulation,
        final double hoursLeft
    ) {
        return delegate.act(model, agent, regulation, hoursLeft);
    }

    @Override
    public SeaTile chooseDestination(
        final Fisher fisher,
        final MersenneTwisterFast random,
        final FishState model,
        final Action currentAction
    ) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
    }

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
