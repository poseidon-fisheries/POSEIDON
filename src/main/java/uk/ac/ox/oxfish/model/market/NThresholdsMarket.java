package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

/**
 * basically a generalization of the "ThreePricesMarket":
 * depending on which bin you are, you get a different price
 */
public class NThresholdsMarket implements MarketWithCounter {

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


    private  PerBinMarket delegate;

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

    @Override
    public void setSpecies(Species species) {
        double[] pricePerBin = new double[species.getNumberOfBins()];
        for(int age =0; age<species.getNumberOfBins();age++) {
            //look for the correct bin
            pricePerBin[age] = pricePerSegment[0];
            for (int i = 0; i < binThresholds.length; i++) {
                if (age > binThresholds[i])
                    pricePerBin[age] = pricePerSegment[i + 1];
                else
                    break;
            }
        }
        delegate = new PerBinMarket(pricePerBin);
        delegate.setSpecies(species);
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
    public TradeInfo sellFish(Hold hold, Fisher fisher, Regulation regulation, FishState state, Species species) {
        return delegate.sellFish(hold, fisher, regulation, state, species);
    }

    @Override
    public TimeSeries<Market> getData() {
        return delegate.getData();
    }

    @Override
    public Species getSpecies() {
        return delegate.getSpecies();
    }

    @Override
    public boolean isStarted() {
        return delegate.isStarted();
    }


    @Override
    public double getMarginalPrice() {
        //simply returns the average price, since we don't really know much better

        return delegate.getMarginalPrice();
    }

    @Override
    public void start(FishState state) {
        delegate.start(state);
    }

    public int[] getBinThresholds() {
        return binThresholds;
    }

    public double[] getPricePerSegment() {
        return pricePerSegment;
    }


    @Override
    public Counter getDailyCounter() {
        return delegate.getDailyCounter();
    }
}
