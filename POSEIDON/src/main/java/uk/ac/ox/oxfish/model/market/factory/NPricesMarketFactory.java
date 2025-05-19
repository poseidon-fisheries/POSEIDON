/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
