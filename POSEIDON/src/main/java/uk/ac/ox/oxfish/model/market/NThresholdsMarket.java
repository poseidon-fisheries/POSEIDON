package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;

/**
 * An old way to construct flexible price markets which is now kind of a facade
 * except it bootstraps its own price when setting a price
 */
public class NThresholdsMarket extends FlexibleAbundanceMarket {

    public static final PricingStrategy PLACE_HOLDER_SINGLETON = new PricingStrategy() {
        @Override
        public double getPricePerKg(Species speciesBeingSold, Fisher seller, int biologicalBin, double quantitySold) {
            throw new RuntimeException("Price not setup yet!");
        }

        @Override
        public void reactToSale(TradeInfo info) {

        }

        @Override
        public double getMarginalPrice() {
            return 0;
        }

        @Override
        public void start(FishState model) {

        }
    };
    /**
     * say we have a price for fish below bin 5, between bin 5 and 10, and 11 or above
     * then here we would have [5,10]
     */
    final private int binThresholds[];

    /**
     * say we charge p1 for fish below bin 5, p2 between bin 5 and 10, and p3 for bins 11 or above
     * this would have [p1,p2,p3]
     */
    final private double pricePerSegment[];


    public NThresholdsMarket(int[] binThresholds, double[] pricePerSegment) {
        //until you set a species you can't set prices. This is okay
        //because it's also the behaviour of the general AbstractMarket
        super(PLACE_HOLDER_SINGLETON);
        this.binThresholds = binThresholds;
        //all the bins must be in order already!
        for (int i = 1; i < binThresholds.length; i++) {
            Preconditions.checkArgument(
                this.binThresholds[i] > this.binThresholds[i - 1],
                Arrays.toString(binThresholds)
            );
        }
        this.pricePerSegment = pricePerSegment;
        Preconditions.checkArgument(this.pricePerSegment.length == this.binThresholds.length + 1);

    }

    static public NThresholdsMarket ThreePricesMarket(
        int lowAgeThreshold,
        int highAgeThreshold,
        double priceBelowThreshold,
        double priceBetweenThresholds,
        double priceAboveThresholds
    ) {

        return new NThresholdsMarket(
            new int[]{lowAgeThreshold, highAgeThreshold},
            new double[]{priceBelowThreshold, priceBetweenThresholds, priceAboveThresholds}

        );
    }

    @Override
    public void setSpecies(Species species) {
        super.setSpecies(species);
        if (getPricingStrategy() == PLACE_HOLDER_SINGLETON) {
            double[] pricePerBin = new double[species.getNumberOfBins()];
            for (int age = 0; age < species.getNumberOfBins(); age++) {
                //look for the correct bin
                pricePerBin[age] = pricePerSegment[0];
                for (int i = 0; i < binThresholds.length; i++) {
                    if (age > binThresholds[i])
                        pricePerBin[age] = pricePerSegment[i + 1];
                    else
                        break;
                }
            }
            setPricingStrategy(new PerBinPricingStrategy(pricePerBin));
        }
    }

    public int[] getBinThresholds() {
        return binThresholds;
    }

    public double[] getPricePerSegment() {
        return pricePerSegment;
    }


}
