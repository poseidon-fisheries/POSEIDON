package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * simplest pricing strategy: each bin and each kg sold always fetches the same price
 */
public class FixedPricingStrategy implements PricingStrategy {


    private final double fixedPrice;

    public FixedPricingStrategy(double fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    @Override
    public void start(FishState model) {

    }

    @Override
    public double getPricePerKg(
        Species speciesBeingSold,
        Fisher seller,
        int biologicalBin,
        double quantitySold
    ) {
        return fixedPrice;
    }

    @Override
    public void reactToSale(TradeInfo info) {
    }

    @Override
    public double getMarginalPrice() {
        return fixedPrice;
    }


}
