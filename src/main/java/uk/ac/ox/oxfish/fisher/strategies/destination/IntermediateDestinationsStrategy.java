package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Streams.stream;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.utility.MasonUtils.weightedOneOf;

abstract class IntermediateDestinationsStrategy {

    protected NauticalMap map;

    @NotNull
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Deque<SeaTile>> currentRoute = Optional.empty();
    // TODO: this should be a parameter somewhere
    private double holdFillProportionConsideredFull = 0.99;

    IntermediateDestinationsStrategy(NauticalMap map) {
        this.map = map;
    }

    protected Optional<Deque<SeaTile>> getRoute(Fisher fisher, SeaTile destination) {
        return Optional.ofNullable(
            map.getPathfinder().getRoute(map, fisher.getLocation(), destination)
        );
    }

    /**
     * This looks at the current route (if there is one) and checks if it's going to a port.
     * We can't use Fisher::isGoingToPort because we want to check the final destination instead
     * of the immediate destination and because the port we're going to might not be the home port.
     */
    private boolean goingToPort() {
        return currentRoute
            .map(Deque::peekLast)
            .filter(seaTile -> seaTile != null && seaTile.isPortHere())
            .isPresent();
    }

    private void goToPort(Fisher fisher) {
        currentRoute = getRoute(fisher, fisher.getHomePort().getLocation());
    }

    private boolean holdFull(Fisher fisher) {
        return fisher.getHold().getPercentageFilled() >= holdFillProportionConsideredFull;
    }

    void resetRoute() { currentRoute = Optional.empty(); }

    Optional<SeaTile> nextDestination(Fisher fisher, MersenneTwisterFast random) {
        if (holdFull(fisher) & !goingToPort()) goToPort(fisher);
        if (!currentRoute.isPresent()) { chooseNewRoute(fisher, random); }
        currentRoute
            .filter(route -> fisher.isAtDestination() && (fisher.isAtPort() || !fisher.canAndWantToFishHere()))
            .ifPresent(Deque::poll);
        return currentRoute.flatMap(route -> Optional.ofNullable(route.peekFirst()));
    }

    abstract Set<SeaTile> possibleDestinations(Fisher fisher);

    private ImmutableSet<Deque<SeaTile>> possibleRoutes(Fisher fisher) {
        return possibleDestinations(fisher)
            .stream()
            .flatMap(destination -> stream(getRoute(fisher, destination)))
            .collect(toImmutableSet());
    }

    abstract double seaTileValue(Fisher fisher, SeaTile seaTile);

    private void chooseNewRoute(Fisher fisher, MersenneTwisterFast random) {
        final Set<Deque<SeaTile>> possibleRoutes = possibleRoutes(fisher);

        if (possibleRoutes.isEmpty())
            currentRoute = Optional.empty();
        else {
            final Map<SeaTile, Double> seaTileValues =
                possibleRoutes.stream()
                    .flatMap(Deque::stream)
                    .distinct()
                    .collect(toImmutableMap(identity(), seaTile -> seaTileValue(fisher, seaTile)));

            final Map<Deque<SeaTile>, Double> routeValues =
                possibleRoutes.stream().collect(toImmutableMap(
                    identity(),
                    route -> routeValue(route, seaTileValues::get, fisher)
                ));

            final ImmutableMap<Deque<SeaTile>, Double> positiveRoutes =
                routeValues.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 0)
                    .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

            final ImmutableMap<Deque<SeaTile>, Double> candidateRoutes =
                positiveRoutes.isEmpty() ?
                    routeValues.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> 1.0 / -e.getValue())) :
                    positiveRoutes;

            currentRoute = Optional.of(weightedOneOf(candidateRoutes.keySet().asList(), candidateRoutes::get, random));
        }
    }

    private double routeValue(Deque<SeaTile> route, ToDoubleFunction<SeaTile> seaTileValue, Fisher fisher) {
        final double distanceInKm = map.getDistance().distanceAlongPath(route, map);
        final double travelTimeInHours = fisher.hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(distanceInKm);
        final double tripRevenues = route.stream().mapToDouble(seaTileValue).sum();
        final double tripCost = fisher.getAdditionalTripCosts().stream()
            .mapToDouble(cost -> cost.cost(fisher, null, null, 0.0, travelTimeInHours))
            .sum();
        return tripRevenues - tripCost;
    }
}
