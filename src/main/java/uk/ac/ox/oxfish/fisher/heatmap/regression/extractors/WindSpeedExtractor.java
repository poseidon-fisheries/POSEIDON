package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Wind speed
 * Created by carrknight on 2/13/17.
 */
public class WindSpeedExtractor implements ObservationExtractor {
    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        return tile.getWindSpeedInKph();
    }
}
