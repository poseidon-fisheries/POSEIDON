package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.Startable;

/**
 * pricing algorithm for an abundance based market.
 * Basically the "strategy" pattern so we can swap in and out different pricing systems (or just change
 * prices) without touching the market object itself.
 */
public interface PricingStrategy extends Startable {


    /**
     * get the price commander per unit of fish sold for this fish at this bin (as in abundance bin)
     * @param speciesBeingSold species being sold
     * @param seller who is selling
     * @param biologicalBin the abundance "bin" i.e. either the length or the age (depending on the model)
     * @param quantitySold how much is being sold
     * @return price PER UNIT (not scaled to quantity sold!)
     */
    public double getPricePerKg(Species speciesBeingSold,
                                Fisher seller,
                                int biologicalBin,
                                double quantitySold);


    public void reactToSale(TradeInfo info);

    /**
     * a guessed price of what the next kg sold will be; this is a bad interface we
     * are inheriting from AbstractMarket
     */
    public double getMarginalPrice();



}
