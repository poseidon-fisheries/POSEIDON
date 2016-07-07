package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Created by carrknight on 7/5/16.
 */
public class SpaceTimeRegressionDistance implements RegressionDistance
{



    private final CompositeRegressionDistance delegate;


    public SpaceTimeRegressionDistance(double timeBandwidth,
                                       double spaceBandwidth) {

        delegate = new CompositeRegressionDistance(
                new SpaceRegressionDistance(spaceBandwidth),
                new TimeRegressionDistance(timeBandwidth)
        );

    }


    @Override
    public double distance(
            Fisher fisher, SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {


        return delegate.distance(fisher, tile, currentTimeInHours, observation);
    }
}
