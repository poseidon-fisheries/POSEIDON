package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.Streams.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.utility.bandit.SoftmaxBanditAlgorithm.drawFromSoftmax;

abstract class IntermediateDestinationsStrategy {

    protected NauticalMap map;

    @NotNull
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Deque<SeaTile>> currentRoute = Optional.empty();

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

    // TODO: this should be a parameter somewhere
    private double holdFillProportionConsideredFull = 0.99;
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

    private List<Deque<SeaTile>> possibleRoutes(Fisher fisher) {
        return possibleDestinations(fisher)
            .stream()
            .flatMap(destination -> stream(getRoute(fisher, destination)))
            .collect(toList());
    }

    abstract double seaTileValue(Fisher fisher, SeaTile seaTile);

    private void chooseNewRoute(Fisher fisher, MersenneTwisterFast random) {

        final List<Deque<SeaTile>> possibleRoutes = possibleRoutes(fisher);
        final Map<SeaTile, Double> seaTileValues = possibleRoutes.stream()
            .flatMap(Deque::stream)
            .distinct()
            .collect(toMap(identity(), seaTile -> seaTileValue(fisher, seaTile)));

        if (possibleRoutes.isEmpty())
            currentRoute = Optional.empty();
        else {
            Function<Integer, Double> destinationValue = i ->
                possibleRoutes.get(i)
                    .stream()
                    .mapToDouble(seaTileValues::get)
                    .sum();
            currentRoute = Optional.of(
                possibleRoutes.get(drawFromSoftmax(random, possibleRoutes.size(), destinationValue))
            );
        }
    }
}
