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


    private final ImmutableList<Double> weights;

    private final ImmutableList<Double> maturities;


    private final double mortalityRate;



    public FromListMeristics(double mortalityRate,
                             Double[] maturities,
                             Double... weights) {
        Preconditions.checkArgument(maturities.length == weights.length, "length mismatch between maturities and weights");
        this.weights = ImmutableList.copyOf(weights);
        this.maturities = ImmutableList.copyOf(maturities);
        this.mortalityRate = mortalityRate;
    }

    @Override
    public int getMaxAge() {
        return weights.size()-1;
    }

    @Override
    public double getMortalityParameterMMale() {
        return mortalityRate;
    }

    @Override
    public double getMortalityParameterMFemale() {
        return mortalityRate;
    }

    @Override
    public ImmutableList<Double> getLengthMaleInCm() {
        return null;
    }

    @Override
    public ImmutableList<Double> getLengthFemaleInCm() {
        return null;
    }

    @Override
    public ImmutableList<Double> getWeightMaleInKg() {
        return weights;
    }

    @Override
    public ImmutableList<Double> getWeightFemaleInKg() {
        return weights;
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
}
