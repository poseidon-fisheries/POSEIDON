package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Returns day of the year
 * Created by carrknight on 2/13/17.
 */
public class DayOfTheYearExtractor implements ObservationExtractor {


    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        return model.getDayOfTheYear();
    }
}
