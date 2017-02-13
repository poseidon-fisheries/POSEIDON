package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * returns the grid y of the observation's tile
 * Created by carrknight on 8/24/16.
 */
public class GridYExtractor implements ObservationExtractor{


    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        return tile.getGridY();
    }
}
