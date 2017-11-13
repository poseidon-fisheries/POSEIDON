
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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * The fixed boxcar method is not really kosher but it's very simple and
 * I think can be useful as an approximation. Basically keep track of only
 * length bins and ages fish from one bin to the next depending on a fixed graduation proportion
 */
public class FixedBoxcar {


    /**
     * population, split into length bins
     */
    private final  float[] currentDistribution;

    /**
     * proportion graduating each time step
     */
    private final float[] proportionGraduating;

    /**
     * what is the MIN length associated with each bin
     */
    private final float[] lengthPerBin;

    private FixedBoxcar(float[] currentDistribution,
                        float[] proportionGraduating,
                        float[] lengthPerBin) {
        this.currentDistribution = currentDistribution;
        this.proportionGraduating = proportionGraduating;
        this.lengthPerBin = lengthPerBin;
    }

    /**
     * Von Bertalanfy growth; equally spaced (by length) bins
     * @param LZero minimum length
     * @param LInfinity maximum length
     * @param K YEARLY growth as defined by Von Bertalanffy
     * @param daysPerStep how many days pass between each graduation
     * @return a Fixed Boxcar tuned to these parameters
     */
    public static FixedBoxcar buildEquallySpacedVB(
            float LZero,
            float LInfinity,
            float K,
            int daysPerStep,
            int numberOfBins
    )
    {
        Preconditions.checkArgument(numberOfBins > 1);
        Preconditions.checkArgument(LZero > 0);
        Preconditions.checkArgument(LInfinity > LZero);
        Preconditions.checkArgument(K > 0);
        Preconditions.checkArgument(daysPerStep > 0);
        //set the length per bin (they should be all equal)
        float increment = (LInfinity - LZero)/numberOfBins;
        float[] lengths = new float[numberOfBins];
        lengths[0] = LZero;
        for(int i=1;i<lengths.length; i++)
            lengths[i] = lengths[i-1] + increment;


        /**
         * set graduating proportion
         */
        return buildBoxcarFromLengthBins(LInfinity, K, daysPerStep, numberOfBins, lengths);

    }

    /**
     * Von Bertalanffy growth, Bins are of different length (smaller increments at first)
     * @param LZero minimum length
     * @param LInfinity maximum length
     * @param K YEARLY growth as defined by Von Bertalanffy
     * @param daysPerStep how many days pass between each graduation
     * @param factor1
     *@param cohortCoefficientVariation @return a Fixed Boxcar tuned to these parameters
     */
    public static FixedBoxcar buildVariableWidthVB(
            float LZero,
            float LInfinity,
            float K,
            int daysPerStep,
            int numberOfBins,
            float factor1,
            float cohortCoefficientVariation)
    {
        Preconditions.checkArgument(numberOfBins > 1);
        Preconditions.checkArgument(LZero > 0);
        Preconditions.checkArgument(LInfinity > LZero);
        Preconditions.checkArgument(K > 0);
        Preconditions.checkArgument(daysPerStep > 0);
        //set the length per bin following's Peter formula
        float[] increments = new float[numberOfBins];
        for(int i=0;i<increments.length; i++)
            increments[i] = factor1 * 2 * LZero * cohortCoefficientVariation * cohortCoefficientVariation *
                    (float) Math.pow(1 + factor1 * 2 * cohortCoefficientVariation * cohortCoefficientVariation,i);

        float[] lengths = new float[numberOfBins];
        lengths[0] = increments[0];
        for(int i=1; i<increments.length; i++ )
            lengths[i] = lengths[i-1] + increments[i];

        /**
         * set graduating proportion
         */
        return buildBoxcarFromLengthBins(LInfinity, K, daysPerStep, numberOfBins, lengths);

    }

    @NotNull
    private static FixedBoxcar buildBoxcarFromLengthBins(
            float LInfinity, float K, int daysPerStep, int numberOfBins, float[] lenghts) {
        float deltaT = daysPerStep/365; //scaling factor to turn yearly VB to daily
        //this is just the derivative of VB per time
        float[] growthPerBin = new float[numberOfBins];
        for(int i = 0; i<numberOfBins; i++)
            growthPerBin[i] = K * (LInfinity - lenghts[i]) * deltaT;
        //turn this into graduating proportion
        //which is basically what % of length distance has been covered within deltaT (by growthPerBin)
        float[] proportionGraduating = new float[numberOfBins];
        for(int i = 0; i<numberOfBins-1; i++)
            proportionGraduating[i] = growthPerBin[i]/(lenghts[i+1]-lenghts[i]);
        proportionGraduating[numberOfBins-1] = Float.NaN;


        return new FixedBoxcar(
                new float[numberOfBins],
                proportionGraduating,
                lenghts
        );
    }

    /**
     * very simple helper: given an array of currentDistribution,
     * graduates a proportion of each to the next bin;
     * CurrentDistribution is changed as a **side effect**
     *
     * @param currentDistribution
     * @param proportionGraduating
     * @return number of graduates (at position i is the number that left bin i and went into bin i+1)
     */
    public static float[] stepInTime(
            float[] currentDistribution,
            float[] proportionGraduating
    )
    {
        int bins = currentDistribution.length;
        float[] graduate = new float[bins];
        Preconditions.checkArgument(bins ==
        proportionGraduating.length);
        Preconditions.checkArgument(bins >2);
        assert proportionGraduating[bins-1]==0 || Float.isNaN(proportionGraduating[bins-1]);
        //going backward
        for(int i=bins-2; i>=0; i--)
        {
            assert currentDistribution[i] >=0;
            // find graduates
            graduate[i] = currentDistribution[i]*proportionGraduating[i];
            currentDistribution[i+1] += graduate[i];
            currentDistribution[i] -= graduate[i];
            assert currentDistribution[i] >=0;

        }
        return graduate;

    }


    /**
     * just the VB growth function
     * @param time time (which is usually years, but can be whatever depending on how K is scaled)
     * @param K the growth parameter
     * @param LInfinite the maximum average length for the oldest fish
     * @param LZero length at recruitment
     * @return length at specified time
     */
    public static double VonBertalanffyLength(
            double time,
            double K,
            double LInfinite,
            double LZero
    ){
        return (LZero + ((LInfinite - LZero) * (1 - Math.exp(-K * time))));
    }




}
