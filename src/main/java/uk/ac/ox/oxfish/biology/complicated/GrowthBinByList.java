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

import com.google.common.base.Preconditions;

/**
 * computes nothing, is given a list of weights and lengths and just spit them out
 */
public class GrowthBinByList implements GrowthBinParameters {


    private final double[] lenghts;

    private final double[] weights;

    private final int subdivisions;


    public GrowthBinByList(int subdivisions, double[] lenghts, double[] weights) {
        this.lenghts = lenghts;
        this.weights = weights;
        this.subdivisions = subdivisions;
        Preconditions.checkArgument(lenghts.length >0);
        Preconditions.checkArgument(lenghts.length  == weights.length);
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
}
