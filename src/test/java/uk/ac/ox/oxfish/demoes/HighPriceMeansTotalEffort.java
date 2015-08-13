package uk.ac.ox.oxfish.demoes;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.experiments.EffortThrottling;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 *
 * Created by carrknight on 8/13/15.
 */
public class HighPriceMeansTotalEffort {


    /**
     * price becomes so high it's irresistible and everybody puts in infinite effort
     * @throws Exception
     */
    @Test
    public void priceIsSoHighEverybodyIsFishing() throws Exception
    {

        //sets very low price
        FixedPriceMarketFactory market = new FixedPriceMarketFactory();
        market.setMarketPrice(new FixedDoubleParameter(10.0));

        FishState state = EffortThrottling.effortThrottling(40, market, System.currentTimeMillis(),
                                                            new UniformDoubleParameter(0.001, 1), null, null
        );

        Assert.assertTrue(state.getDailyDataSet().getLatestObservation("Probability to leave port") > 0.8);

    }
}
