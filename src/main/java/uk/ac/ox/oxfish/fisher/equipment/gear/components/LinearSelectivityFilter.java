/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import uk.ac.ox.oxfish.biology.Species;

/**
 * this is basically just the formulas used for SPR instantaneous mortality in the SPR code from TNC
 */
public class LinearSelectivityFilter extends FormulaAbundanceFilter {


    private final double minLengthCaughtInCm;

    private final double mostFrequentLengthCaughtInCm;

    private final double vonBertalanfyKParameter;

    private final double meanLengthCaughtAboveThresholdInCm;

    private final double lengthInfinityInCm;


    public LinearSelectivityFilter(double minLengthCaughtInCm, double mostFrequentLengthCaughtInCm,
            double vonBertalanfyKParameter, double meanLengthCaughtAboveThresholdInCm, double lengthInfinityInCm) {
        super(true, false);
        this.minLengthCaughtInCm = minLengthCaughtInCm;
        this.mostFrequentLengthCaughtInCm = mostFrequentLengthCaughtInCm;
        this.vonBertalanfyKParameter = vonBertalanfyKParameter;
        this.meanLengthCaughtAboveThresholdInCm = meanLengthCaughtAboveThresholdInCm;
        this.lengthInfinityInCm = lengthInfinityInCm;
    }


    /**
     * the method that gives the probability matrix for each age class and each sex of not filtering the abundance away
     *
     * @param species
     * @return
     */
    @Override
    protected double[][] computeSelectivity(Species species) {

        double mortality[][] = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

        //get instantaneousMortality
        double instantaneousMortality = (vonBertalanfyKParameter * (lengthInfinityInCm - meanLengthCaughtAboveThresholdInCm)) /
                (meanLengthCaughtAboveThresholdInCm - mostFrequentLengthCaughtInCm);

        //remove from it natural mortality
        instantaneousMortality -= Math.pow(10,0.566-(0.718*Math.log10(lengthInfinityInCm))+0.02*20);

        for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
            for (int subdivision = 0; subdivision < species.getNumberOfSubdivisions(); subdivision++) {

                double length = species.getLength(subdivision, bin);
                mortality[subdivision][bin] = length <= minLengthCaughtInCm ?
                        0 :
                        Math.min(1, (length - minLengthCaughtInCm) /
                                (mostFrequentLengthCaughtInCm - minLengthCaughtInCm)) * instantaneousMortality;
            }

        }
        return mortality;
    }
    /**
     * Getter for property 'minLengthCaughtInCm'.
     *
     * @return Value for property 'minLengthCaughtInCm'.
     */
    public double getMinLengthCaughtInCm() {
        return minLengthCaughtInCm;
    }

    /**
     * Getter for property 'mostFrequentLengthCaughtInCm'.
     *
     * @return Value for property 'mostFrequentLengthCaughtInCm'.
     */
    public double getMostFrequentLengthCaughtInCm() {
        return mostFrequentLengthCaughtInCm;
    }

    /**
     * Getter for property 'vonBertalanfyKParameter'.
     *
     * @return Value for property 'vonBertalanfyKParameter'.
     */
    public double getVonBertalanfyKParameter() {
        return vonBertalanfyKParameter;
    }

    /**
     * Getter for property 'meanLengthCaughtAboveThresholdInCm'.
     *
     * @return Value for property 'meanLengthCaughtAboveThresholdInCm'.
     */
    public double getMeanLengthCaughtAboveThresholdInCm() {
        return meanLengthCaughtAboveThresholdInCm;
    }
}
