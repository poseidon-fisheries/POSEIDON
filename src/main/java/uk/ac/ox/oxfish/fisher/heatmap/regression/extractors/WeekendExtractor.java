package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Created by carrknight on 4/19/17.
 */
public class WeekendExtractor implements ObservationExtractor {


    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        return (SeasonExtractor.getDaySinceStartFromHoursSinceStart(timeOfObservation,model)) % 7
                >=5 ? 1 : 0;
    }


}
