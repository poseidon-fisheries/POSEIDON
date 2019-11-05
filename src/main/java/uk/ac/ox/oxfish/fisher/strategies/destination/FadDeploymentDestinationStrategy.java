package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;

public class FadDeploymentDestinationStrategy extends IntermediateDestinationsStrategy {

    private Map<SeaTile, Double> deploymentLocationValues = null;

    public FadDeploymentDestinationStrategy(NauticalMap map) { super(map); }

    @SuppressWarnings("unused")
    public Map<SeaTile, Double> getDeploymentLocationValues() { return deploymentLocationValues; }

    public void setDeploymentLocationValues(Map<SeaTile, Double> deploymentLocationValues) {
        this.deploymentLocationValues = deploymentLocationValues;
    }

    @Override
    Set<SeaTile> possibleDestinations(Fisher fisher, int timeStep) { return deploymentLocationValues.keySet(); }

    @Override ToDoubleBiFunction<SeaTile, Integer> seaTileValueAtStepFunction(Fisher fisher, FishState fishState) {
        final Table<SeaTile, Integer, Double> seaTileValuesByStep = HashBasedTable.create();
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
