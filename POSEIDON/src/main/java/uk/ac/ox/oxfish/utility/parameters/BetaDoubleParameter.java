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

package uk.ac.ox.oxfish.utility.parameters;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.distribution.BetaDistribution;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

/**
 * Returns a beta distributed double value randomly
 * <p>
 * Created by Brian Powers 5/1/2016
 */
public class BetaDoubleParameter implements DoubleParameter {


    private double alpha;

    private double beta;

    private BetaDistribution betaDist;

    public BetaDoubleParameter() {
    }

    public BetaDoubleParameter(double alpha, double beta) {
        Preconditions.checkArgument(alpha > 0);
        Preconditions.checkArgument(beta > 0);
        this.alpha = alpha;
        this.beta = beta;
        betaDist = new BetaDistribution(alpha, beta);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public double applyAsDouble(MersenneTwisterFast mersenneTwisterFast) {
        return betaDist.inverseCumulativeProbability(mersenneTwisterFast.nextDouble());
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    @Override
    public DoubleParameter makeCopy() {
        return new BetaDoubleParameter(alpha, beta);
    }
}
