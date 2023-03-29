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

package uk.ac.ox.oxfish.utility.parameters;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;

/**
 * Returns a gaussian double value randomly
 *
 * Created by carrknight on 6/7/15.
 */
public class NormalDoubleParameter implements DoubleParameter {


    private double mean;

    private double standardDeviation;

    public NormalDoubleParameter() {
    }

    public NormalDoubleParameter(double mean, double standardDeviation) {
        Preconditions.checkArgument(standardDeviation >=0);
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public double applyAsDouble(MersenneTwisterFast mersenneTwisterFast) {
        return mersenneTwisterFast.nextGaussian()*standardDeviation + mean;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    @Override
    public DoubleParameter makeCopy() {
        return new NormalDoubleParameter(mean,standardDeviation);
    }
}
