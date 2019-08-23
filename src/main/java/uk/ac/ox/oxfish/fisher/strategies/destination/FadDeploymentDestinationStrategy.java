package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.ac.ox.oxfish.utility.bandit.SoftmaxBanditAlgorithm.drawFromSoftmax;

public class FadDeploymentDestinationStrategy extends IntermediateDestinationsStrategy {

    private Map<SeaTile, Double> deploymentLocationValues = null;

    public FadDeploymentDestinationStrategy(NauticalMap map) { super(map); }

    @SuppressWarnings("unused")
    public Map<SeaTile, Double> getDeploymentLocationValues() { return deploymentLocationValues; }

    public void setDeploymentLocationValues(Map<SeaTile, Double> deploymentLocationValues) {
        this.deploymentLocationValues = deploymentLocationValues;
    }

    protected void chooseNewRoute(SeaTile currentLocation, MersenneTwisterFast random) {

        final List<Deque<SeaTile>> routes = deploymentLocationValues.keySet().stream()
            .map(destination -> getRoute.apply(currentLocation, destination))
            .collect(Collectors.toList());

        Function<Integer, Double> destinationValue = i ->
            routes.get(i).stream()
                .mapToDouble(seaTile -> deploymentLocationValues.getOrDefault(seaTile, 0.0))
                .sum();

        currentRoute = routes.get(drawFromSoftmax(random, routes.size(), destinationValue));
    }

}
