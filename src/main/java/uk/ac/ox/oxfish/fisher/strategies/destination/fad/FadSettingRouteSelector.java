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

package uk.ac.ox.oxfish.fisher.strategies.destination.fad;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeFadSet;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.regs.fads.SetLimits;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.Math.min;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class FadSettingRouteSelector extends AbstractRouteSelector {

    private int numberOfStepsToLookAheadForFadPositions;

    public FadSettingRouteSelector(
        FishState fishState,
        double maxTravelTimeInHours,
        double travelSpeedMultiplier,
        int numberOfStepsToLookAheadForFadPositions
    ) {
        super(fishState, maxTravelTimeInHours, travelSpeedMultiplier);
        this.numberOfStepsToLookAheadForFadPositions = numberOfStepsToLookAheadForFadPositions;
    }

    @Override boolean shouldGoToPort(Fisher fisher) {
        double holdFillProportionConsideredFull = 0.99; // TODO: this should be a parameter somewhere
        boolean holdFull = fisher.getHold().getPercentageFilled() >= holdFillProportionConsideredFull;
        final boolean anyLimitedActionsRemaining =
            getFadManager(fisher).getActionSpecificRegulations().anyYearlyLimitedActionRemaining(fisher);
        return holdFull || !anyLimitedActionsRemaining;
    }

    @Override public Set<SeaTile> getPossibleDestinations(Fisher fisher, int timeStep) {
        return getFadManager(fisher).fadLocationsInTimeStepRange(
            timeStep, timeStep + getNumberOfStepsToLookAheadForFadPositions()
        );
    }

    @Override public Stream<SimpleImmutableEntry<Route, Double>> evaluateRoutes(
        Fisher fisher,
        ImmutableList<PossibleRoute> possibleRoutes,
        int timeStep
    ) {
        final FadManager fadManager = getFadManager(fisher);
        final ImmutableMap<Integer, ImmutableSetMultimap<SeaTile, Fad>> fadsByTileByStep =
            fadsByTileByStep(fadManager, possibleRoutes, timeStep);
        final long fadSetsRemaining = getFadSetsRemaining(fadManager);

        return possibleRoutes.stream().map(possibleRoute -> {
            final double possibleRevenues = possibleRoute.getSteps().stream()
                .filter(routeStep -> canFishAtStep(fisher, routeStep.getSeaTile(), routeStep.getTimeStep()))
                .flatMap(routeStep ->
                    fadsByTileByStep
                        .get(routeStep.getTimeStep())
                        .get(routeStep.getSeaTile())
                        .stream()
                        .map(fad -> fad.valueOfSet(fisher))
                )
                .sorted(reverseOrder())
                .limit(fadSetsRemaining)
                .mapToDouble(Double::doubleValue)
                .sum();
            return entry(
                possibleRoute.makeRoute(fisher),
                min(possibleRevenues, maxPossibleRevenues(fisher)) - possibleRoute.getCost(fisher)
            );
        });
    }

    private ImmutableMap<Integer, ImmutableSetMultimap<SeaTile, Fad>> fadsByTileByStep(
        FadManager fadManager,
        Collection<PossibleRoute> routes,
        int timeStep
    ) {
        return getTimeStepRange(timeStep, routes).stream()
            .collect(toImmutableMap(identity(), fadManager::deployedFadsByTileAtStep));
    }

    public static long getFadSetsRemaining(FadManager fadManager) {
        return fadManager.getActionSpecificRegulations()
            .regulationStream(MakeFadSet.class)
            .filter(reg -> reg instanceof SetLimits)
            .mapToLong(reg -> ((SetLimits) reg).getNumRemainingActions(fadManager.getFisher()))
            .min()
            .orElse(Long.MAX_VALUE);
    }

    private double maxPossibleRevenues(final Fisher fisher) {
        // calc max possible value given space in hold, using the highest species price
        final double highestPrice =
            fisher.getHomePort().getMarketMap(fisher).getMarkets().stream()
                .mapToDouble(Market::getMarginalPrice)
                .max()
                .orElseThrow(() -> new IllegalStateException("No sellable species for " + fisher));
        final double availableHoldCapacity =
            fisher.getMaximumHold() - fisher.getTotalWeightOfCatchInHold();
        return highestPrice * availableHoldCapacity;
    }

    @SuppressWarnings("WeakerAccess") public int getNumberOfStepsToLookAheadForFadPositions() {
        return numberOfStepsToLookAheadForFadPositions;
    }

    public void setNumberOfStepsToLookAheadForFadPositions(int numberOfStepsToLookAheadForFadPositions) {
        this.numberOfStepsToLookAheadForFadPositions = numberOfStepsToLookAheadForFadPositions;
    }

}
