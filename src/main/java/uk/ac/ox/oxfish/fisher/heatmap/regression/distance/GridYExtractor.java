package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * returns the grid y of the observation's tile
 * Created by carrknight on 8/24/16.
 */
public class GridYExtractor implements ObservationExtractor{


    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent) {
        return tile.getGridY();
    }
}
