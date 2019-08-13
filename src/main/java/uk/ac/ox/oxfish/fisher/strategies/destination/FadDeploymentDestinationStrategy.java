package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Deque;
import java.util.HashMap;
import java.util.function.BiFunction;

import static java.util.Comparator.comparingDouble;

public class FadDeploymentDestinationStrategy implements DestinationStrategy {

    private final BiFunction<SeaTile, SeaTile, Deque<SeaTile>> getRoute;
    private final HashMap<SeaTile, Double> deploymentLocationValues;

    public FadDeploymentDestinationStrategy(
        NauticalMap map,
        HashMap<SeaTile, Double> deploymentLocationValues
    ) {
        this.deploymentLocationValues = deploymentLocationValues;
        this.getRoute = (start, end) -> map.getPathfinder().getRoute(map, start, end);
    }

    @Override public SeaTile chooseDestination(
        Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction
    ) {
        return deploymentLocationValues.keySet().stream()
            .max(comparingDouble(destination ->
                getRoute.apply(fisher.getLocation(), destination)
                    .stream().mapToDouble(deploymentLocationValues::get)
                    .sum()
            ))
            .orElseThrow(() -> new IllegalStateException("Deployment location values not initialized."));
    }

    @Override public void start(FishState model, Fisher fisher) { }

    @Override public void turnOff(Fisher fisher) { }
}
