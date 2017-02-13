package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Always returns the same number
 * Created by carrknight on 2/13/17.
 */
public class InterceptExtractor implements ObservationExtractor {


    private final double intercept;


    public InterceptExtractor(double intercept) {
        this.intercept = intercept;
    }

    public InterceptExtractor() {
        this(1d);
    }

    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        return intercept;
    }
}
