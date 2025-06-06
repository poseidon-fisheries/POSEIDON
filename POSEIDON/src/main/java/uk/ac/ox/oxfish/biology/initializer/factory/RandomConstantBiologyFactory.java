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

package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.RandomConstantBiologyInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Creates a RandomConstantBiologyInitializer
 * Created by carrknight on 6/22/15.
 */
public class RandomConstantBiologyFactory implements AlgorithmFactory<RandomConstantBiologyInitializer> {


    private double minBiomass = 0;

    private double maxBiomass = 5000;

    /**
     * how many times we attempt to smooth the biology between two elements
     */
    private DoubleParameter biologySmoothingIndex = new FixedDoubleParameter(1000000);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RandomConstantBiologyInitializer apply(final FishState state) {
        return new RandomConstantBiologyInitializer(minBiomass, maxBiomass,
            (int) biologySmoothingIndex.applyAsDouble(state.random)
        );
    }


    public double getMinBiomass() {
        return minBiomass;
    }

    public void setMinBiomass(final double minBiomass) {
        this.minBiomass = minBiomass;
    }

    public double getMaxBiomass() {
        return maxBiomass;
    }

    public void setMaxBiomass(final double maxBiomass) {
        this.maxBiomass = maxBiomass;
    }

    public DoubleParameter getBiologySmoothingIndex() {
        return biologySmoothingIndex;
    }

    public void setBiologySmoothingIndex(final DoubleParameter biologySmoothingIndex) {
        this.biologySmoothingIndex = biologySmoothingIndex;
    }
}
