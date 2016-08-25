package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * extract the grid x coordinate from the observation
 * Created by carrknight on 7/7/16.
 */
public class GridXExtractor  implements ObservationExtractor {





    @Override
    public double extract(SeaTile tile, double timeOfObservation, Fisher agent) {
        return tile.getGridX();
    }
}
