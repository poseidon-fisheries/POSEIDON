package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import sim.util.Bag;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Objects;
import java.util.stream.Stream;

import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

public class FadSettingDestinationStrategy extends IntermediateDestinationsStrategy {

    private final Bag allSeaTiles;

    public FadSettingDestinationStrategy(NauticalMap map) {
        super(map);
        allSeaTiles = map.getAllSeaTiles();
    }

    @Override
    protected void chooseNewRoute(SeaTile currentLocation, MersenneTwisterFast random) {
        // just pick a totally random destination for now
        currentRoute = Stream
            .generate(() -> (SeaTile) oneOf(allSeaTiles, random).get())
            .map(destination -> getRoute.apply(currentLocation, destination))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No possible route."));
    }
}
