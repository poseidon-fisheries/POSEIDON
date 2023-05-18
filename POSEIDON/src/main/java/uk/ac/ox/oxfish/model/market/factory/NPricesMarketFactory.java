package uk.ac.ox.oxfish.model.market.factory;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.NThresholdsMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedList;
import java.util.List;

public class NPricesMarketFactory implements AlgorithmFactory<NThresholdsMarket> {


    private List<Integer> binThresholds = new LinkedList<>();


    private List<Double> prices = new LinkedList<>();


    @Override
    public NThresholdsMarket apply(FishState state) {
        Preconditions.checkArgument(prices.size() == binThresholds.size() + 1);
        int[] bins = new int[binThresholds.size()];
        for (int i = 0; i < binThresholds.size(); i++) {
            bins[i] = binThresholds.get(i);

        }
        double[] price = new double[prices.size()];
        for (int i = 0; i < prices.size(); i++) {
            price[i] = prices.get(i);

        }

        return new NThresholdsMarket(
            bins,
            price


        );


    }

    public List<Integer> getBinThresholds() {
        return binThresholds;
    }

    public void setBinThresholds(List<Integer> binThresholds) {
        this.binThresholds = binThresholds;
    }

    public List<Double> getPrices() {
        return prices;
    }

    public void setPrices(List<Double> prices) {
        this.prices = prices;
    }
}
