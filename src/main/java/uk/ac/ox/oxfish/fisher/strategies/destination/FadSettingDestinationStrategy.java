package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.ImmutableMap;
import sim.util.Bag;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Streams.stream;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.fadsAt;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;

public class FadSettingDestinationStrategy extends IntermediateDestinationsStrategy implements FadManagerUtils {

    private final Bag allSeaTiles;

    public FadSettingDestinationStrategy(NauticalMap map) {
        super(map);
        allSeaTiles = map.getAllSeaTiles();
    }

    @Override
    protected Optional<Deque<SeaTile>> getRoute(Fisher fisher, SeaTile destination) {
        return super.getRoute(fisher, destination)
            .flatMap(route ->
                Optional.ofNullable(map.getPathfinder().getRoute(map, destination, fisher.getHomePort().getLocation()))
                    .map(routeBackToPort -> {
                        routeBackToPort.removeFirst();
                        route.addAll(routeBackToPort);
                        return route;
                    })
            );
    }

    @Override
    Set<SeaTile> possibleDestinations(Fisher fisher) {
        final FadManager fadManager = getFadManager(fisher);
        return fadManager.getDeployedFads()
            .stream()
            .flatMap(fad -> stream(fadManager.getFadTile(fad)))
            .collect(toImmutableSet());
    }

    @Override
    ImmutableMap<SeaTile, Double> seaTileValuesAtStep(Fisher fisher, int timeStep) {
        final Collection<Market> markets = fisher.getHomePort().getMarketMap(fisher).getMarkets();
        return getFadManager(fisher)
            .deployedFadsByTileAtStep(timeStep)
            .asMap().entrySet().stream()
            .collect(toImmutableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().mapToDouble(fad -> fad.priceOfFishHere(markets)).sum()
            ));
    }

}
