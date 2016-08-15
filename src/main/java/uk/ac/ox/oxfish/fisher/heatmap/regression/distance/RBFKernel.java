package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Takes a regression distance and makes it into RBF distance
 * Created by carrknight on 8/14/16.
 */
public class RBFKernel implements RegressionDistance
{


    private final RegressionDistance delegate;

    private final double bandwidth;

    public RBFKernel(RegressionDistance delegate, double bandwidth) {
        this.delegate = delegate;
        this.bandwidth = bandwidth;
    }

    @Override
    public double distance(
            Fisher fisher, SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {
        double distance = delegate.distance(fisher,tile,currentTimeInHours,observation);
        return Math.exp(- distance*distance/(bandwidth));


    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public RegressionDistance getDelegate() {
        return delegate;
    }

    /**
     * Getter for property 'bandwidth'.
     *
     * @return Value for property 'bandwidth'.
     */
    public double getBandwidth() {
        return bandwidth;
    }
}
