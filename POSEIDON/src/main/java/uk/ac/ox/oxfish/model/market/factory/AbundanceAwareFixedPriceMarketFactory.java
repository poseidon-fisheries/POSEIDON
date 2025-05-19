/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPricingStrategy;
import uk.ac.ox.oxfish.model.market.FlexibleAbundanceMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * functionally exactly like FixedPriceMarket factory except with data-collectors
 * specific to abundance based classes. <br>
 * Its true purpose is that by returning FlexibleAbundanceMarket I am able to modify
 * prices easily while the model is running!
 */
public class AbundanceAwareFixedPriceMarketFactory implements AlgorithmFactory<FlexibleAbundanceMarket> {


    private DoubleParameter marketPrice = new FixedDoubleParameter(10.0);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FlexibleAbundanceMarket apply(FishState state) {
        return new FlexibleAbundanceMarket(
            new FixedPricingStrategy(
                marketPrice.applyAsDouble(state.getRandom())
            )
        );
    }


    public DoubleParameter getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(DoubleParameter marketPrice) {
        this.marketPrice = marketPrice;
    }

}
