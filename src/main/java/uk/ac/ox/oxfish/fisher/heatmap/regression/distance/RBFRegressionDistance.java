package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

/**
 * 1/rbfKernel
 * Created by carrknight on 8/24/16.
 */
public class RBFRegressionDistance implements RegressionDistance
{


    private final RBFKernel delegate;


    public RBFRegressionDistance(double bandwidth) {
        this.delegate = new RBFKernel(bandwidth);
    }


    @Override
    public double distance(double firstObservation, double secondObservation) {
        return delegate.distance(firstObservation, secondObservation);
    }

    public double getBandwidth() {
        return delegate.getBandwidth();
    }

    public void setBandwidth(double bandwidth) {
        delegate.setBandwidth(bandwidth);
    }
}
