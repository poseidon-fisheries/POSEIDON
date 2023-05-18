package uk.ac.ox.oxfish.model.market;

/**
 * Now just a facade for FlexibleAbundanceMarket with PerBinPricingStrategy
 * For each bin (across subdivisions!) you are given its own price.
 */
public class PerBinMarket extends FlexibleAbundanceMarket {


    public PerBinMarket(double[] pricePerBin) {
        super(new PerBinPricingStrategy(pricePerBin));

    }


}
