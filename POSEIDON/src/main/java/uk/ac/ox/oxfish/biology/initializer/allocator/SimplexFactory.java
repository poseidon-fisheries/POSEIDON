/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NullParameter;

public class SimplexFactory implements AlgorithmFactory<SimplexAllocator> {

    private DoubleParameter minimum = new FixedDoubleParameter(0);

    private DoubleParameter maximum = new FixedDoubleParameter(1);


    private DoubleParameter bandwidth = new FixedDoubleParameter(5);

    private DoubleParameter randomSeed = new NullParameter();


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public SimplexAllocator apply(final FishState fishState) {
        final double drawnRandomSeed = randomSeed.applyAsDouble(fishState.getRandom());
        final long randomSeed;
        //it might not have been set up, if so just pick at random!
        if (!Double.isFinite(drawnRandomSeed) || Double.isNaN(drawnRandomSeed))
            randomSeed = fishState.getRandom().nextLong();
        else
            randomSeed = (long) drawnRandomSeed;
        System.out.println("simplex random seed: " + randomSeed);
        return new SimplexAllocator(
            maximum.applyAsDouble(fishState.getRandom()),
            minimum.applyAsDouble(fishState.getRandom()),
            bandwidth.applyAsDouble(fishState.getRandom()),
            randomSeed
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
    public void setMinimum(final DoubleParameter minimum) {
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
    public void setMaximum(final DoubleParameter maximum) {
        this.maximum = maximum;
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
    public void setBandwidth(final DoubleParameter bandwidth) {
        this.bandwidth = bandwidth;
    }


    public DoubleParameter getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(final DoubleParameter randomSeed) {
        this.randomSeed = randomSeed;
    }
}
