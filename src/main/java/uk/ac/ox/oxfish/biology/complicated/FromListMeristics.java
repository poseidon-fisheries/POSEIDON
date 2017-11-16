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
import com.google.common.collect.ImmutableList;

/**
 * A plug while waiting for a proper meristic reform. This simply c
 * Created by carrknight on 7/5/17.
 */
public class FromListMeristics implements Meristics {


    private final GrowthBinByList growth;




    private final  ImmutableList<Double> maturities;


    public FromListMeristics(
            Double[] maturities,
            double[] weights) {
        this(maturities, weights, new double[weights.length]);
    }

    public FromListMeristics(
            Double[] maturities,
            double[] weights,
            double[] lenghts) {
        Preconditions.checkArgument(maturities.length == weights.length, "length mismatch between maturities and weights");
        Preconditions.checkArgument(lenghts.length == weights.length, "length mismatch between lenghts and weights");

        this.maturities = ImmutableList.copyOf(maturities);

        this.growth = new GrowthBinByList(2,
                                          lenghts,
                                          weights);
    }

    @Override
    public int getMaxAge() {
        return growth.getNumberOfBins()-1;
    }






    @Override
    public ImmutableList<Double> getMaturity() {
        return maturities;
    }

    @Override
    public ImmutableList<Double> getRelativeFecundity() {

        return null;
    }

    @Override
    public double getCumulativePhi() {

       return Double.NaN;
    }

    @Override
    public boolean isAddRelativeFecundityToSpawningBiomass() {
        return false;
    }


    @Override
    public int getVirginRecruits() {
        return  - 1;
    }

    @Override
    public double getSteepness() {
        return Double.NaN;
    }

    /**
     * you can pick as many subdivisions as you want, they will all just spit out the same weight and length
     * @param subdivision
     * @param bin
     */
    @Override
    public double getLength(int subdivision, int bin) {
        return growth.getLength(subdivision, bin);
    }

    @Override
    public double getWeight(int subdivision, int bin) {
        return growth.getWeight(subdivision, bin);
    }

    /**
     * subdivision are groups like male-female or age cohorts
     *
     * @return
     */
    @Override
    public int getNumberOfSubdivisions() {
        return growth.getNumberOfSubdivisions();
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
        return growth.getNumberOfBins();
    }
}
