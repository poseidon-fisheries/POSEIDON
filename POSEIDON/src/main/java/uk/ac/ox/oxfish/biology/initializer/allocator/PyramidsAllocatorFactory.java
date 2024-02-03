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
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class PyramidsAllocatorFactory implements AlgorithmFactory<PyramidsAllocator> {

    private int numberOfPeaks = 10;


    private DoubleParameter smoothingValue = new FixedDoubleParameter(.7d);

    private int maxSpread = 6;


    private DoubleParameter peakBiomass = new FixedDoubleParameter(1);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public PyramidsAllocator apply(final FishState fishState) {
        return new PyramidsAllocator(
            numberOfPeaks,
            smoothingValue.applyAsDouble(fishState.getRandom()),
            maxSpread,
            peakBiomass.applyAsDouble(fishState.getRandom())
        );
    }

    /**
     * Getter for property 'numberOfPeaks'.
     *
     * @return Value for property 'numberOfPeaks'.
     */
    public int getNumberOfPeaks() {
        return numberOfPeaks;
    }

    /**
     * Setter for property 'numberOfPeaks'.
     *
     * @param numberOfPeaks Value to set for property 'numberOfPeaks'.
     */
    public void setNumberOfPeaks(final int numberOfPeaks) {
        this.numberOfPeaks = numberOfPeaks;
    }

    /**
     * Getter for property 'smoothingValue'.
     *
     * @return Value for property 'smoothingValue'.
     */
    public DoubleParameter getSmoothingValue() {
        return smoothingValue;
    }

    /**
     * Setter for property 'smoothingValue'.
     *
     * @param smoothingValue Value to set for property 'smoothingValue'.
     */
    public void setSmoothingValue(final DoubleParameter smoothingValue) {
        this.smoothingValue = smoothingValue;
    }

    /**
     * Getter for property 'maxSpread'.
     *
     * @return Value for property 'maxSpread'.
     */
    public int getMaxSpread() {
        return maxSpread;
    }

    /**
     * Setter for property 'maxSpread'.
     *
     * @param maxSpread Value to set for property 'maxSpread'.
     */
    public void setMaxSpread(final int maxSpread) {
        this.maxSpread = maxSpread;
    }

    /**
     * Getter for property 'peakBiomass'.
     *
     * @return Value for property 'peakBiomass'.
     */
    public DoubleParameter getPeakBiomass() {
        return peakBiomass;
    }

    /**
     * Setter for property 'peakBiomass'.
     *
     * @param peakBiomass Value to set for property 'peakBiomass'.
     */
    public void setPeakBiomass(final DoubleParameter peakBiomass) {
        this.peakBiomass = peakBiomass;
    }
}
