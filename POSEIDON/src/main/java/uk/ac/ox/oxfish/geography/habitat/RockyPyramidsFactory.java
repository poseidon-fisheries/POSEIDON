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

package uk.ac.ox.oxfish.geography.habitat;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Creates rocky initializers
 * Created by carrknight on 10/1/15.
 */
public class RockyPyramidsFactory implements AlgorithmFactory<RockyPyramidsHabitatInitializer> {


    private int numberOfPeaks = 10;


    private DoubleParameter smoothingValue = new FixedDoubleParameter(.7d);

    private int maxSpread = 6;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RockyPyramidsHabitatInitializer apply(FishState state) {
        return new RockyPyramidsHabitatInitializer(numberOfPeaks, smoothingValue,
            maxSpread
        );
    }

    public int getNumberOfPeaks() {
        return numberOfPeaks;
    }

    public void setNumberOfPeaks(int numberOfPeaks) {
        this.numberOfPeaks = numberOfPeaks;
    }

    public DoubleParameter getSmoothingValue() {
        return smoothingValue;
    }

    public void setSmoothingValue(DoubleParameter smoothingValue) {
        this.smoothingValue = smoothingValue;
    }

    public int getMaxSpread() {
        return maxSpread;
    }

    public void setMaxSpread(int maxSpread) {
        this.maxSpread = maxSpread;
    }
}
