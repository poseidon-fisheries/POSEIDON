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
import uk.ac.ox.oxfish.model.market.CongestedMarket;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class CongestedMarketFactory  implements AlgorithmFactory<CongestedMarket>
{


    /**
     * if in the market there is less than this biomass available, there is no penalty to price
     */
    private DoubleParameter  acceptableBiomassThreshold = new FixedDoubleParameter(7000);


    /**
     * maximum price of the market
     */
    private DoubleParameter maxPrice = new FixedDoubleParameter(10);

    /**
     * by how much does price decrease in proportion to how much we are above biomass threshold. It's basically $/weight
     */
    private DoubleParameter demandSlope = new FixedDoubleParameter(.0001);

    /**
     * how much biomass for this fish gets consumed each day (and removed from the market)
     */
    private DoubleParameter dailyConsumption = new FixedDoubleParameter(8000);



    private DoubleParameter consumptionPeriod = new FixedDoubleParameter(1);



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public CongestedMarket apply(FishState state) {
        return new CongestedMarket(acceptableBiomassThreshold.apply(state.getRandom()),
                                   maxPrice.apply(state.getRandom()),
                                   demandSlope.apply(state.getRandom()),
                                   dailyConsumption.apply(state.getRandom()),
                                   consumptionPeriod.apply(state.getRandom()).intValue());
    }


    public DoubleParameter getAcceptableBiomassThreshold() {
        return acceptableBiomassThreshold;
    }

    public void setAcceptableBiomassThreshold(
            DoubleParameter acceptableBiomassThreshold) {
        this.acceptableBiomassThreshold = acceptableBiomassThreshold;
    }

    public DoubleParameter getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(DoubleParameter maxPrice) {
        this.maxPrice = maxPrice;
    }

    public DoubleParameter getDemandSlope() {
        return demandSlope;
    }

    public void setDemandSlope(DoubleParameter demandSlope) {
        this.demandSlope = demandSlope;
    }

    public DoubleParameter getDailyConsumption() {
        return dailyConsumption;
    }

    public void setDailyConsumption(DoubleParameter dailyConsumption) {
        this.dailyConsumption = dailyConsumption;
    }

    public DoubleParameter getConsumptionPeriod() {
        return consumptionPeriod;
    }

    public void setConsumptionPeriod(DoubleParameter consumptionPeriod) {
        this.consumptionPeriod = consumptionPeriod;
    }
}
