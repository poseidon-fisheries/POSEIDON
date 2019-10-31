package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Streams.stream;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.utility.MasonUtils.weightedOneOf;

abstract class IntermediateDestinationsStrategy {

    protected NauticalMap map;

    @NotNull
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Deque<SeaTile>> currentRoute = Optional.empty();

    IntermediateDestinationsStrategy(NauticalMap map) {
        this.map = map;
    }

    void resetRoute() { currentRoute = Optional.empty(); }

    Optional<SeaTile> nextDestination(Fisher fisher, FishState model) {
        if (holdFull(fisher) & !goingToPort()) goToPort(fisher);
        if (!currentRoute.isPresent()) { chooseNewRoute(fisher, model); }
        currentRoute
            .filter(route -> fisher.isAtDestination() && (fisher.isAtPort() || !fisher.canAndWantToFishHere()))
            .ifPresent(Deque::poll);
        return currentRoute.flatMap(route -> Optional.ofNullable(route.peekFirst()));
    }

    private boolean holdFull(Fisher fisher) {
        // TODO: this should be a parameter somewhere
        double holdFillProportionConsideredFull = 0.99;
        return fisher.getHold().getPercentageFilled() >= holdFillProportionConsideredFull;
    }

    /**
     * This looks at the current route (if there is one) and checks if it's going to a port.
     * We can't use Fisher::isGoingToPort because we want to check the final destination instead
     * of the immediate destination and because the port we're going to might not be the home port.
     */
    private boolean goingToPort() {
        return currentRoute
            .map(Deque::peekLast)
            .filter(SeaTile::isPortHere)
            .isPresent();
    }

    private void goToPort(Fisher fisher) {
        currentRoute = getRoute(fisher, fisher.getHomePort().getLocation());
    }

    private void chooseNewRoute(Fisher fisher, FishState model) {
        final Set<Deque<SeaTile>> possibleRoutes = possibleRoutes(fisher, model.getStep());
        if (possibleRoutes.isEmpty())
            currentRoute = Optional.empty();
        else {
            final ImmutableMap<Deque<SeaTile>, Double> candidateRoutes = findCandidateRoutes(fisher, model, possibleRoutes);
            currentRoute = Optional.of(weightedOneOf(candidateRoutes.keySet().asList(), candidateRoutes::get, model.getRandom()));
        }
    }

    protected Optional<Deque<SeaTile>> getRoute(Fisher fisher, SeaTile destination) {
        return Optional.ofNullable(
            map.getPathfinder().getRoute(map, fisher.getLocation(), destination)
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private ImmutableSet<Deque<SeaTile>> possibleRoutes(Fisher fisher, int timeStep) {
        return possibleDestinations(fisher, timeStep)
            .stream()
            .flatMap(destination -> stream(getRoute(fisher, destination)))
            .collect(toImmutableSet());
    }

    private ImmutableMap<Deque<SeaTile>, Double> findCandidateRoutes(
        Fisher fisher,
        FishState model,
        Set<Deque<SeaTile>> possibleRoutes
    ) {

        final Map<Integer, Map<SeaTile, Double>> seaTileValuesByStep = new HashMap<>();
        ToDoubleBiFunction<SeaTile, Integer> seaTileValueAtStep = (seaTile, timeStep) ->
            fisher.getRegulation().canFishHere(fisher, seaTile, model, timeStep) ?
                seaTileValuesByStep.computeIfAbsent(timeStep, step -> seaTileValuesAtStep(fisher, step)).getOrDefault(seaTile, 0.0) :
                0.0;

        final ImmutableMap<Deque<SeaTile>, Double> routeValues =
            possibleRoutes.stream().collect(toImmutableMap(
                identity(),
                route -> routeValue(route, seaTileValueAtStep, fisher, model.getStep(), model.getHoursPerStep())
            ));

        final ImmutableMap<Deque<SeaTile>, Double> positiveRoutes =
            routeValues.entrySet().stream()
                .filter(entry -> entry.getValue() >= 0)
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

        return positiveRoutes.isEmpty() ?
            routeValues.entrySet().stream().collect(toImmutableMap(Map.Entry::getKey, e -> 1.0 / -e.getValue())) :
            positiveRoutes;
    }

    abstract Set<SeaTile> possibleDestinations(Fisher fisher, int timeStep);

    abstract Map<SeaTile, Double> seaTileValuesAtStep(Fisher fisher, int timeStep);

    private double routeValue(
        Deque<SeaTile> route,
        ToDoubleBiFunction<SeaTile, Integer> seaTileValueAtStep,
        Fisher fisher,
        int timeStep,
        double hoursPerStep
    ) {
        final ImmutableList<Pair<SeaTile, Double>> cumulativeDistances =
            map.getDistance().cumulativeDistanceAlongRoute(route, map);
        final ImmutableList<Pair<SeaTile, Double>> travelTimesInHours =
            cumulativeDistances.stream()
                .map(pair -> pair.mapSecond(fisher::hypotheticalTravelTimeToMoveThisMuchAtFullSpeed))
                .collect(toImmutableList());
        final ImmutableList<Pair<SeaTile, Double>> valuesAlongRoute =
            travelTimesInHours.stream()
                .map(pair -> pair.mapSecond((seaTile, hours) ->
                    seaTileValueAtStep.applyAsDouble(seaTile, timeStep + (int) (hours / hoursPerStep))
                )).collect(toImmutableList());
        final double totalTravelTimeInHours = getLast(travelTimesInHours).getSecond();
        final double tripRevenues = valuesAlongRoute.stream().mapToDouble(Pair::getSecond).sum();
        final double tripCost = fisher.getAdditionalTripCosts().stream()
            .mapToDouble(cost -> cost.cost(fisher, null, null, 0.0, totalTravelTimeInHours))
            .sum();
        return tripRevenues - tripCost;
    }
}
