package uk.ac.ox.oxfish.model.market.factory;


import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.WeightLimitMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class WeightLimitMarketFactory implements AlgorithmFactory<WeightLimitMarket> {


    private DoubleParameter priceBelowWeight = new FixedDoubleParameter(0d);

    private DoubleParameter priceAboveWeight = new FixedDoubleParameter(100d);

    private DoubleParameter weightThreshold = new FixedDoubleParameter(0.5);


    @Override
    public WeightLimitMarket apply(FishState fishState) {
        return new WeightLimitMarket(
                priceBelowWeight.apply(fishState.getRandom()),
                priceAboveWeight.apply(fishState.getRandom()),
                weightThreshold.apply(fishState.getRandom())
        );
    }

    public DoubleParameter getPriceBelowWeight() {
        return priceBelowWeight;
    }

    public void setPriceBelowWeight(DoubleParameter priceBelowWeight) {
        this.priceBelowWeight = priceBelowWeight;
    }

    public DoubleParameter getPriceAboveWeight() {
        return priceAboveWeight;
    }

    public void setPriceAboveWeight(DoubleParameter priceAboveWeight) {
        this.priceAboveWeight = priceAboveWeight;
    }

    public DoubleParameter getWeightThreshold() {
        return weightThreshold;
    }

    public void setWeightThreshold(DoubleParameter weightThreshold) {
        this.weightThreshold = weightThreshold;
    }
}