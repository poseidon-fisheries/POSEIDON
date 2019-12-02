package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class FadDeploymentDestinationStrategy extends IntermediateDestinationsStrategy {

    private final ImmutableList<SeaTile> possibleRouteTiles; // will serve as row keys for our ArrayTable of values
    private Map<SeaTile, Double> deploymentLocationValues = null;

    public FadDeploymentDestinationStrategy(
        NauticalMap map,
        double travelSpeedMultiplier
    ) {
        super(map, travelSpeedMultiplier);
        possibleRouteTiles = Stream.concat(
            map.getPorts().stream().map(Port::getLocation),
            map.getAllSeaTilesExcludingLandAsList().stream()
        ).collect(toImmutableList());
    }

    @SuppressWarnings("unused")
    public Map<SeaTile, Double> getDeploymentLocationValues() { return deploymentLocationValues; }

    public void setDeploymentLocationValues(Map<SeaTile, Double> deploymentLocationValues) {
        this.deploymentLocationValues = deploymentLocationValues;
    }

    @Override
    Set<SeaTile> possibleDestinations(Fisher fisher, int timeStep) { return deploymentLocationValues.keySet(); }

    @SuppressWarnings("UnstableApiUsage")
    @Override ToDoubleBiFunction<SeaTile, Integer> seaTileValueAtStepFunction(
        Fisher fisher,
        FishState fishState,
        IntStream possibleSteps
    ) {
        final Table<SeaTile, Integer, Double> seaTileValuesByStep = ArrayTable.create(
            possibleRouteTiles,
            possibleSteps.boxed().collect(toImmutableList())
        );
        return (seaTile, timeStep) -> {
            final Double cachedValue = seaTileValuesByStep.get(seaTile, timeStep);
            if (cachedValue != null) return cachedValue;
            final double value = (fisher.getRegulation().canFishHere(fisher, seaTile, fishState, timeStep))
                ? deploymentLocationValues.getOrDefault(seaTile, 0.0)
                : 0.0;
            seaTileValuesByStep.put(seaTile, timeStep, value);
            return value;
        };
    }
}
