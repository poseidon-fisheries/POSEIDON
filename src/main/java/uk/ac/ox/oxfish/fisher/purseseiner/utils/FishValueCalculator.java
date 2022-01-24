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

import static com.google.common.collect.Streams.stream;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.market.MarketMap;

public class FishValueCalculator {

    private final MarketMap marketMap;
    private final GlobalBiology globalBiology;

    public FishValueCalculator(final Fisher fisher) {
        this(
            fisher.getHomePort().getMarketMap(fisher),
            fisher.grabState().getBiology()
        );
    }

    public FishValueCalculator(
        final MarketMap marketMap,
        final GlobalBiology globalBiology
    ) {
        this.marketMap = marketMap;
        this.globalBiology = globalBiology;
    }

    public double valueOf(final Catch catchesKept) {
        return valueOf(catchesKept.getBiomassArray());
    }

    public double valueOf(final double[] biomass) {
        double sum = 0.0;
        for (int i = 0; i < biomass.length; i++) {
            sum += biomass[i] * marketMap.getMarket(i).getMarginalPrice();
        }
        return sum;
    }

    public double valueOf(final LocalBiology biology) {
        final double[] biomass =
            globalBiology.getSpecies().stream()
                .mapToDouble(biology::getBiomass)
                .toArray();
        return valueOf(biomass);
    }

}
