package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toList;
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

    void resetRoute() { currentRoute = Optional.empty(); }

    Optional<SeaTile> nextDestination(Fisher fisher, MersenneTwisterFast random) {
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
        if (possibleRoutes.isEmpty())
            currentRoute = Optional.empty();
        else {
            Function<Integer, Double> destinationValue = i ->
                possibleRoutes.get(i)
                    .stream()
                    .mapToDouble(seaTile -> seaTileValue(fisher, seaTile))
                    .sum();
            currentRoute = Optional.of(
                possibleRoutes.get(drawFromSoftmax(random, possibleRoutes.size(), destinationValue))
            );
        }
    }
}
