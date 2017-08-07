package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Basically the intercept of a logit choice regression when we are using location intercepts.
 * The key to understand this is that this extractor doesn't return the group number but rather
 * the intercept associated with that group!
 * Created by carrknight on 8/7/17.
 */
public class GroupInterceptExtractor implements ObservationExtractor{

    /**
     * intercepts, must be all provided even the ones that are 0 to avoid interference with the
     * real (non-grouped_ intercept
     */
    final private double[] intercepts;

    final private MapDiscretization discretization;


    public GroupInterceptExtractor(
            double[] intercepts, MapDiscretization discretization) {
        this.intercepts = intercepts;
        this.discretization = discretization;
    }


    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {

        int group = discretization.getGroup(tile);

        return intercepts[group];


    }
}
