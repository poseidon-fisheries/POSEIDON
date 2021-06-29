package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.DoubleSummaryStatistics;

/**
 * market with two prices, one below and one above weight limit.
 * If there are multiple subdivisions, the bin price depends on the average weight;
 *
 * mostly delegates to PerBinMarket (set up during the set species)
 */
public class WeightLimitMarket implements MarketWithCounter {


    private final double priceBelowWeightLimit;

    private final double priceAboveWeightLimit;

    private final double weightLimitInKg;

    private  PerBinMarket delegate;


    public WeightLimitMarket(double priceBelowWeightLimit, double priceAboveWeightLimit, double weightLimitInKg) {
        this.priceBelowWeightLimit = priceBelowWeightLimit;
        this.priceAboveWeightLimit = priceAboveWeightLimit;
        this.weightLimitInKg = weightLimitInKg;
    }

    @Override
    public void setSpecies(Species species) {
        double[] pricePerBin = new double[species.getNumberOfBins()];
        for(int age =0; age<species.getNumberOfBins();age++) {
            DoubleSummaryStatistics binWeight = new DoubleSummaryStatistics();
            for (int subdivision = 0; subdivision < species.getNumberOfSubdivisions(); subdivision++) {
                binWeight.accept(species.getWeight(subdivision,age));
            }
            if(binWeight.getAverage()<weightLimitInKg)
                pricePerBin[age] = priceBelowWeightLimit;
            else
                pricePerBin[age] = priceAboveWeightLimit;

        }
        delegate = new PerBinMarket(pricePerBin);
        delegate.setSpecies(species);
    }

    @Override
    public Species getSpecies() {
        return delegate.getSpecies();
    }

    @Override
    public void turnOff() {
        delegate.turnOff();
    }



    @Override
    public TimeSeries<Market> getData() {
        return delegate.getData();
    }

    @Override
    public Counter getDailyCounter() {
        return delegate.getDailyCounter();
    }

    @Override
    public boolean isStarted() {
        return delegate.isStarted();
    }

    @Override
    public TradeInfo sellFish(Hold hold, Fisher fisher, Regulation regulation, FishState state, Species species) {
        return delegate.sellFish(hold, fisher, regulation, state, species);
    }


    @Override
    public double getMarginalPrice() {
        return delegate.getMarginalPrice();
    }

    @Override
    public void start(FishState state) {
        delegate.start(state);
    }
}
