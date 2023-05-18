package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPricingStrategy;
import uk.ac.ox.oxfish.model.market.FlexibleAbundanceMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

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
