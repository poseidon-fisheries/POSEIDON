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

package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.LessThanXFishersHereExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;


public class LessThanXFishersHereExtractorFactory implements AlgorithmFactory<LessThanXFishersHereExtractor> {


    private DoubleParameter minimumNumberOfFishersToMakeItUnacceptable = new FixedDoubleParameter(1d);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public LessThanXFishersHereExtractor apply(final FishState state) {
        return new LessThanXFishersHereExtractor(
            (int) minimumNumberOfFishersToMakeItUnacceptable.applyAsDouble(state.getRandom()));
    }

    /**
     * Getter for property 'minimumNumberOfFishersToMakeItUnacceptable'.
     *
     * @return Value for property 'minimumNumberOfFishersToMakeItUnacceptable'.
     */
    public DoubleParameter getMinimumNumberOfFishersToMakeItUnacceptable() {
        return minimumNumberOfFishersToMakeItUnacceptable;
    }

    /**
     * Setter for property 'minimumNumberOfFishersToMakeItUnacceptable'.
     *
     * @param minimumNumberOfFishersToMakeItUnacceptable Value to set for property 'minimumNumberOfFishersToMakeItUnacceptable'.
     */
    public void setMinimumNumberOfFishersToMakeItUnacceptable(
        final DoubleParameter minimumNumberOfFishersToMakeItUnacceptable
    ) {
        this.minimumNumberOfFishersToMakeItUnacceptable = minimumNumberOfFishersToMakeItUnacceptable;
    }
}
