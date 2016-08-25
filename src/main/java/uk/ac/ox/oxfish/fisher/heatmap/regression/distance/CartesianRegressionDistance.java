package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

/**
 * distance is (x-y)^2/bandwidth
 * Created by carrknight on 8/24/16.
 */
public class CartesianRegressionDistance implements RegressionDistance {




    private double bandwidth;


    public CartesianRegressionDistance(double bandwidth) {
        this.bandwidth = bandwidth;
    }

    @Override
    public double distance(double firstObservation, double secondObservation) {

        double distance = firstObservation - secondObservation;
        return distance*distance/bandwidth;
    }


    /**
     * Getter for property 'bandwidth'.
     *
     * @return Value for property 'bandwidth'.
     */
    public double getBandwidth() {
        return bandwidth;
    }

    /**
     * Setter for property 'bandwidth'.
     *
     * @param bandwidth Value to set for property 'bandwidth'.
     */
    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }


}
