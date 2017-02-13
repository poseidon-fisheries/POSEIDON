package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Created by carrknight on 2/13/17.
 */
public class GasPriceExtractor implements ObservationExtractor {


    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        return agent.getHomePort().getGasPricePerLiter();
    }
}
