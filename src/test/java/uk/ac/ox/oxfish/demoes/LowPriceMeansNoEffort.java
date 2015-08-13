package uk.ac.ox.oxfish.demoes;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.experiments.EffortThrottling;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;


public class LowPriceMeansNoEffort {


    /**
     * if the price doesn't pay for trips, just stay home.
     * @throws Exception
     */
    @Test
    public void priceIsTooLowAgentsStopFishing() throws Exception
    {


        //sets very low price
        FixedPriceMarketFactory market = new FixedPriceMarketFactory();
        market.setMarketPrice(new FixedDoubleParameter(2.0));

        FishState state = EffortThrottling.effortThrottling(40, market, System.currentTimeMillis(),
                                                            new UniformDoubleParameter(0.001, 1), null, null
        );

        //probability should be very low!
        Assert.assertTrue(state.getDailyDataSet().getLatestObservation("Probability to leave port") < .001);

    }
}
