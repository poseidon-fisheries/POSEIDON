package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.model.data.collectors.Counter;

/**
 * market with an additional counter handle (useful for data collection)
 */
public interface MarketWithCounter extends Market {

    public Counter getDailyCounter();


}
