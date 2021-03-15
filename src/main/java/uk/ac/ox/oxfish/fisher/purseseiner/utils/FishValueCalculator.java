/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.market.MarketMap;

public class FishValueCalculator {

    private final MarketMap marketMap;

    public FishValueCalculator(Fisher fisher) {
        this(fisher.getHomePort().getMarketMap(fisher));
    }

    public FishValueCalculator(MarketMap marketMap) {
        this.marketMap = marketMap;
    }

    public double valueOf(Catch catchesKept) {
        return valueOf(catchesKept.getBiomassArray());
    }

    public double valueOf(double[] biomass) {
        double sum = 0.0;
        for (int i = 0; i < biomass.length; i++)
            sum += biomass[i] * marketMap.getMarket(i).getMarginalPrice();
        return sum;
    }

    public double valueOf(VariableBiomassBasedBiology biology) {
        return valueOf(biology.getCurrentBiomass());
    }


}
