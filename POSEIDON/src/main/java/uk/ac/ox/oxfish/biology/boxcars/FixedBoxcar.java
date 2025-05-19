
/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.boxcars;

import com.google.common.base.Preconditions;

/**
 * The fixed boxcar method is not really kosher but it's very simple and
 * I think can be useful as an approximation. Basically keep track of only
 * length bins and ages fish from one bin to the next depending on a fixed graduation proportion
 */
public class FixedBoxcar {


    /**
     * population, split into length bins
     */
    private final double[] currentDistribution;

    /**
     * proportion graduating each time step
     */
    private final double[] proportionGraduating;

    /**
     * what is the MIN length associated with each bin
     */
    private final double[] lengthPerBin;

    private FixedBoxcar(
        final double[] currentDistribution,
        final double[] proportionGraduating,
        final double[] lengthPerBin
    ) {
        this.currentDistribution = currentDistribution;
        this.proportionGraduating = proportionGraduating;
        this.lengthPerBin = lengthPerBin;
    }

    /**
     * Von Bertalanfy growth; equally spaced (by length) bins
     *
     * @param LZero       minimum length
     * @param LInfinity   maximum length
     * @param K           YEARLY growth as defined by Von Bertalanffy
     * @param daysPerStep how many days pass between each graduation
     * @return a Fixed Boxcar tuned to these parameters
     */
    public static FixedBoxcar buildEquallySpacedVB(
        final double LZero,
        final double LInfinity,
        final double K,
        final int daysPerStep,
        final int numberOfBins
    ) {
        Preconditions.checkArgument(numberOfBins > 1);
        Preconditions.checkArgument(LZero > 0);
        Preconditions.checkArgument(LInfinity > LZero);
        Preconditions.checkArgument(K > 0);
        Preconditions.checkArgument(daysPerStep > 0);
        //set the length per bin (they should be all equal)
        final double increment = (LInfinity - LZero) / (numberOfBins - 1);
        final double[] lengths = new double[numberOfBins];
        lengths[0] = LZero;
        for (int i = 1; i < lengths.length; i++)
            lengths[i] = lengths[i - 1] + increment;


        /**
         * set graduating proportion
         */
        return buildBoxcarFromLengthBins(LInfinity, K, daysPerStep, numberOfBins, lengths);

    }

    private static FixedBoxcar buildBoxcarFromLengthBins(
        final double LInfinity, final double K, final int daysPerStep, final int numberOfBins, final double[] lenghts
    ) {
        final double deltaT = daysPerStep / 365f; //scaling factor to turn yearly VB to daily
        //this is just the derivative of VB per time
        final double[] growthPerBin = new double[numberOfBins];
        for (int i = 0; i < numberOfBins; i++)
            growthPerBin[i] = K * (LInfinity - lenghts[i]) * deltaT;
        //turn this into graduating proportion
        //which is basically what % of length distance has been covered within deltaT (by growthPerBin)
        final double[] proportionGraduating = new double[numberOfBins];
        for (int i = 0; i < numberOfBins - 1; i++)
            proportionGraduating[i] = Math.max(growthPerBin[i] / (lenghts[i + 1] - lenghts[i]), 0);
        proportionGraduating[numberOfBins - 1] = Double.NaN;


        return new FixedBoxcar(
            new double[numberOfBins],
            proportionGraduating,
            lenghts
        );
    }

    /**
     * Von Bertalanffy growth, Bins are of different length (smaller increments at first)
     *
     * @param LZero                      minimum length
     * @param LInfinity                  maximum length
     * @param K                          YEARLY growth as defined by Von Bertalanffy
     * @param daysPerStep                how many days pass between each graduation
     * @param factor1
     * @param cohortCoefficientVariation @return a Fixed Boxcar tuned to these parameters
     */
    public static FixedBoxcar buildVariableWidthVB(
        final double LZero,
        final double LInfinity,
        final double K,
        final int daysPerStep,
        final int numberOfBins,
        final double factor1,
        final double cohortCoefficientVariation
    ) {
        Preconditions.checkArgument(numberOfBins > 1);
        Preconditions.checkArgument(LZero > 0);
        Preconditions.checkArgument(LInfinity > LZero);
        Preconditions.checkArgument(K > 0);
        Preconditions.checkArgument(daysPerStep > 0);
        //set the length per bin following's Peter formula
        final double[] increments = new double[numberOfBins];
        for (int i = 0; i < increments.length; i++)
            increments[i] = factor1 * 2 * LZero * cohortCoefficientVariation * cohortCoefficientVariation *
                Math.pow(1 + factor1 * 2 * cohortCoefficientVariation * cohortCoefficientVariation, i);

        final double[] lengths = new double[numberOfBins];
        lengths[0] = increments[0];
        for (int i = 1; i < increments.length; i++)
            lengths[i] = lengths[i - 1] + increments[i];

        /**
         * set graduating proportion
         */
        return buildBoxcarFromLengthBins(LInfinity, K, daysPerStep, numberOfBins, lengths);

    }

    /**
     * just the VB growth function
     *
     * @param time      time (which is usually years, but can be whatever depending on how K is scaled)
     * @param K         the growth parameter
     * @param LInfinite the maximum average length for the oldest fish
     * @param LZero     length at recruitment
     * @return length at specified time
     */
    public static double VonBertalanffyLength(
        final double time,
        final double K,
        final double LInfinite,
        final double LZero
    ) {
        return (LZero + ((LInfinite - LZero) * (1 - Math.exp(-K * time))));
    }


    /**
     * the current population, split in bins; the link is *live* so any change ruin everything
     */
    public double[] getCurrentDistribution() {
        return currentDistribution;
    }

    /**
     * the proportion of fish moving from one bin to the next in the given time step; this is no copy, so careful about modifying it
     */
    public double[] getProportionGraduating() {
        return proportionGraduating;
    }

    /**
     * the length associated with each bin
     */
    public double[] getLengthPerBin() {
        return lengthPerBin;
    }


}
