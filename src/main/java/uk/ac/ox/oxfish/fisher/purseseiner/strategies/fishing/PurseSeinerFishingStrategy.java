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
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.ActionAttractionField;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.Double.min;
import static java.lang.Math.exp;
import static java.util.Comparator.comparingDouble;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear.getPurseSeineGear;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class PurseSeinerFishingStrategy implements FishingStrategy {

    private static final double MOVING_THRESHOLD = 0.5;
    private final Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>> actionWeightsLoader;
    private final Function<Fisher, SetOpportunityDetector> setOpportunityLocatorProvider;
    private final Map<Class<? extends PurseSeinerAction>, DoubleUnaryOperator> actionValueFunctions;
    private final Multiset<Class<? extends PurseSeinerAction>> actionCounts = HashMultiset.create();
    private final double searchActionDecayConstant;
    private final double fadDeploymentActionDecayConstant;
    private ImmutableMap<? extends Class<? extends PurseSeinerAction>, ActionAttractionField> attractionFields;
    private SetOpportunityDetector setOpportunityDetector;
    private Map<Class<? extends PurseSeinerAction>, Double> actionWeights;
    private List<Entry<PurseSeinerAction, Double>> potentialActions = ImmutableList.of();

    PurseSeinerFishingStrategy(
        final Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>> actionWeightsLoader,
        final Function<Fisher, SetOpportunityDetector> setOpportunityLocatorProvider,
        final Map<Class<? extends PurseSeinerAction>, DoubleUnaryOperator> actionValueFunctions,
        final double searchActionDecayConstant,
        final double fadDeploymentActionDecayConstant
    ) {
        this.actionWeightsLoader = actionWeightsLoader;
        this.setOpportunityLocatorProvider = setOpportunityLocatorProvider;
        this.actionValueFunctions = ImmutableMap.copyOf(actionValueFunctions);
        this.searchActionDecayConstant = searchActionDecayConstant;
        this.fadDeploymentActionDecayConstant = fadDeploymentActionDecayConstant;
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        actionWeights = normalizeWeights(actionWeightsLoader.apply(fisher));
        setOpportunityDetector = setOpportunityLocatorProvider.apply(fisher);
        attractionFields =
            getPurseSeineGear(fisher)
                .getAttractionFields()
                .stream()
                .filter(field -> field instanceof ActionAttractionField)
                .map(field -> (ActionAttractionField) field)
                .collect(toImmutableMap(
                    ActionAttractionField::getActionClass,
                    identity()
                ));
    }

    private static <T> Map<T, Double> normalizeWeights(final Map<T, Double> weightMap) {
        final double sumOfWeights =
            weightMap.values().stream().mapToDouble(Double::doubleValue).sum();
        return weightMap.entrySet().stream()
            .collect(toImmutableMap(Entry::getKey, entry -> entry.getValue() / sumOfWeights));
    }

    @Override
    public boolean shouldFish(
        final Fisher fisher,
        final MersenneTwisterFast random,
        final FishState fishState,
        final TripRecord currentTrip
    ) {
        if (potentialActions.isEmpty()) potentialActions = findPotentialActions(fisher);
        if (potentialActions.isEmpty()) actionCounts.clear();
        return !potentialActions.isEmpty();
    }

    private List<Entry<PurseSeinerAction, Double>> findPotentialActions(final Fisher fisher) {

        if (fisher.getLocation().isLand()) return ImmutableList.of();

        final Int2D gridLocation = fisher.getLocation().getGridLocation();
        final List<AbstractSetAction> possibleSetActions =
            setOpportunityDetector.possibleSetActions();

        final Stream<Entry<PurseSeinerAction, Double>> weightedSetActions =
            possibleSetActions.stream().map(action -> weightedAction(
                action,
                valueOfSetAction(action, actionValueFunctions.get(action.getClass()))
            ));

        // Generate a search action for each of the set classes with no opportunities,
        // and give them a weight equivalent to the class they replace
        final ImmutableSet<? extends Class<?>> possibleSetActionClasses =
            possibleSetActions.stream().map(Object::getClass).collect(toImmutableSet());
        final Stream<Entry<PurseSeinerAction, Double>> weightedSearchActions = Stream
            .of(
                OpportunisticFadSetAction.class,
                NonAssociatedSetAction.class,
                DolphinSetAction.class
            )
            .filter(actionClass -> !possibleSetActionClasses.contains(actionClass))
            .map(actionClass -> weightedAction(
                new SearchAction(fisher, setOpportunityDetector, actionClass),
                valueOfLocationBasedAction(
                    actionCounts.count(SearchAction.class),
                    attractionFields.get(actionClass).getActionValueAt(gridLocation),
                    actionValueFunctions.get(SearchAction.class),
                    searchActionDecayConstant
                )
            ));

        final Stream<Entry<PurseSeinerAction, Double>> weightedFadDeploymentAction = Stream
            .of(weightedAction(
                new FadDeploymentAction(fisher),
                valueOfLocationBasedAction(
                    actionCounts.count(FadDeploymentAction.class),
                    attractionFields.get(FadDeploymentAction.class).getActionValueAt(gridLocation),
                    actionValueFunctions.get(FadDeploymentAction.class),
                    fadDeploymentActionDecayConstant
                )
            ));

        return Streams
            .concat(
                weightedSetActions,
                weightedSearchActions,
                weightedFadDeploymentAction
            )
            .filter(entry -> entry.getKey().isPermitted())
            .filter(entry -> entry.getValue() > MOVING_THRESHOLD)
            .collect(toImmutableList());

    }

    private Entry<PurseSeinerAction, Double> weightedAction(
        final PurseSeinerAction action,
        final double actionValue
    ) {
        final Double w = actionWeights.getOrDefault(action.getClassForWeighting(), 0.0);
        return entry(action, actionValue * w);
    }

    private static double valueOfSetAction(
        final AbstractSetAction action,
        final DoubleUnaryOperator actionValueFunction
    ) {
        final double totalBiomass = action.getTargetBiology().getTotalBiomass();
        assert totalBiomass >= 0;
        if (totalBiomass == 0) {
            return 0; // avoids div by 0 when calculating catchableProportion
        } else {
            final Hold hold = action.getFisher().getHold();
            final double capacity = hold.getMaximumLoad() - hold.getTotalWeightOfCatchInHold();
            final double catchableProportion = min(1, capacity / totalBiomass);
            final double[] biomass = action.getTargetBiology().getCurrentBiomass();
            final double[] potentialCatch = Arrays.copyOf(biomass, biomass.length);
            for (int i = 0; i < potentialCatch.length; i++)
                potentialCatch[i] *= catchableProportion;
            final double valueOfPotentialCatch =
                new FishValueCalculator(action.getFisher()).valueOf(potentialCatch);
            return actionValueFunction.applyAsDouble(valueOfPotentialCatch);
        }
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
    public ActionResult act(
        final FishState fishState,
        final Fisher fisher,
        final Regulation regulation,
        final double hoursLeft
    ) {
        // record our visit to that tile
        getPurseSeineGear(fisher).recordVisit(fisher.getLocation().getGridLocation(), fishState.getStep());

        // Pick the potential action with the highest value or
        // get moving if there aren't any possible actions.
        final Optional<PurseSeinerAction> chosenAction =
            potentialActions.stream()
                .filter(entry -> entry.getKey().getDuration() <= hoursLeft)
                .max(comparingDouble(Entry::getValue))
                .map(Entry::getKey);
        potentialActions = ImmutableList.of();
        return chosenAction
            .map(action -> {
                actionCounts.add(action.getClass());
                return action.act(fishState, fisher, regulation, hoursLeft);
            })
            .orElse(new ActionResult(new Arriving(), 0)); // wait until tomorrow
    }

}
