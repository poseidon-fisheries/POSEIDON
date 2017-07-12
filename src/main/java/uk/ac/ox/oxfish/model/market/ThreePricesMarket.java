package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 *
 * There are 2 thresholds and three prices (one below the first threshold, one in between and one above)
 * where the thresholds are in terms of "age"
 *
 * Created by carrknight on 7/4/17.
 */
public class ThreePricesMarket extends AbstractMarket {


    private final int lowAgeThreshold;

    private final int highAgeThreshold;

    private final double priceBelowThreshold;

    private final double priceBetweenThresholds;

    private final double priceAboveThresholds;



    public ThreePricesMarket(
            int lowAgeThreshold,
            int highAgeThreshold,
            double priceBelowThreshold,
            double priceBetweenThresholds,
            double priceAboveThresholds) {
        this.lowAgeThreshold = lowAgeThreshold;
        this.highAgeThreshold = highAgeThreshold;
        this.priceBelowThreshold = priceBelowThreshold;
        this.priceBetweenThresholds = priceBetweenThresholds;
        this.priceAboveThresholds = priceAboveThresholds;
    }


    /**
     * starts gathering data. If called multiple times all the calls after the first are ignored
     *
     * @param state the model
     */
    @Override
    public void start(FishState state) {
        super.start(state);


        for(int age =0; age<getSpecies().getMaxAge()+1;age++) {
            String columnName = LANDINGS_COLUMN_NAME + " - age bin " + age;
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

    /**
     * returns the average marginal price
     *
     * @return price
     */
    @Override
    public double getMarginalPrice() {
        return (priceBelowThreshold +
                priceBetweenThresholds +
                priceAboveThresholds)/3d;
    }

    @Override
    protected TradeInfo sellFishImplementation(
            Hold hold,
            Fisher fisher,
            Regulation regulation,
            FishState state,
            Species species)
    {



        //find out legal biomass sold
        double proportionActuallySellable =
                Math.min(1d,
                         regulation.maximumBiomassSellable(fisher,
                                                           species,
                                                           state)/
                                 hold.getWeightOfCatchInHold(species));
        assert proportionActuallySellable>=0;
        assert proportionActuallySellable<=1;

        if(proportionActuallySellable == 0)
            return new TradeInfo(0,species,0d);

        //for each age find price sold at and quantity sold
        double price =priceBelowThreshold;
        double earnings = 0;
        double sold = 0;
        for(int age =0; age<species.getMaxAge()+1;age++)
        {
            if(age>highAgeThreshold)
                price= priceAboveThresholds;
            else
            if(age>lowAgeThreshold)
                price=priceBetweenThresholds;

            double soldThisBin =  hold.getWeightOfBin(species,age);
            //reweight because you might be not allowed to sell more than x
            soldThisBin *= proportionActuallySellable;
            getDailyCounter().count(LANDINGS_COLUMN_NAME + " - age bin " + age,soldThisBin);
            earnings+= soldThisBin *price;
            getDailyCounter().count(EARNINGS_COLUMN_NAME + " - age bin " + age,soldThisBin *price);

            sold+= soldThisBin;
        }

        //give fisher the money
        fisher.earn(earnings);

        //tell regulation
        regulation.reactToSale(species, fisher , sold, earnings);

        //return data!
        return new TradeInfo(sold,species,earnings);
    }
}
