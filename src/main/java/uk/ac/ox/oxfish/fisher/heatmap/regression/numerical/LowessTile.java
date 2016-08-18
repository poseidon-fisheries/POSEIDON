package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import com.google.common.base.Preconditions;
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


    public LowessTile(
            int dimension, double[][] uncertainty, double[] beta, double exponentialForgetting) {
        Preconditions.checkArgument(dimension>0);
        this.dimension = dimension;
        this.uncertainty = uncertainty;
        this.beta = beta;
        this.exponentialForgetting = exponentialForgetting;
    }

    public LowessTile(
            int dimension, double uncertainty, double[] beta, double exponentialForgetting) {
        this.dimension = dimension;
        this.uncertainty = new double[dimension][dimension];
        for(int i=0; i<dimension; i++)
            this.uncertainty[i][i] = uncertainty;
        this.beta = beta;
        this.exponentialForgetting = exponentialForgetting;
    }

    public void addObservation(double[] x, double y, double sigmaSquared){

        assert x.length == dimension;


        //going through the least squares filter as described here:
        //http://www.cs.tut.fi/~tabus/course/ASP/LectureNew10.pdf
        double pi[] = new double[dimension];
        for(int column=0; column<dimension; column++)
            for(int row=0; row<dimension; row++)
            {
                pi[column] += x[row] * uncertainty[row][column];
            }
        //gamma is basically dispersion
        double gamma = exponentialForgetting * sigmaSquared;
        for(int row=0; row<dimension; row++)
            gamma+= x[row] *  pi[row];


        //kalman gain
        double[] kalman = new double[dimension];
        for(int row=0; row<dimension; row++)
                kalman[row] = pi[row]/gamma;

        //prediction error
        double prediction = 0;
        for(int i=0; i<x.length; i++)
            prediction += x[i] * beta[i];
        double predictionError = y - prediction;

        //update beta
        for(int i=0; i<dimension; i++)
            beta[i] += predictionError * kalman[i];

        //get P'
        final double[][] prime = new double[dimension][dimension];
        for(int row=0; row<dimension; row++)
            for(int column=0; column<dimension; column++)
                prime[row][column] = kalman[row] * pi[column];

        //update uncertainty
        for(int row=0; row<dimension; row++)
            for(int column=0; column<dimension; column++)
            {
                uncertainty[row][column]-=prime[row][column];
                uncertainty[row][column]/=exponentialForgetting;
            }


    }

    /**
     * Getter for property 'beta'.
     *
     * @return Value for property 'beta'.
     */
    public double[] getBeta() {
        return beta;
    }
}
