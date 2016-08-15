package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RegressionDistance;

/**
 * Classic RLS filter where sigma^2 is 1/distance function as provided
 * Created by carrknight on 8/15/16.
 */
public class LowessTile {


    private final int dimension;

    private final double[][] uncertainty;

    private final double[] beta;


    private final double exponentialForgetting;


    private final RegressionDistance distance;

    public LowessTile(
            int dimension, double[][] uncertainty, double[] beta, double exponentialForgetting,
            RegressionDistance distance) {
        this.dimension = dimension;
        this.uncertainty = uncertainty;
        this.beta = beta;
        this.exponentialForgetting = exponentialForgetting;
        this.distance = distance;
    }

    public void addObservation(double[] x, double y, double sigmaSquared){

        assert x.length == dimension;

        //prediction error
        double prediction = 0;
        for(int i=0; i<x.length; i++)
            prediction += x[i] * beta[i];
        double predictionError = y - prediction;

        //dispersion
        double[] intermediateSums = new double[dimension];
        for(int sum=0; sum<dimension; sum++)
            for(int i=0; i<dimension; i++)
                intermediateSums[sum] += uncertainty[sum][i] * x[i];
        double dispersion = 0;
        for(int sum=0; sum<dimension; sum++)
            dispersion += intermediateSums[sum] * x[sum];
        dispersion += exponentialForgetting* sigmaSquared;

        //kalman gain
        double[] kalman = new double[dimension];
        for(int i=0; i<dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                kalman[i] += uncertainty[i][j] * x[j];
            }
            kalman[i]/=dispersion;
        }

        //update beta
        for(int i=0; i<dimension; i++)
            beta[i] += predictionError * kalman[i];

        //update P


    }


}
