package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * A fixed price market maker
 * Created by carrknight on 8/11/15.
 */
public class FixedPriceMarketFactory implements AlgorithmFactory<FixedPriceMarket>
{

    private DoubleParameter marketPrice = new FixedDoubleParameter(10.0);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedPriceMarket apply(FishState state) {
        return new FixedPriceMarket(marketPrice.apply(state.getRandom()));
    }


    public DoubleParameter getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(DoubleParameter marketPrice) {
        this.marketPrice = marketPrice;
    }
}
