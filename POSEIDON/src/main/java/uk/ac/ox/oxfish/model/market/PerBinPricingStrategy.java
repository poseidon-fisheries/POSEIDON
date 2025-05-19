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

package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;

/**
 * very generic price for abundance: each bin is allocated a specific price.
 */
public class PerBinPricingStrategy implements PricingStrategy {

    private final double[] pricePerBin;

    private final double averagePrice;


    public PerBinPricingStrategy(double[] pricePerBin) {
        this.pricePerBin = pricePerBin;
        this.averagePrice = Arrays.stream(pricePerBin).summaryStatistics().getAverage();
    }

    @Override
    public void start(FishState model) {

    }

    @Override
    public double getPricePerKg(Species speciesBeingSold, Fisher seller, int biologicalBin, double quantitySold) {
        return pricePerBin[biologicalBin];
    }

    @Override
    public void reactToSale(TradeInfo info) {

    }

    @Override
    public double getMarginalPrice() {
        return averagePrice;
    }
}
