/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.boxcars;


import com.google.common.base.Preconditions;
import org.apache.commons.math3.distribution.GammaDistribution;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * gamma distributed VB growth matrix
 */
public class SullivanTransitionProbability {


    //cannot go bigger than this times lInfinity
    private final static double L_MAX_TO_LINFINITY = 1.2;
    /**
     * what they call "beta" in the paper (incorrectly, since beta is the name not of the scale but the rate!)
     */
    private final double gammaScaleParameter;
    private final double lInfinity;
    private final double vbkGrowthParameter;
    private final int numberOfBins;
    /**
     * transition matrix; [departingBin][arrivingBin]
     */
    private final double[][] transitionMatrix;

    /**
     * basically scales increments; if it's daily transition then this will be 1/365d
     */
    private final double scaling;


    public SullivanTransitionProbability(
        double gammaScaleParameter,
        double lInfinity,
        double vbkGrowthParameter,
        int numberOfBins,
        int binLengthInCm
    ) {

        this(gammaScaleParameter, lInfinity, vbkGrowthParameter, numberOfBins, binLengthInCm, 1.0);
    }

    public SullivanTransitionProbability(
        double gammaScaleParameter,
        double lInfinity,
        double vbkGrowthParameter,
        int numberOfBins,
        int binLengthInCm,
        double scaling
    ) {
        this.gammaScaleParameter = gammaScaleParameter;
        this.lInfinity = lInfinity;
        this.vbkGrowthParameter = vbkGrowthParameter;
        this.numberOfBins = numberOfBins;
        this.scaling = scaling;

        transitionMatrix = new double[numberOfBins][numberOfBins];
        gammaTransitionMatrix(binLengthInCm);
    }

    private void gammaTransitionMatrix(double binLengthInCm) {

        //compute upper, lower and mid bins
        double[] midLengths = new double[numberOfBins];


        for (int i = 0; i < midLengths.length; i++) {

            midLengths[i] = i * binLengthInCm + (binLengthInCm) / 2.0;


        }


        gammaTransitionMatrix(midLengths);

    }

    //build it given midlengths (will assume lower and upper lengths are in between the midlengths)
    private void gammaTransitionMatrix(double[] midLengths) {

        //compute upper, lower and mid bins
        double[] lowerLengths = new double[numberOfBins];
        double[] upperLengths = new double[numberOfBins];
        //also compute the average growth and the corresponding alpha
        double[] deltaL = new double[numberOfBins]; //this is the vbk growth from midlength
        double[] alphaL = new double[numberOfBins]; //this is shape of growth when made stochastic

        for (int i = 0; i < midLengths.length; i++) {
            lowerLengths[i] = i == 0 ? 0 : (midLengths[i - 1] + midLengths[i]) / 2.0;
            upperLengths[i] = i == midLengths.length - 1 ?
                midLengths[i] + (midLengths[i] - midLengths[i]) / 2.0 :
                (midLengths[i + 1] + midLengths[i]) / 2.0;


            deltaL[i] = (lInfinity - midLengths[i]) * (1 - Math.exp(-vbkGrowthParameter));
            deltaL[i] = deltaL[i] * scaling;
            alphaL[i] = (midLengths[i] + deltaL[i]) / gammaScaleParameter;


        }


        gammaTransitionMatrix(lowerLengths, upperLengths, alphaL);

    }

    private void gammaTransitionMatrix(double[] lowerLengths, double[] upperLengths, double[] alphaL) {
        for (int departure = 0; departure < numberOfBins; departure++) {
            GammaDistribution fromBin = new GammaDistribution(alphaL[departure], gammaScaleParameter);
            for (int arrival = 0; arrival < numberOfBins; arrival++) {

                if (departure > arrival)
                    transitionMatrix[departure][arrival] = 0.0; //can't grow down
                else if (departure == arrival)
                    transitionMatrix[departure][arrival] =
                        arrival == numberOfBins - 1 ? 1.0 :
                            fromBin.cumulativeProbability(
                                upperLengths[arrival]); //gamma up to lowest bin should be counted
                    //don't let the fish grow a lot more than L_INF
                else if (upperLengths[arrival] >= L_MAX_TO_LINFINITY * lInfinity)
                    transitionMatrix[departure][arrival] = 0;
                else if (departure < arrival & arrival == numberOfBins - 1) //if you are at the edge, upper
                    // probability is always 1
                    transitionMatrix[departure][arrival] = 1 - fromBin.cumulativeProbability(lowerLengths[arrival]);
                else if (departure < arrival)
                    transitionMatrix[departure][arrival] =
                        fromBin.cumulativeProbability(upperLengths[arrival]) - fromBin.cumulativeProbability(
                            lowerLengths[arrival]);


                //don't bother with probabilities below 0.00001
                transitionMatrix[departure][arrival] = FishStateUtilities.round5(transitionMatrix[departure][arrival]);

                Preconditions.checkState(transitionMatrix[departure][arrival] >= 0);

            }

            //small discrepancies still account for a lot so normalize forcefully
            normalizeToOne(transitionMatrix[departure]);


        }
    }

    private void normalizeToOne(double[] array) {
        double sum = sumArray(array);
        for (int i = 0; i < array.length; i++) {
            array[i] /= sum;
        }
    }

    private double sumArray(double[] array) {
        double sum = 0;
        for (double element : array) {
            sum += element;
        }
        return sum;
    }

    public SullivanTransitionProbability(
        double gammaScaleParameter,
        double lInfinity,
        double vbkGrowthParameter,
        double scaling,
        int subdivision, //transition matrix for one sex of the species
        Species species
    ) {
        this.gammaScaleParameter = gammaScaleParameter;
        this.lInfinity = lInfinity;
        this.vbkGrowthParameter = vbkGrowthParameter;

        this.scaling = scaling;
        this.numberOfBins = species.getNumberOfBins();

        transitionMatrix = new double[species.getNumberOfBins()][species.getNumberOfBins()];

        double[] midLengths = new double[species.getNumberOfBins()];
        for (int i = 0; i < midLengths.length; i++)
            midLengths[i] = species.getLength(subdivision, i);

        gammaTransitionMatrix(midLengths);
    }

    /**
     * Getter for property 'gammaScaleParameter'.
     *
     * @return Value for property 'gammaScaleParameter'.
     */
    public double getGammaScaleParameter() {
        return gammaScaleParameter;
    }

    /**
     * Getter for property 'lInfinity'.
     *
     * @return Value for property 'lInfinity'.
     */
    public double getlInfinity() {
        return lInfinity;
    }

    /**
     * Getter for property 'vbkGrowthParameter'.
     *
     * @return Value for property 'vbkGrowthParameter'.
     */
    public double getVbkGrowthParameter() {
        return vbkGrowthParameter;
    }

    /**
     * Getter for property 'numberOfBins'.
     *
     * @return Value for property 'numberOfBins'.
     */
    public int getNumberOfBins() {
        return numberOfBins;
    }


    /**
     * Getter for property 'transitionMatrix'.
     *
     * @return Value for property 'transitionMatrix'.
     */
    public double[][] getTransitionMatrix() {
        return transitionMatrix;
    }
}
