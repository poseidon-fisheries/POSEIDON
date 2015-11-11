package uk.ac.ox.oxfish.model.market.itq;

/**
 * A method to decide for any pair of crossing quotas what the trading price is
 * Created by carrknight on 11/11/15.
 */
public interface PricingPolicy {



    public double tradePrice(double askPrice, double bidPrice);


}
