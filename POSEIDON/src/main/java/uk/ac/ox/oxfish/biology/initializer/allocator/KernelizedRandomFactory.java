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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class KernelizedRandomFactory implements AlgorithmFactory<KernelizedRandomAllocator> {


    private DoubleParameter minimum = new FixedDoubleParameter(0);

    private DoubleParameter maximum = new FixedDoubleParameter(1);

    private DoubleParameter fixedPoints = new FixedDoubleParameter(10);

    private DoubleParameter bandwidth = new FixedDoubleParameter(5);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public KernelizedRandomAllocator apply(FishState fishState) {
        return new KernelizedRandomAllocator(
                maximum.apply(fishState.getRandom()),
                minimum.apply(fishState.getRandom()),
                bandwidth.apply(fishState.getRandom()),
                fixedPoints.apply(fishState.getRandom()).intValue()
        );
    }

    /**
     * Getter for property 'minimum'.
     *
     * @return Value for property 'minimum'.
     */
    public DoubleParameter getMinimum() {
        return minimum;
    }

    /**
     * Setter for property 'minimum'.
     *
     * @param minimum Value to set for property 'minimum'.
     */
    public void setMinimum(DoubleParameter minimum) {
        this.minimum = minimum;
    }

    /**
     * Getter for property 'maximum'.
     *
     * @return Value for property 'maximum'.
     */
    public DoubleParameter getMaximum() {
        return maximum;
    }

    /**
     * Setter for property 'maximum'.
     *
     * @param maximum Value to set for property 'maximum'.
     */
    public void setMaximum(DoubleParameter maximum) {
        this.maximum = maximum;
    }

    /**
     * Getter for property 'fixedPoints'.
     *
     * @return Value for property 'fixedPoints'.
     */
    public DoubleParameter getFixedPoints() {
        return fixedPoints;
    }

    /**
     * Setter for property 'fixedPoints'.
     *
     * @param fixedPoints Value to set for property 'fixedPoints'.
     */
    public void setFixedPoints(DoubleParameter fixedPoints) {
        this.fixedPoints = fixedPoints;
    }

    /**
     * Getter for property 'bandwidth'.
     *
     * @return Value for property 'bandwidth'.
     */
    public DoubleParameter getBandwidth() {
        return bandwidth;
    }

    /**
     * Setter for property 'bandwidth'.
     *
     * @param bandwidth Value to set for property 'bandwidth'.
     */
    public void setBandwidth(DoubleParameter bandwidth) {
        this.bandwidth = bandwidth;
    }
}
