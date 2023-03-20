/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import com.google.common.collect.*;
import ec.util.MersenneTwisterFast;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectArrayMessage;
import org.jetbrains.annotations.NotNull;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategy;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.ActionAttractionField;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.Double.min;
import static java.lang.Math.exp;
import static java.util.Comparator.comparingDouble;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear.getPurseSeineGear;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class PurseSeinerFishingStrategy<B extends LocalBiology>
    implements FishingStrategy {

    private final double movingThreshold;
    private final Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>>
        actionWeightsLoader;
    private final Function<Fisher, SetOpportunityDetector<B>> setOpportunityDetectorProvider;
    private final Map<Class<? extends PurseSeinerAction>, DoubleUnaryOperator> actionValueFunctions;
    private final ToDoubleFunction<Class<? extends PurseSeinerAction>> maxCurrentSpeeds;
    private final Multiset<Class<? extends PurseSeinerAction>> actionCounts = HashMultiset.create();
    private final double searchActionDecayConstant;
    private final double fadDeploymentActionDecayConstant;
    private ImmutableMap<? extends Class<? extends PurseSeinerAction>, ActionAttractionField>
        attractionFields;
    private SetOpportunityDetector<B> setOpportunityDetector;
    private Map<Class<? extends PurseSeinerAction>, Double> actionWeights;
    private List<WeightedAction<?>> potentialActions = ImmutableList.of();

    PurseSeinerFishingStrategy(
        final Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>> actionWeightsLoader,
        final Function<Fisher, SetOpportunityDetector<B>> setOpportunityDetectorProvider,
        final Map<Class<? extends PurseSeinerAction>, DoubleUnaryOperator> actionValueFunctions,
        final ToDoubleFunction<Class<? extends PurseSeinerAction>> maxCurrentSpeeds,
        final double searchActionDecayConstant,
        final double fadDeploymentActionDecayConstant,
        final double movingThreshold
    ) {
        this.actionWeightsLoader = actionWeightsLoader;
        this.setOpportunityDetectorProvider = setOpportunityDetectorProvider;
        this.actionValueFunctions = ImmutableMap.copyOf(actionValueFunctions);
        this.maxCurrentSpeeds = maxCurrentSpeeds;
        this.searchActionDecayConstant = searchActionDecayConstant;
        this.fadDeploymentActionDecayConstant = fadDeploymentActionDecayConstant;
        this.movingThreshold = movingThreshold;
    }

    private static <T> Map<T, Double> normalizeWeights(final Map<T, Double> weightMap) {
        final double sumOfWeights =
            weightMap.values().stream().mapToDouble(Double::doubleValue).sum();
        return weightMap.entrySet().stream()
            .collect(toImmutableMap(Entry::getKey, entry -> entry.getValue() / sumOfWeights));
    }

    private static Double2D getCurrentVector(final Fisher fisher) {
        final Int2D gridLocation = fisher.getLocation().getGridLocation();
        final int step = fisher.grabState().getStep();
        return getFadManager(fisher)
            .getFadMap()
            .getDriftingObjectsMap()
            .getCurrentVectors()
            .getVector(step, gridLocation);
    }

    private static boolean isSafe(
        final PurseSeinerAction actionObject,
        final Collection<Class<? extends PurseSeinerAction>> safeActionClasses
    ) {
        return isSafe(actionObject.getClass(), safeActionClasses);
    }

    private static boolean isSafe(
        final Class<? extends PurseSeinerAction> actionClass,
        final Collection<Class<? extends PurseSeinerAction>> safeActionClasses
    ) {
        return safeActionClasses
            .stream()
            .anyMatch(safeActionClass -> safeActionClass.isAssignableFrom(actionClass));
    }

    private static double valueOfLocationBasedAction(
        final int previousActionsHere,
        final double locationValue,
        final DoubleUnaryOperator valueFunction,
        final double decayConstant
    ) {
        final double value = valueFunction.applyAsDouble(locationValue);
        final double decay = exp(-decayConstant * previousActionsHere);
        return value * decay;
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        actionWeights = normalizeWeights(actionWeightsLoader.apply(fisher));
        setOpportunityDetector = setOpportunityDetectorProvider.apply(fisher);
        attractionFields = Stream
            .of(fisher.getDestinationStrategy())
            .filter(destinationStrategy ->
                destinationStrategy instanceof GravityDestinationStrategy
            )
            .flatMap(destinationStrategy ->
                ((GravityDestinationStrategy) destinationStrategy).getAttractionFields().stream()
            )
            .filter(field -> field instanceof ActionAttractionField)
            .map(field -> (ActionAttractionField) field)
            .collect(toImmutableMap(
                ActionAttractionField::getActionClass,
                identity()
            ));
    }

    @Override
    public boolean shouldFish(
        final Fisher fisher,
        final MersenneTwisterFast random,
        final FishState fishState,
        final TripRecord currentTrip
    ) {
        if (potentialActions.isEmpty()) {
            potentialActions = findPotentialActions(fisher, fishState.getSpecies());
        }
        if (potentialActions.isEmpty()) {
            actionCounts.clear();
        }
        return !potentialActions.isEmpty();
    }

    private List<WeightedAction<?>> findPotentialActions(
        final Fisher fisher,
        final Collection<Species> species
    ) {

        if (fisher.getLocation().isLand()) {
            return ImmutableList.of();
        }

        final Int2D gridLocation = fisher.getLocation().getGridLocation();

        final ImmutableSet<Class<? extends PurseSeinerAction>> safeActionClasses =
            safeActionClasses(getCurrentVector(fisher));

        final List<AbstractSetAction<B>> possibleSetActions =
            setOpportunityDetector.possibleSetActions()
                .filter(actionObject -> isSafe(actionObject, safeActionClasses))
                .collect(toImmutableList());

        final Stream<WeightedAction<AbstractSetAction<B>>> weightedSetActions =
            possibleSetActions.stream().map(action -> WeightedAction.from(
                action,
                valueOfSetAction(action, species),
                actionValueFunctions.get(action.getClass()),
                actionWeights
            ));

        // Generate a search action for each of the set classes with no opportunities,
        // and give them a weight equivalent to the class they replace
        final ImmutableSet<? extends Class<?>> possibleSetActionClasses =
            possibleSetActions.stream().map(Object::getClass).collect(toImmutableSet());
        final Stream<WeightedAction<SearchAction>> weightedSearchActions = Stream
            .of(
                OpportunisticFadSetAction.class,
                NonAssociatedSetAction.class,
                DolphinSetAction.class
            )
            .filter(actionClass ->
                !possibleSetActionClasses.contains(actionClass) &&
                    isSafe(actionClass, safeActionClasses)
            )
            .map(actionClass -> WeightedAction.from(
                new SearchAction(fisher, setOpportunityDetector, actionClass),
                attractionFields.get(actionClass).getActionValueAt(gridLocation),
                v -> valueOfLocationBasedAction(
                    actionCounts.count(SearchAction.class),
                    v,
                    actionValueFunctions.get(SearchAction.class),
                    searchActionDecayConstant
                ),
                actionWeights
            ));

        final Stream<WeightedAction<FadDeploymentAction>> weightedFadDeploymentAction =
            getFadManager(fisher).getNumFadsInStock() < 1
                ? Stream.empty()
                : Stream.of(WeightedAction.from(
                new FadDeploymentAction(fisher),
                attractionFields.get(FadDeploymentAction.class).getActionValueAt(gridLocation),
                v -> valueOfLocationBasedAction(
                    actionCounts.count(FadDeploymentAction.class),
                    v,
                    actionValueFunctions.get(FadDeploymentAction.class),
                    fadDeploymentActionDecayConstant
                ),
                actionWeights
            ));

        final ImmutableList<WeightedAction<?>> actionsAvailable = Streams
            .concat(
                weightedSetActions,
                weightedSearchActions,
                weightedFadDeploymentAction
            ).collect(toImmutableList());

        final Logger logger = LogManager.getLogger("potential_actions");

        return actionsAvailable.stream()
            .filter(weightedAction -> weightedAction.getAction().isPermitted())
            .peek(weightedAction -> logger.debug(() -> new ObjectArrayMessage(
                weightedAction.getAction().getClass().getSimpleName(),
                weightedAction.getInitialValue(),
                weightedAction.getModulatedValue(),
                weightedAction.getWeightedValue()
            )))
            .filter(weightedAction -> weightedAction.getWeightedValue() > movingThreshold)
            .collect(toImmutableList());
    }

    private ImmutableSet<Class<? extends PurseSeinerAction>> safeActionClasses(
        final Double2D currentVector
    ) {
        final double currentSpeed = currentVector.length();
        return ActionClass.classMap.keySet()
            .stream()
            .filter(entry -> currentSpeed <= maxCurrentSpeeds.applyAsDouble(entry))
            .collect(toImmutableSet());
    }

    double valueOfSetAction(
        @NotNull final AbstractSetAction<? extends LocalBiology> action,
        final Collection<Species> species
    ) {
        final double totalBiomass = action.getTargetBiology().getTotalBiomass(species);
        assert totalBiomass >= 0;
        if (totalBiomass == 0) {
            return 0; // avoids div by 0 when calculating catchableProportion
        } else {
            final Fisher fisher = action.getFisher();
            final Hold hold = fisher.getHold();
            final double capacity = hold.getMaximumLoad() - hold.getTotalWeightOfCatchInHold();
            final double catchableProportion = min(1, capacity / totalBiomass);
            final double[] biomass = species.stream()
                .mapToDouble(s -> action.getTargetBiology().getBiomass(s))
                .toArray();
            final double[] potentialCatch = Arrays.copyOf(biomass, biomass.length);
            for (int i = 0; i < potentialCatch.length; i++) {
                potentialCatch[i] *= catchableProportion;
            }
            final double[] prices = fisher.getHomePort().getMarketMap(fisher).getPrices();
            return getFadManager(fisher).getFishValueCalculator().valueOf(potentialCatch, prices);
        }
    }

    @Override
    public ActionResult act(
        final FishState fishState,
        final Fisher fisher,
        final Regulation regulation,
        final double hoursLeft
    ) {
        // record our visit to that tile
        getPurseSeineGear(fisher).recordVisit(
            fisher.getLocation().getGridLocation(),
            fishState.getStep()
        );

        // Pick the potential action with the highest value or
        // get moving if there aren't any possible actions.
        final Optional<PurseSeinerAction> chosenAction =
            potentialActions.stream()
                .filter(entry -> entry.getAction().getDuration() <= hoursLeft)
                .max(comparingDouble(WeightedAction::getWeightedValue))
                .map(WeightedAction::getAction);
        potentialActions = ImmutableList.of();
        return chosenAction
            .map(action -> {
                actionCounts.add(action.getClass());
                return action.act(fishState, fisher, regulation, hoursLeft);
            })
            .orElse(new ActionResult(new Arriving(), 0)); // wait until tomorrow
    }

}
