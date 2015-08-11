package uk.ac.ox.oxfish.model.market.factory;


import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.CongestedMarket;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class CongestedMarketFactory  implements AlgorithmFactory<CongestedMarket>
{


    /**
     * if in the market there is less than this biomass available, there is no penalty to price
     */
    private DoubleParameter  acceptableBiomassThreshold = new FixedDoubleParameter(7000);


    /**
     * maximum price of the market
     */
    private DoubleParameter maxPrice = new FixedDoubleParameter(10);

    /**
     * by how much does price decrease in proportion to how much we are above biomass threshold. It's basically $/weight
     */
    private DoubleParameter demandSlope = new FixedDoubleParameter(.0005);

    /**
     * how much biomass for this fish gets consumed each day (and removed from the market)
     */
    private DoubleParameter dailyConsumption = new FixedDoubleParameter(8000);




    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public CongestedMarket apply(FishState state) {
        return new CongestedMarket(acceptableBiomassThreshold.apply(state.getRandom()),
                                   maxPrice.apply(state.getRandom()),
                                   demandSlope.apply(state.getRandom()),
                                   dailyConsumption.apply(state.getRandom()));
    }


    public DoubleParameter getAcceptableBiomassThreshold() {
        return acceptableBiomassThreshold;
    }

    public void setAcceptableBiomassThreshold(
            DoubleParameter acceptableBiomassThreshold) {
        this.acceptableBiomassThreshold = acceptableBiomassThreshold;
    }

    public DoubleParameter getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(DoubleParameter maxPrice) {
        this.maxPrice = maxPrice;
    }

    public DoubleParameter getDemandSlope() {
        return demandSlope;
    }

    public void setDemandSlope(DoubleParameter demandSlope) {
        this.demandSlope = demandSlope;
    }

    public DoubleParameter getDailyConsumption() {
        return dailyConsumption;
    }

    public void setDailyConsumption(DoubleParameter dailyConsumption) {
        this.dailyConsumption = dailyConsumption;
    }
}
