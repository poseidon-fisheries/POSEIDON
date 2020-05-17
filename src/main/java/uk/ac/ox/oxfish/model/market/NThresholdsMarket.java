package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

/**
 * basically a generalization of the "ThreePricesMarket": depending on which bin you are, you get a different price
 */
public class NThresholdsMarket extends AbstractMarket {

    public static final String AGE_BIN_PREFIX = " - age bin ";
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

    /**
     * just a simple variable averaging the price per segment. Meaningless but returned when asked for marginal price
     */
    final private double averageHeadPrice;

    public NThresholdsMarket(int[] binThresholds, double[] pricePerSegment)
    {
        this.binThresholds = binThresholds;
        //all the bins must be in order already!
        for (int i = 1; i < binThresholds.length; i++) {
            Preconditions.checkArgument( this.binThresholds[i]>this.binThresholds[i-1],
                    Arrays.toString(binThresholds));
        }
        this.pricePerSegment = pricePerSegment;
        Preconditions.checkArgument(this.pricePerSegment.length==this.binThresholds.length+1);

        averageHeadPrice = Arrays.stream(pricePerSegment).average().getAsDouble();
    }

    static public NThresholdsMarket ThreePricesMarket(
            int lowAgeThreshold,
            int highAgeThreshold,
            double priceBelowThreshold,
            double priceBetweenThresholds,
            double priceAboveThresholds) {

        return new NThresholdsMarket(
                new int[]{lowAgeThreshold,highAgeThreshold},
                new double[]{priceBelowThreshold,priceBetweenThresholds,priceAboveThresholds}

        );
    }

    @Override
    protected TradeInfo sellFishImplementation(Hold hold, Fisher fisher, Regulation regulation, FishState state, Species species) {

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
        double earnings = 0;
        double sold = 0;
        for(int age =0; age<species.getNumberOfBins();age++)
        {
            //look for the correct bin
            double priceForThisBin = pricePerSegment[0];
            for (int i = 0; i < binThresholds.length; i++) {
                if(age>binThresholds[i])
                    priceForThisBin= pricePerSegment[i+1];
                else
                    break;
            }


            double soldThisBin =  hold.getWeightOfBin(species,age);
            //reweight because you might be not allowed to sell more than x
            soldThisBin *= proportionActuallySellable;
            getDailyCounter().count(LANDINGS_COLUMN_NAME + " - age bin " + age,soldThisBin);
            earnings+= soldThisBin *priceForThisBin;
            getDailyCounter().count(EARNINGS_COLUMN_NAME + " - age bin " + age,soldThisBin *priceForThisBin);

            sold+= soldThisBin;
        }

        //give fisher the money
        fisher.earn(earnings);

        //tell regulation
        regulation.reactToSale(species, fisher, sold, earnings, state);

        //return data!
        return new TradeInfo(sold,species,earnings);

    }

    @Override
    public double getMarginalPrice() {
        //simply returns the average price, since we don't really know much better

        return averageHeadPrice;
    }

    @Override
    public void start(FishState state) {
        super.start(state);

        for(int age =0; age<getSpecies().getNumberOfBins();age++) {
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

    public int[] getBinThresholds() {
        return binThresholds;
    }

    public double[] getPricePerSegment() {
        return pricePerSegment;
    }
}
