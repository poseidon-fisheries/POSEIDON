package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;
import java.util.Optional;
import java.util.function.BiFunction;

abstract class IntermediateDestinationsStrategy {

    protected final BiFunction<SeaTile, SeaTile, Deque<SeaTile>> getRoute;
    Deque<SeaTile> currentRoute = null;

    IntermediateDestinationsStrategy(NauticalMap map) {
        this.getRoute = (start, end) -> map.getPathfinder().getRoute(map, start, end);
    }

    void resetRoute() { currentRoute = null; }

    Optional<SeaTile> nextDestination(Fisher fisher, MersenneTwisterFast random) {
        if (currentRoute == null) chooseNewRoute(fisher.getLocation(), random);
        if (fisher.isAtDestination() && (fisher.isAtPort() || !fisher.canAndWantToFishHere()))
            currentRoute.poll();
        return Optional.ofNullable(currentRoute.peekFirst());
    }

    protected abstract void chooseNewRoute(SeaTile currentLocation, MersenneTwisterFast random);

}
