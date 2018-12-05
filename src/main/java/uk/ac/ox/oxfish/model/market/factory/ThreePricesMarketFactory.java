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
import uk.ac.ox.oxfish.model.market.ThreePricesMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/12/17.
 */
public class ThreePricesMarketFactory implements AlgorithmFactory<ThreePricesMarket>{


    private DoubleParameter lowAgeThreshold = new FixedDoubleParameter(1);

    private DoubleParameter highAgeThreshold= new FixedDoubleParameter(10);

    private DoubleParameter priceBelowThreshold= new FixedDoubleParameter(10);

    private DoubleParameter priceBetweenThresholds= new FixedDoubleParameter(10);

    private DoubleParameter priceAboveThresholds= new FixedDoubleParameter(10);


    public ThreePricesMarketFactory() {
    }

    public ThreePricesMarketFactory(
            double lowAgeThreshold, double highAgeThreshold,
            double priceBelowThreshold, double priceBetweenThresholds,
            double priceAboveThresholds) {
        this.lowAgeThreshold = new FixedDoubleParameter(lowAgeThreshold);
        this.highAgeThreshold = new FixedDoubleParameter(highAgeThreshold);
        this.priceBelowThreshold = new FixedDoubleParameter(priceBelowThreshold);
        this.priceBetweenThresholds = new FixedDoubleParameter(priceBetweenThresholds);
        this.priceAboveThresholds = new FixedDoubleParameter(priceAboveThresholds);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ThreePricesMarket apply(FishState state) {


        return new ThreePricesMarket(
                lowAgeThreshold.apply(state.getRandom()).intValue(),
                highAgeThreshold.apply(state.getRandom()).intValue(),
                priceBelowThreshold.apply(state.getRandom()),
                priceBetweenThresholds.apply(state.getRandom()),
                priceAboveThresholds.apply(state.getRandom())
        );
    }

    /**
     * Getter for property 'lowAgeThreshold'.
     *
     * @return Value for property 'lowAgeThreshold'.
     */
    public DoubleParameter getLowAgeThreshold() {
        return lowAgeThreshold;
    }

    /**
     * Setter for property 'lowAgeThreshold'.
     *
     * @param lowAgeThreshold Value to set for property 'lowAgeThreshold'.
     */
    public void setLowAgeThreshold(DoubleParameter lowAgeThreshold) {
        this.lowAgeThreshold = lowAgeThreshold;
    }

    /**
     * Getter for property 'highAgeThreshold'.
     *
     * @return Value for property 'highAgeThreshold'.
     */
    public DoubleParameter getHighAgeThreshold() {
        return highAgeThreshold;
    }

    /**
     * Setter for property 'highAgeThreshold'.
     *
     * @param highAgeThreshold Value to set for property 'highAgeThreshold'.
     */
    public void setHighAgeThreshold(DoubleParameter highAgeThreshold) {
        this.highAgeThreshold = highAgeThreshold;
    }

    /**
     * Getter for property 'priceBelowThreshold'.
     *
     * @return Value for property 'priceBelowThreshold'.
     */
    public DoubleParameter getPriceBelowThreshold() {
        return priceBelowThreshold;
    }

    /**
     * Setter for property 'priceBelowThreshold'.
     *
     * @param priceBelowThreshold Value to set for property 'priceBelowThreshold'.
     */
    public void setPriceBelowThreshold(DoubleParameter priceBelowThreshold) {
        this.priceBelowThreshold = priceBelowThreshold;
    }

    /**
     * Getter for property 'priceBetweenThresholds'.
     *
     * @return Value for property 'priceBetweenThresholds'.
     */
    public DoubleParameter getPriceBetweenThresholds() {
        return priceBetweenThresholds;
    }

    /**
     * Setter for property 'priceBetweenThresholds'.
     *
     * @param priceBetweenThresholds Value to set for property 'priceBetweenThresholds'.
     */
    public void setPriceBetweenThresholds(DoubleParameter priceBetweenThresholds) {
        this.priceBetweenThresholds = priceBetweenThresholds;
    }

    /**
     * Getter for property 'priceAboveThresholds'.
     *
     * @return Value for property 'priceAboveThresholds'.
     */
    public DoubleParameter getPriceAboveThresholds() {
        return priceAboveThresholds;
    }

    /**
     * Setter for property 'priceAboveThresholds'.
     *
     * @param priceAboveThresholds Value to set for property 'priceAboveThresholds'.
     */
    public void setPriceAboveThresholds(DoubleParameter priceAboveThresholds) {
        this.priceAboveThresholds = priceAboveThresholds;
    }
}
