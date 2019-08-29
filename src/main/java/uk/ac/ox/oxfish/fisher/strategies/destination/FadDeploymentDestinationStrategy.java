package uk.ac.ox.oxfish.fisher.strategies.destination;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Map;
import java.util.Set;

public class FadDeploymentDestinationStrategy extends IntermediateDestinationsStrategy {

    private Map<SeaTile, Double> deploymentLocationValues = null;

    public FadDeploymentDestinationStrategy(NauticalMap map) { super(map); }

    @SuppressWarnings("unused")
    public Map<SeaTile, Double> getDeploymentLocationValues() { return deploymentLocationValues; }

    public void setDeploymentLocationValues(Map<SeaTile, Double> deploymentLocationValues) {
        this.deploymentLocationValues = deploymentLocationValues;
    }

    @Override
    Set<SeaTile> possibleDestinations(Fisher fisher) { return deploymentLocationValues.keySet(); }

    @Override
    double seaTileValue(Fisher fisher, SeaTile seaTile) {
        return deploymentLocationValues.getOrDefault(seaTile, 0.0);
    }

}
