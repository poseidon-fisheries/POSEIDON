package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

/**
 * Created by carrknight on 9/2/16.
 */
public class EpanechinikovKernel implements RegressionDistance {

    private double bandwidth;

    public EpanechinikovKernel(double bandwidth) {
        this.bandwidth = bandwidth;
    }


    @Override
    public double distance(double firstObservation, double secondObservation) {
        double distance = firstObservation-secondObservation;
        return  Math.max(.75 * (1- distance*distance/bandwidth),0);

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
