package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;
/**
 * The most "generic" market for "abundance" species.
 * The pricing behaviour is commanded by a pricing strategy which should be fully swappable
 *
 */
public class FlexibleAbundanceMarket extends AbstractMarket {


    /**
     * the object deciding the prices
     */
    private PricingStrategy pricingStrategy;

    public static final String AGE_BIN_PREFIX = " - age bin ";


    public FlexibleAbundanceMarket(PricingStrategy pricingStrategy) {

        this.pricingStrategy = pricingStrategy;

    }

    @Override
    protected TradeInfo sellFishImplementation(Hold hold, Fisher fisher,
                                               Regulation regulation,
                                               FishState state, Species species) {


        //find out legal biomass sold
        double proportionActuallySellable =
                Math.min(1d,
                        regulation.maximumBiomassSellable(fisher,
                                species,
                                state) /
                                hold.getWeightOfCatchInHold(species));
        assert proportionActuallySellable >= 0;
        assert proportionActuallySellable <= 1;

        if (proportionActuallySellable == 0)
            return new TradeInfo(0, species, 0d);


        double earnings = 0;
        double sold = 0;
        for (int age = 0; age < species.getNumberOfBins(); age++) {

            double soldThisBin = hold.getWeightOfBin(species, age);
            //reweight because you might be not allowed to sell more than x
            soldThisBin *= proportionActuallySellable;
            //if there is nothing to sell here...
            if(soldThisBin<=0)
            {
                //sometimes this becomes -1e-35
                assert soldThisBin>=-FishStateUtilities.EPSILON;
                continue;
            }

            //look for the correct price bin
            double priceForThisBin = pricingStrategy.getPricePerKg(
                    species,
                    fisher,
                    age,
                    soldThisBin
            );

            getDailyCounter().count(LANDINGS_COLUMN_NAME + " - age bin " + age, soldThisBin);
            earnings += soldThisBin * priceForThisBin;
            getDailyCounter().count(EARNINGS_COLUMN_NAME + " - age bin " + age, soldThisBin * priceForThisBin);

            sold += soldThisBin;
        }

        //give fisher the money
        fisher.earn(earnings);

        //tell regulation
        regulation.reactToSale(species, fisher, sold, earnings, state);

        //return data!
        return new TradeInfo(sold, species, earnings);
    }

    @Override
    public double getMarginalPrice() {
        return pricingStrategy.getMarginalPrice();
    }

    @Override
    public void start(FishState state) {
        super.start(state);

        for (int age = 0; age < getSpecies().getNumberOfBins(); age++) {
            String columnName = LANDINGS_COLUMN_NAME + AGE_BIN_PREFIX + age;
            getDailyCounter().addColumn(columnName);
            String finalColumnName1 = columnName;
            getData().registerGatherer(columnName, new Gatherer<Market>() {
                        @Override
                        public Double apply(Market market) {
                            return getDailyCounter().getColumn(finalColumnName1);
                        }
                    },
                    0);


            columnName = EARNINGS_COLUMN_NAME + " - age bin " + age;
            getDailyCounter().addColumn(columnName);
            String finalColumnName = columnName;
            getData().registerGatherer(columnName, new Gatherer<Market>() {
                        @Override
                        public Double apply(Market market) {
                            return getDailyCounter().getColumn(finalColumnName);
                        }
                    },
                    0);
        }
    }


    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }
}

