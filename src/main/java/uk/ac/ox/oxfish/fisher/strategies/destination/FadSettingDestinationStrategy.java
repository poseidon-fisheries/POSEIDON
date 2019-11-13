package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;

public class FadSettingDestinationStrategy extends IntermediateDestinationsStrategy implements FadManagerUtils {

    private final int NUM_STEPS_TO_LOOK_AHEAD = 30; // TODO: make this a parameter

    public FadSettingDestinationStrategy(NauticalMap map) { super(map); }

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
    Set<SeaTile> possibleDestinations(Fisher fisher, int timeStep) {
        return getFadManager(fisher).fadLocationsInTimeStepRange(timeStep, timeStep + NUM_STEPS_TO_LOOK_AHEAD);
    }

    @Override ToDoubleBiFunction<SeaTile, Integer> seaTileValueAtStepFunction(
        Fisher fisher,
        FishState fishState,
        IntStream possibleSteps
    ) {
        final ImmutableMap<Integer, ImmutableMap<SeaTile, Double>> seaTileValuesByStep =
            possibleSteps.boxed().collect(toImmutableMap(
                identity(),
                step -> seaTileValuesAtStep(fisher, fishState, step)
            ));
        return (seaTile, timeStep) -> seaTileValuesByStep.get(timeStep).getOrDefault(seaTile, 0.0);
    }

    private ImmutableMap<SeaTile, Double> seaTileValuesAtStep(Fisher fisher, FishState fishState, int timeStep) {
        final Collection<Market> markets = fisher.getHomePort().getMarketMap(fisher).getMarkets();
        return getFadManager(fisher)
            .deployedFadsByTileAtStep(timeStep)
            .asMap().entrySet().stream()
            .filter(entry -> fisher.getRegulation().canFishHere(fisher, entry.getKey(), fishState, timeStep))
            .collect(toImmutableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().mapToDouble(fad -> fad.priceOfFishHere(markets)).sum()
            ));
    }
}
