/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import com.google.common.base.Preconditions;

import java.util.Arrays;

/**
 * Classic RLS filter where we allow weights in terms of sigma^2
 * Created by carrknight on 8/15/16.
 */
public class LeastSquareFilter {


    private final int dimension;

    private final double[][] uncertainty;

    private final double[] beta;


    private double exponentialForgetting;


    public LeastSquareFilter(
        int dimension, double[][] uncertainty, double[] beta, double exponentialForgetting
    ) {
        Preconditions.checkArgument(dimension > 0);
        this.dimension = dimension;
        this.uncertainty = uncertainty;
        this.beta = beta;
        this.exponentialForgetting = exponentialForgetting;
    }

    public LeastSquareFilter(
        int dimension, double uncertainty, double[] beta, double exponentialForgetting
    ) {
        this.dimension = dimension;
        this.uncertainty = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++)
            this.uncertainty[i][i] = uncertainty;
        this.beta = beta;
        this.exponentialForgetting = exponentialForgetting;
    }

    public void addObservation(double[] x, double y, double sigmaSquared) {

        assert x.length == dimension;


        //going through the least squares filter as described here:
        //http://www.cs.tut.fi/~tabus/course/ASP/LectureNew10.pdf
        double pi[] = new double[dimension];
        for (int column = 0; column < dimension; column++)
            for (int row = 0; row < dimension; row++) {
                pi[column] += x[row] * uncertainty[row][column];
                assert (Double.isFinite(pi[column]));

            }
        //gamma is basically dispersion
        double gamma = exponentialForgetting * sigmaSquared;
        assert (gamma != 0);

        for (int row = 0; row < dimension; row++)
            gamma += x[row] * pi[row];

        //if the dispersion is not invertible, do not add the observation
        if (gamma == 0) {
            //  System.out.println("ignored");
            increaseUncertainty();
            return;
        }


        //kalman gain
        double[] kalman = new double[dimension];
        for (int row = 0; row < dimension; row++) {
            assert (Double.isFinite(pi[row]));
            assert (Double.isFinite(gamma));

            kalman[row] = pi[row] / gamma;

            assert (Double.isFinite(kalman[row]));


        }

        //prediction error
        double prediction = 0;
        for (int i = 0; i < x.length; i++)
            prediction += x[i] * beta[i];
        double predictionError = y - prediction;
        assert (Double.isFinite(predictionError));

        //update beta
        for (int i = 0; i < dimension; i++) {
            beta[i] += predictionError * kalman[i];
            assert Double.isFinite(beta[i]);
        }
        //get P'
        final double[][] prime = new double[dimension][dimension];
        for (int row = 0; row < dimension; row++)
            for (int column = 0; column < dimension; column++) {
                prime[row][column] = kalman[row] * pi[column];
                assert (Double.isFinite(prime[row][column])) : "pi " + pi[column] + " , kalman: " + kalman[column];

            }
        //update uncertainty
        for (int row = 0; row < dimension; row++)
            for (int column = 0; column < dimension; column++) {
                assert (Double.isFinite(prime[row][column]));

                uncertainty[row][column] -= prime[row][column];
                uncertainty[row][column] /= exponentialForgetting;
                assert (Double.isFinite(uncertainty[row][column]));

            }


    }

    /**
     * if sigma^2 is infinite the kalman will be 0 which means that the only thing actually changing is P increasing.
     * This method just applies that part
     */
    public void increaseUncertainty() {
        for (int row = 0; row < dimension; row++)
            for (int column = 0; column < dimension; column++) {
                uncertainty[row][column] /= exponentialForgetting;
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


    @Override
    public String toString() {
        return Arrays.toString(beta);
    }

    /**
     * Getter for property 'exponentialForgetting'.
     *
     * @return Value for property 'exponentialForgetting'.
     */
    public double getExponentialForgetting() {
        return exponentialForgetting;
    }

    /**
     * Setter for property 'exponentialForgetting'.
     *
     * @param exponentialForgetting Value to set for property 'exponentialForgetting'.
     */
    public void setExponentialForgetting(double exponentialForgetting) {
        this.exponentialForgetting = exponentialForgetting;
    }
}
