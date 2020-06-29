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
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;
import static java.util.stream.IntStream.rangeClosed;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.MasonUtils.weightedOneOf;

public abstract class AbstractRouteSelector implements RouteSelector {

    private final FishState fishState;
    private final double hoursPerStep;
    private final double travelSpeedMultiplier;
    private double maxTravelTimeInHours;

    AbstractRouteSelector(
        FishState fishState,
        double maxTravelTimeInHours,
        double travelSpeedMultiplier
    ) {
        this.fishState = fishState;
        this.maxTravelTimeInHours = maxTravelTimeInHours;
        this.hoursPerStep = fishState.getHoursPerStep();
        this.travelSpeedMultiplier = travelSpeedMultiplier;
    }

    static ImmutableList<Integer> getTimeStepRange(int startingStep, Collection<PossibleRoute> possibleRoutes) {
        final int maxTimeStep = possibleRoutes.stream().mapToInt(PossibleRoute::getLastTimeStep).max().orElse(0);
        return rangeClosed(startingStep, maxTimeStep).boxed().collect(toImmutableList());
    }

    boolean canFishAtStep(Fisher fisher, SeaTile seaTile, int timeStep) {
        return fisher.getRegulation().canFishHere(fisher, seaTile, fishState, timeStep);
    }

    @Override public Optional<Route> selectRoute(Fisher fisher, int timeStep, MersenneTwisterFast rng) {

        if (shouldGoToPort(fisher)) return Optional.empty();

        final ImmutableList<PossibleRoute> possibleRoutes =
            getPossibleRoutes(fisher, fishState.getMap(), getPossibleDestinations(fisher, timeStep), timeStep);

        final List<Entry<Route, Double>> candidateRoutes =
            evaluateRoutes(fisher, possibleRoutes, timeStep)
                .collect(toImmutableList());

        if (candidateRoutes.isEmpty()) return Optional.empty();

        final Stream<Entry<Route, Double>> routeWeights;

        if (candidateRoutes.stream().anyMatch(entry -> entry.getValue() > 0))
            // we have at least one positive, so we can get rid of the negatives
            routeWeights = candidateRoutes.stream()
                .filter(entry -> entry.getValue() > 0);
        else if (candidateRoutes.stream().anyMatch(entry -> entry.getValue() == 0))
            // we have no positives, but at least one zero: keep only the zeros and weight them to one
            routeWeights = candidateRoutes.stream()
                .filter(entry -> entry.getValue() == 0)
                .map(entry -> entry(entry.getKey(), 1.0));
        else
            // if we're left with only negatives, just minimize the cost by weighting them to 1/-value
            routeWeights = candidateRoutes.stream()
                .map(entry -> entry(entry.getKey(), 1.0 / -entry.getValue()));

        return Optional
            .of(weightedOneOf(routeWeights.collect(toImmutableList()), Entry::getValue, rng))
            .map(Entry::getKey);
    }

    abstract boolean shouldGoToPort(Fisher fisher);

    @SuppressWarnings("UnstableApiUsage")
    ImmutableList<PossibleRoute> getPossibleRoutes(
        final Fisher fisher,
        final NauticalMap map,
        final Collection<SeaTile> possibleDestinations,
        final int startingTimeStep
    ) {
        return possibleDestinations
            .stream()
            .flatMap(destinationTile -> stream(getRoute(fisher.getLocation(), destinationTile)))
            .map(routeDeque -> new PossibleRoute(
                routeDeque,
                startingTimeStep,
                hoursPerStep,
                fisher.getBoat().getSpeedInKph() * travelSpeedMultiplier,
                map,
                fisher.getHomePort()
            ))
            .filter(route -> route.getTotalTravelTimeAndBackToPortInHours() <= getMaxTravelTimeInHours())
            .collect(toImmutableList());
    }

    abstract Set<SeaTile> getPossibleDestinations(Fisher fisher, int timeStep);

    abstract Stream<SimpleImmutableEntry<Route, Double>> evaluateRoutes(
        Fisher fisher, ImmutableList<PossibleRoute> possibleRoutes, int timeStep
    );

    @SuppressWarnings("WeakerAccess") public double getMaxTravelTimeInHours() { return maxTravelTimeInHours; }

    public void setMaxTravelTimeInHours(double maxTravelTimeInHours) {
        this.maxTravelTimeInHours = maxTravelTimeInHours;
    }

    Optional<Deque<SeaTile>> getRoute(SeaTile startingTile, SeaTile destination) {
        return Optional.ofNullable(fishState.getMap().getRoute(startingTile, destination));
    }

}
