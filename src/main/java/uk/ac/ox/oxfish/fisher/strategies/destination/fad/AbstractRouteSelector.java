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
import uk.ac.ox.oxfish.utility.Pair;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;
import static java.util.stream.IntStream.rangeClosed;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;
import static uk.ac.ox.oxfish.utility.MasonUtils.weightedOneOf;

public abstract class AbstractRouteSelector implements RouteSelector {

    final FishState fishState;
    private final double hoursPerStep;
    private final double travelSpeedMultiplier;
    double maxTravelTimeInHours;

    protected AbstractRouteSelector(
        FishState fishState,
        double maxTravelTimeInHours,
        double travelSpeedMultiplier
    ) {
        this.fishState = fishState;
        this.maxTravelTimeInHours = maxTravelTimeInHours;
        this.hoursPerStep = fishState.getHoursPerStep();
        this.travelSpeedMultiplier = travelSpeedMultiplier;
    }

    static ImmutableList<Integer> getTimeStepRange(int startingStep, ImmutableList<Route> possibleRoutes) {
        final int maxTimeStep = possibleRoutes.stream().mapToInt(Route::getLastTimeStep).max().orElse(0);
        return rangeClosed(startingStep, maxTimeStep).boxed().collect(toImmutableList());
    }

    public double getMaxTravelTimeInHours() { return maxTravelTimeInHours; }

    public void setMaxTravelTimeInHours(double maxTravelTimeInHours) { this.maxTravelTimeInHours = maxTravelTimeInHours; }

    boolean canFishAtStep(Fisher fisher, SeaTile seaTile, int timeStep) {
        return fisher.getRegulation().canFishHere(fisher, seaTile, fishState, timeStep);
    }

    @Override public Optional<Deque<SeaTile>> selectRoute(Fisher fisher, int timeStep, MersenneTwisterFast rng) {
        if (shouldGoToPort(fisher)) return Optional.empty();
        final ImmutableList<Route> possibleRoutes = getPossibleRoutes(
            fisher, getPossibleDestinations(fisher, timeStep), timeStep
        );
        if (possibleRoutes.isEmpty())
            return Optional.empty();
        else {
            final ImmutableList<Map.Entry<Deque<SeaTile>, Double>> candidateRoutes =
                evaluateRoutes(fisher, possibleRoutes, timeStep)
                    .filter(entry -> entry.getValue() > 0)
                    .collect(toImmutableList());
            if (candidateRoutes.isEmpty())
                return Optional.empty();
            else
                return Optional.of(weightedOneOf(candidateRoutes, Map.Entry::getValue, rng)).map(Map.Entry::getKey);
        }
    }

    abstract boolean shouldGoToPort(Fisher fisher);

    @SuppressWarnings("UnstableApiUsage")
    ImmutableList<Route> getPossibleRoutes(
        Fisher fisher,
        Set<SeaTile> possibleDestinations,
        int startingTimeStep
    ) {
        return possibleDestinations
            .stream()
            .flatMap(destinationTile -> stream(getRoute(fisher, fisher.getLocation(), destinationTile)))
            .map(routeDeque -> new Route(
                routeDeque, startingTimeStep, hoursPerStep,
                fisher.getBoat().getSpeedInKph() * travelSpeedMultiplier,
                this::cumulativeTravelTimeAlongRouteInHours
            ))
            .filter(route -> route.getTotalTravelTimeInHours() <= maxTravelTimeInHours)
            .collect(toImmutableList());
    }

    abstract Set<SeaTile> getPossibleDestinations(Fisher fisher, int timeStep);

    abstract Stream<SimpleImmutableEntry<Deque<SeaTile>, Double>> evaluateRoutes(
        Fisher fisher, ImmutableList<Route> routes, int timeStep
    );

    Optional<Deque<SeaTile>> getRoute(Fisher fisher, SeaTile startingTile, SeaTile destination) {
        NauticalMap map = fishState.getMap();
        return Optional.ofNullable(map.getPathfinder().getRoute(map, startingTile, destination));
    }

    private ImmutableList<Pair<SeaTile, Double>> cumulativeTravelTimeAlongRouteInHours(Deque<SeaTile> route, Double speedInKph) {
        NauticalMap map = fishState.getMap();
        return map.getDistance().cumulativeTravelTimeAlongRouteInHours(route, map, speedInKph);
    }

}
