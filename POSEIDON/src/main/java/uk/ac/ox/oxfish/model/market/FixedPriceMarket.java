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

package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * The simplest market, the price is constant no matter how much stuff gets sold every day
 * Created by carrknight on 5/3/15.
 */
public class FixedPriceMarket extends AbstractBiomassMarket {

    private double price;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
  //      Preconditions.checkArgument(price>=0); can be negative if it's a fine!
        this.price = price;
    }


    public FixedPriceMarket(double price) {
        this.price = price;
    }

    /**
     * the only method to implement for subclasses. Needs to actually do the trading and return the result
     *
     * @param biomass     the biomass caught by the seller
     * @param fisher      the seller
     * @param regulation the rules the seller abides to
     * @param state       the model
     * @return TradeInfo  results
     */
    @Override
    protected TradeInfo sellFishImplementation(
            double biomass, Fisher fisher, Regulation regulation, FishState state,
            Species species) {
        return Market.defaultMarketTransaction(biomass, fisher, regulation, state,
                                               biomassTraded -> biomassTraded *price, species);
    }


    /**
     * how much do you intend to pay the next epsilon amount of biomass sold here
     *
     * @return price
     */
    @Override
    public double getMarginalPrice() {
        return price;
    }
}
