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
