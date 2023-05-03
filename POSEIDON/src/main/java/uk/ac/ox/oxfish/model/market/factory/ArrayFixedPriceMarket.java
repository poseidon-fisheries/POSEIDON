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

package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

/**
 * loops through an array of prices to create fixed price markets
 * Created by carrknight on 8/31/16.
 */
public class ArrayFixedPriceMarket implements AlgorithmFactory<FixedPriceMarket> {


    /**
     * price is 10 for 2 species
     */
    private String prices = "10,10";


    /**
     * when first called the constructor
     */
    private double[] pricesAsNumbers = null;

    private int index = 0;

    public ArrayFixedPriceMarket() {
    }


    public ArrayFixedPriceMarket(String prices) {
        this.prices = prices;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public FixedPriceMarket apply(FishState fishState) {

        if(pricesAsNumbers == null)
            pricesAsNumbers = Arrays.stream(prices.split(",")).
                    mapToDouble(Double::parseDouble).toArray();

        FixedPriceMarket market = new FixedPriceMarket(pricesAsNumbers[index]);
        index++;
        if(index==pricesAsNumbers.length)
            index=0;
        return market;
    }

    /**
     * Getter for property 'prices'.
     *
     * @return Value for property 'prices'.
     */
    public String getPrices() {
        return prices;
    }

    /**
     * Setter for property 'prices'.
     *
     * @param prices Value to set for property 'prices'.
     */
    public void setPrices(String prices) {
        this.prices = prices;
    }
}
