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

package uk.ac.ox.oxfish.biology.boxcars;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.complicated.GrowthBinByList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class EquallySpacedBertalanffyFactory implements AlgorithmFactory<GrowthBinByList> {


    public static final int MAXIMUM_AGE_TRACKED = 100;
    /**
     * the allometric alpha converting length length cm to weight grams
     */
    private DoubleParameter allometricAlpha =
        new FixedDoubleParameter(0.015);

    /**
     * the allometric beta converting length length cm to weight grams
     */
    private DoubleParameter allometricBeta =
        new FixedDoubleParameter(2.961);

    /**
     * the L_zero of the VB formula
     */
    private DoubleParameter recruitLengthInCm = new FixedDoubleParameter(10);


    private DoubleParameter maxLengthInCm = new FixedDoubleParameter(113);

    /**
     * the K parameter of VB
     */
    private DoubleParameter kYearlyParameter = new FixedDoubleParameter(0.364);

    /**
     * if this is null or NaN then the bins go from L0 to LInf; if this is not null then each bin has a fixed width
     */
    private Double cmPerBin = null;


    private int numberOfBins = 100;

    @NotNull

    public static double[] bertalanffyLengthAtAge(
        final double LInfinity, final double LZero,
        final double kParameter, final int maximumAgeTracked
    ) {
        final double[] lengthAtAge = new double[100];
        lengthAtAge[0] = LZero;
        for (int i = 1; i < maximumAgeTracked; i++) {
            lengthAtAge[i] = LInfinity + ((LZero - LInfinity)) *
                Math.exp(-kParameter * i);
        }
        return lengthAtAge;
    }

    public static double bertnalanffyLengthToWeight(final double alpha, final double beta, final double currentLength) {
        return alpha * Math.pow(currentLength, beta) / 1000d;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public GrowthBinByList apply(final FishState fishState) {

        final double[] lengths = new double[numberOfBins];
        final double[] weights = new double[numberOfBins];
        //equal spaced growth
        final double LInfinity = maxLengthInCm.applyAsDouble(fishState.getRandom());
        final double LZero = recruitLengthInCm.applyAsDouble(fishState.getRandom());
        double increment = (LInfinity - LZero) / (numberOfBins - 1);


        //allometric weight
        final double alpha = allometricAlpha.applyAsDouble(fishState.getRandom());
        final double beta = allometricBeta.applyAsDouble(fishState.getRandom());
        lengths[0] = LZero;

        //if you are using a fixed increment, time to set it now!
        if (cmPerBin != null && Double.isFinite(cmPerBin)) {
            increment = cmPerBin;
            lengths[0] = cmPerBin / 2;

        }

        weights[0] = bertnalanffyLengthToWeight(alpha, beta, lengths[0]);


        for (int i = 1; i < lengths.length; i++) {
            lengths[i] = lengths[i - 1] + increment;
            //the allometric function turns it into grams, we want kg!
            weights[i] = bertnalanffyLengthToWeight(alpha, beta, lengths[i]);

        }


        //sometimes we need to know what is the length at specific age, we can compute that here!
        final double k = kYearlyParameter.applyAsDouble(fishState.getRandom());
        final double[] lengthAtAge = bertalanffyLengthAtAge(LInfinity, LZero, k,
            MAXIMUM_AGE_TRACKED
        );


        return new GrowthBinByList(
            1,
            lengths,
            weights,
            lengthAtAge
        );


    }

    /**
     * Getter for property 'allometricAlpha'.
     *
     * @return Value for property 'allometricAlpha'.
     */
    public DoubleParameter getAllometricAlpha() {
        return allometricAlpha;
    }

    /**
     * Setter for property 'allometricAlpha'.
     *
     * @param allometricAlpha Value to set for property 'allometricAlpha'.
     */
    public void setAllometricAlpha(final DoubleParameter allometricAlpha) {
        this.allometricAlpha = allometricAlpha;
    }

    /**
     * Getter for property 'allometricBeta'.
     *
     * @return Value for property 'allometricBeta'.
     */
    public DoubleParameter getAllometricBeta() {
        return allometricBeta;
    }

    /**
     * Setter for property 'allometricBeta'.
     *
     * @param allometricBeta Value to set for property 'allometricBeta'.
     */
    public void setAllometricBeta(final DoubleParameter allometricBeta) {
        this.allometricBeta = allometricBeta;
    }

    /**
     * Getter for property 'recruitLengthInCm'.
     *
     * @return Value for property 'recruitLengthInCm'.
     */
    public DoubleParameter getRecruitLengthInCm() {
        return recruitLengthInCm;
    }

    /**
     * Setter for property 'recruitLengthInCm'.
     *
     * @param recruitLengthInCm Value to set for property 'recruitLengthInCm'.
     */
    public void setRecruitLengthInCm(final DoubleParameter recruitLengthInCm) {
        this.recruitLengthInCm = recruitLengthInCm;
    }

    /**
     * Getter for property 'maxLengthInCm'.
     *
     * @return Value for property 'maxLengthInCm'.
     */
    public DoubleParameter getMaxLengthInCm() {
        return maxLengthInCm;
    }

    /**
     * Setter for property 'maxLengthInCm'.
     *
     * @param maxLengthInCm Value to set for property 'maxLengthInCm'.
     */
    public void setMaxLengthInCm(final DoubleParameter maxLengthInCm) {
        this.maxLengthInCm = maxLengthInCm;
    }

    /**
     * Getter for property 'kYearlyParameter'.
     *
     * @return Value for property 'kYearlyParameter'.
     */
    public DoubleParameter getkYearlyParameter() {
        return kYearlyParameter;
    }

    /**
     * Setter for property 'kYearlyParameter'.
     *
     * @param kYearlyParameter Value to set for property 'kYearlyParameter'.
     */
    public void setkYearlyParameter(final DoubleParameter kYearlyParameter) {
        this.kYearlyParameter = kYearlyParameter;
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
     * Setter for property 'numberOfBins'.
     *
     * @param numberOfBins Value to set for property 'numberOfBins'.
     */
    public void setNumberOfBins(final int numberOfBins) {
        this.numberOfBins = numberOfBins;
    }

    /**
     * Getter for property 'cmPerBin'.
     *
     * @return Value for property 'cmPerBin'.
     */
    public Double getCmPerBin() {
        return cmPerBin;
    }

    /**
     * Setter for property 'cmPerBin'.
     *
     * @param cmPerBin Value to set for property 'cmPerBin'.
     */
    public void setCmPerBin(final Double cmPerBin) {
        this.cmPerBin = cmPerBin;
    }
}
