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

package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.FromLeftToRightInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * The initializer for the left-to-right biology initializer
 * Created by carrknight on 6/22/15.
 */
public class FromLeftToRightFactory implements AlgorithmFactory<BiologyInitializer> {

    /**
     * leftmost biomass
     */
    private DoubleParameter maximumBiomass = new FixedDoubleParameter(5000);

    /**
     * how many times we attempt to smooth the biology between two elements
     */
    private DoubleParameter biologySmoothingIndex = new FixedDoubleParameter(1000000);


    private DoubleParameter exponent = new FixedDoubleParameter(2);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BiologyInitializer apply(final FishState state) {
        return new FromLeftToRightInitializer(maximumBiomass.applyAsDouble(state.random),
            (int) biologySmoothingIndex.applyAsDouble(state.random), exponent.applyAsDouble(state.getRandom())
        );
    }


    public DoubleParameter getMaximumBiomass() {
        return maximumBiomass;
    }

    public void setMaximumBiomass(final DoubleParameter maximumBiomass) {
        this.maximumBiomass = maximumBiomass;
    }

    public DoubleParameter getBiologySmoothingIndex() {
        return biologySmoothingIndex;
    }

    public void setBiologySmoothingIndex(final DoubleParameter biologySmoothingIndex) {
        this.biologySmoothingIndex = biologySmoothingIndex;
    }

    /**
     * Getter for property 'exponent'.
     *
     * @return Value for property 'exponent'.
     */
    public DoubleParameter getExponent() {
        return exponent;
    }

    /**
     * Setter for property 'exponent'.
     *
     * @param exponent Value to set for property 'exponent'.
     */
    public void setExponent(final DoubleParameter exponent) {
        this.exponent = exponent;
    }
}
