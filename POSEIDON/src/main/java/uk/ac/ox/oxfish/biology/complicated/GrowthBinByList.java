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

package uk.ac.ox.oxfish.biology.complicated;

import javax.annotation.Nullable;

/**
 * computes nothing, is given a list of weights and lengths and just spit them out.
 * It assumes that each bin is one year apart unless a lengthAtAge array is provided
 */
public class GrowthBinByList implements Meristics {


    private final double[] lenghts;

    private final double[] weights;

    private final int subdivisions;

    /**
     * If this is not provided we assume that each bin represents 1 year
     */
    @Nullable
    private final double[] lengthAtAge;


    public GrowthBinByList(int subdivisions, double[] lenghts, double[] weights) {

        this(subdivisions, lenghts, weights, null);
    }


    /**
     *  @param subdivisions
     * @param lenghts
     * @param weights
     * @param lengthAtAge an array with [age]---> length at all subdivisions <br> NULL if each bin described in lengths is also one year apart
     */
    public GrowthBinByList(
            int subdivisions, double[] lenghts, double[] weights,
            @Nullable
                    double[] lengthAtAge) {
        this.lenghts = lenghts;
        this.weights = weights;
        this.subdivisions = subdivisions;
        this.lengthAtAge = lengthAtAge;
    }

    /**
     * you can pick as many subdivisions as you want, they will all just spit out the same weight and length
     */

    @Override
    public double getLength(int subdivision, int bin) {
        return lenghts[bin];
    }

    @Override
    public double getWeight(int subdivision, int bin) {

        return weights[bin];
    }

    /**
     * subdivision are groups like male-female or age cohorts
     *
     * @return
     */
    @Override
    public int getNumberOfSubdivisions() {

        return subdivisions;
    }

    /**
     * number of bins for each subdivision. All subdivisions are assumed to have these number of bins
     * and all bins with the same index refer to the same weight and length; <br>
     * Bins can be length-bins or age-bins, it depends on the use case
     *
     * @return
     */
    @Override
    public int getNumberOfBins() {
        return lenghts.length;
    }

    /**
     * if no lengthAtBin was provided then bins represent age here and this is just a lookup; we always round down the age
     *
     * Otherwise just lookup the table provided! (if asked for an age that doesn't exit, return max age instead)
     * @param ageInYears  age in terms of years
     * @param subdivision the subdivision we are study (male/female is different for example)
     * @return the length of the fish
     */
    @Override
    public double getLengthAtAge(int ageInYears, int subdivision) {

        if(lengthAtAge == null)
            return getLength(subdivision,ageInYears <= (lenghts.length-1) ? (int)ageInYears : lenghts.length-1 );
        else
            return lengthAtAge[ageInYears <= (lengthAtAge.length-1) ? (int)ageInYears : lengthAtAge.length-1 ];
    }
}
