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

public class RandomSmoothedFactory implements AlgorithmFactory<RandomSmoothedAllocator> {


    private DoubleParameter absoluteMaximum = new FixedDoubleParameter(5000d);

    private DoubleParameter absoluteMinimum = new FixedDoubleParameter(1d);

    private DoubleParameter smoothingRuns = new FixedDoubleParameter(10000);


    private DoubleParameter aggressivness = new FixedDoubleParameter(0.4);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RandomSmoothedAllocator apply(final FishState state) {
        return new RandomSmoothedAllocator(
            absoluteMaximum.applyAsDouble(state.getRandom()),
            absoluteMinimum.applyAsDouble(state.getRandom()),
            (int) smoothingRuns.applyAsDouble(state.getRandom()),
            aggressivness.applyAsDouble(state.getRandom())
        );
    }

    /**
     * Getter for property 'absoluteMaximum'.
     *
     * @return Value for property 'absoluteMaximum'.
     */
    public DoubleParameter getAbsoluteMaximum() {
        return absoluteMaximum;
    }

    /**
     * Setter for property 'absoluteMaximum'.
     *
     * @param absoluteMaximum Value to set for property 'absoluteMaximum'.
     */
    public void setAbsoluteMaximum(final DoubleParameter absoluteMaximum) {
        this.absoluteMaximum = absoluteMaximum;
    }

    /**
     * Getter for property 'absoluteMinimum'.
     *
     * @return Value for property 'absoluteMinimum'.
     */
    public DoubleParameter getAbsoluteMinimum() {
        return absoluteMinimum;
    }

    /**
     * Setter for property 'absoluteMinimum'.
     *
     * @param absoluteMinimum Value to set for property 'absoluteMinimum'.
     */
    public void setAbsoluteMinimum(final DoubleParameter absoluteMinimum) {
        this.absoluteMinimum = absoluteMinimum;
    }

    /**
     * Getter for property 'smoothingRuns'.
     *
     * @return Value for property 'smoothingRuns'.
     */
    public DoubleParameter getSmoothingRuns() {
        return smoothingRuns;
    }

    /**
     * Setter for property 'smoothingRuns'.
     *
     * @param smoothingRuns Value to set for property 'smoothingRuns'.
     */
    public void setSmoothingRuns(final DoubleParameter smoothingRuns) {
        this.smoothingRuns = smoothingRuns;
    }

    /**
     * Getter for property 'aggressivness'.
     *
     * @return Value for property 'aggressivness'.
     */
    public DoubleParameter getAggressivness() {
        return aggressivness;
    }

    /**
     * Setter for property 'aggressivness'.
     *
     * @param aggressivness Value to set for property 'aggressivness'.
     */
    public void setAggressivness(final DoubleParameter aggressivness) {
        this.aggressivness = aggressivness;
    }
}
