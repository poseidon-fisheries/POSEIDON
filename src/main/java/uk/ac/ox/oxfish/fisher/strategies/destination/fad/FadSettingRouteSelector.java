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
import uk.ac.ox.oxfish.fisher.actions.fads.MakeFadSet;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.fads.SetLimits;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Deque;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.makeEntry;

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

    @Override Optional<Deque<SeaTile>> getRoute(Fisher fisher, SeaTile startingTile, SeaTile destination) {
        final SeaTile port = fisher.getHomePort().getLocation();
        return super.getRoute(fisher, startingTile, destination)
            .flatMap(route ->
                super.getRoute(fisher, destination, port).map(routeBackToPort -> {
                    routeBackToPort.removeFirst();
                    route.addAll(routeBackToPort);
                    return route;
                })
            );
    }

    @Override public Stream<SimpleImmutableEntry<Deque<SeaTile>, Double>> evaluateRoutes(
        Fisher fisher,
        ImmutableList<Route> routes,
        int timeStep
    ) {
        final FadManager fadManager = getFadManager(fisher);
        final ImmutableMap<Integer, ImmutableSetMultimap<SeaTile, Fad>> fadsByTileByStep =
            getTimeStepRange(timeStep, routes).stream()
                .collect(toImmutableMap(identity(), fadManager::deployedFadsByTileAtStep));

        final long fadSetsRemaining = fadManager.getActionSpecificRegulations()
            .regulationStream(MakeFadSet.class)
            .filter(reg -> reg instanceof SetLimits)
            .mapToLong(reg -> ((SetLimits) reg).getNumRemainingActions(fisher))
            .min()
            .orElse(Long.MAX_VALUE);

        return routes.stream().map(route -> makeEntry(
            route.getRouteDeque(),
            route.getSteps().stream()
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
                .sum() - route.getCost(fisher)
        ));
    }

    @Override public Set<SeaTile> getPossibleDestinations(Fisher fisher, int timeStep) {
        return getFadManager(fisher).fadLocationsInTimeStepRange(
            timeStep, timeStep + getNumberOfStepsToLookAheadForFadPositions()
        );
    }

    public int getNumberOfStepsToLookAheadForFadPositions() {
        return numberOfStepsToLookAheadForFadPositions;
    }

    public void setNumberOfStepsToLookAheadForFadPositions(int numberOfStepsToLookAheadForFadPositions) {
        this.numberOfStepsToLookAheadForFadPositions = numberOfStepsToLookAheadForFadPositions;
    }
}
