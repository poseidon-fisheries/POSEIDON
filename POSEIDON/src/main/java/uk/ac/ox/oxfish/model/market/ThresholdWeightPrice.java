package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * there are two prices, depending on whether the catch is below or above a threshold (defined as weight per individual).
 */
public class ThresholdWeightPrice implements PricingStrategy {


    final private double priceAboveThreshold;

    final private double priceBelowThreshold;

    final private double weightThreshold;

    public ThresholdWeightPrice(double priceAboveThreshold,
                                double priceBelowThreshold,
                                double weightThreshold) {
        this.priceAboveThreshold = priceAboveThreshold;
        this.priceBelowThreshold = priceBelowThreshold;
        this.weightThreshold = weightThreshold;
    }

    @Override
    public void start(FishState model) {

    }

    @Override
    public double getPricePerKg(Species speciesBeingSold, Fisher seller, int biologicalBin, double quantitySold) {
        if(
                speciesBeingSold.getWeight(0,biologicalBin) <
                        weightThreshold)
            return priceBelowThreshold;
        else
            return priceAboveThreshold;
    }

    @Override
    public void reactToSale(TradeInfo info) {

    }

    @Override
    public double getMarginalPrice() {
        return (priceBelowThreshold+priceAboveThreshold)/2;
    }
}
