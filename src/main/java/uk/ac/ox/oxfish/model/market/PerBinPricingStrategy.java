package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;

/**
 * very generic price for abundance: each bin is allocated a specific price.
 */
public class PerBinPricingStrategy implements PricingStrategy {

    private final double[] pricePerBin;

    private final double averagePrice;


    public PerBinPricingStrategy(double[] pricePerBin) {
        this.pricePerBin = pricePerBin;
        this.averagePrice = Arrays.stream(pricePerBin).summaryStatistics().getAverage();
    }

    @Override
    public void start(FishState model) {

    }

    @Override
    public double getPricePerKg(Species speciesBeingSold, Fisher seller, int biologicalBin, double quantitySold) {
        return pricePerBin[biologicalBin];
    }

    @Override
    public void reactToSale(TradeInfo info) {

    }

    @Override
    public double getMarginalPrice() {
        return averagePrice;
    }
}
