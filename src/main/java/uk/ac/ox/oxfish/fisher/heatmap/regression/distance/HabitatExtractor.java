package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * returns the rockiness of the tile
 * Created by carrknight on 7/7/16.
 */
public class HabitatExtractor implements ObservationExtractor {


    @Override
    public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        return tile.getRockyPercentage();
    }
}
