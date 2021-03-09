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

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.List;
import java.util.function.ToDoubleBiFunction;

public class FishValueCalculator {

    private final List<Market> markets;

    public FishValueCalculator(Fisher fisher) {
        this(fisher.getHomePort().getMarketMap(fisher).getMarkets());
    }

    public FishValueCalculator(final Iterable<Market> markets) {
        this.markets = ImmutableList.copyOf(markets);
    }

    public double valueOf(Catch catchesKept) {
        return valueOf(catchesKept, Catch::getWeightCaught);
    }

    <T> double valueOf(
        T biomassContainer,
        ToDoubleBiFunction<T, Species> biomassExtractor
    ) {
        return markets.stream().mapToDouble(market ->
            biomassExtractor.applyAsDouble(biomassContainer, market.getSpecies()) * market.getMarginalPrice()
        ).sum();
    }

    public double valueOf(LocalBiology biology) {
        return valueOf(biology, LocalBiology::getBiomass);
    }

}
