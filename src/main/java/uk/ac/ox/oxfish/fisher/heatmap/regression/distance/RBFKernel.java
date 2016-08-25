package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Takes a regression distance and makes it into RBF kernel. Notice that in reality we probably want 1/this for distance
 * since Kernels are a measure of similarity
 * Created by carrknight on 8/14/16.
 */
public class RBFKernel implements RegressionDistance
{



    private double bandwidth;

    public RBFKernel(double bandwidth) {
        this.bandwidth = bandwidth;
    }


    @Override
    public double distance(double firstObservation, double secondObservation) {
        double distance = firstObservation - secondObservation;
        return Math.exp(- distance*distance/(bandwidth));
    }


    /**
     * utility method to use when you already have a difference and you know you will use RBF
     * @param difference
     * @return
     */
    public double transform(double difference){
        return  Math.exp(- difference*difference/(bandwidth));
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }
}
