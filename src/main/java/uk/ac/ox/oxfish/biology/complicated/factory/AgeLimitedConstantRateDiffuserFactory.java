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

package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.AgeLimitedConstantRateDiffuser;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/8/17.
 */
public class AgeLimitedConstantRateDiffuserFactory implements AlgorithmFactory<AgeLimitedConstantRateDiffuser> {


    /**
     * % of differential that moves from here to there
     */
    private DoubleParameter diffusingRate = new FixedDoubleParameter(.001);
    /**
     * max distance in cells fish can move within a day
     */
    private DoubleParameter diffusingRange = new FixedDoubleParameter(1);


    private DoubleParameter smallestMovingBin = new FixedDoubleParameter(0);

    private DoubleParameter largestMovingBin = new FixedDoubleParameter(10000);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public AgeLimitedConstantRateDiffuser apply(FishState state) {
        return new AgeLimitedConstantRateDiffuser(
                diffusingRange.apply(state.getRandom()).intValue(),
                diffusingRate.apply(state.getRandom()),
                smallestMovingBin.apply(state.getRandom()).intValue(),
                largestMovingBin.apply(state.getRandom()).intValue()
        );
    }


    /**
     * Getter for property 'diffusingRate'.
     *
     * @return Value for property 'diffusingRate'.
     */
    public DoubleParameter getDiffusingRate() {
        return diffusingRate;
    }

    /**
     * Getter for property 'diffusingRange'.
     *
     * @return Value for property 'diffusingRange'.
     */
    public DoubleParameter getDiffusingRange() {
        return diffusingRange;
    }

    /**
     * Getter for property 'smallestMovingBin'.
     *
     * @return Value for property 'smallestMovingBin'.
     */
    public DoubleParameter getSmallestMovingBin() {
        return smallestMovingBin;
    }

    /**
     * Getter for property 'largestMovingBin'.
     *
     * @return Value for property 'largestMovingBin'.
     */
    public DoubleParameter getLargestMovingBin() {
        return largestMovingBin;
    }

    /**
     * Setter for property 'diffusingRate'.
     *
     * @param diffusingRate Value to set for property 'diffusingRate'.
     */
    public void setDiffusingRate(DoubleParameter diffusingRate) {
        this.diffusingRate = diffusingRate;
    }

    /**
     * Setter for property 'diffusingRange'.
     *
     * @param diffusingRange Value to set for property 'diffusingRange'.
     */
    public void setDiffusingRange(DoubleParameter diffusingRange) {
        this.diffusingRange = diffusingRange;
    }

    /**
     * Setter for property 'smallestMovingBin'.
     *
     * @param smallestMovingBin Value to set for property 'smallestMovingBin'.
     */
    public void setSmallestMovingBin(DoubleParameter smallestMovingBin) {
        this.smallestMovingBin = smallestMovingBin;
    }

    /**
     * Setter for property 'largestMovingBin'.
     *
     * @param largestMovingBin Value to set for property 'largestMovingBin'.
     */
    public void setLargestMovingBin(DoubleParameter largestMovingBin) {
        this.largestMovingBin = largestMovingBin;
    }
}
