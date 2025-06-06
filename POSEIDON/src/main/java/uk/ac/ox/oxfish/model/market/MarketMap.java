/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Arrays;
import java.util.Collection;

/**
 * A simple map Species ---> Market
 * Created by carrknight on 5/3/15.
 */
public class MarketMap {

    private final Market[] marketList;

    public MarketMap(final GlobalBiology biology) {
        marketList = new Market[biology.getSize()];
    }

    public void addMarket(final Species species, final Market market) {
        Preconditions.checkArgument(marketList[species.getIndex()] == null);
        marketList[species.getIndex()] = market;
        market.setSpecies(species);
    }

    public Market getMarket(final Species species) {
        return marketList[species.getIndex()];
    }

    public Market getMarket(final int speciesIndex) {
        return marketList[speciesIndex];
    }

    public TradeInfo sellFish(
        final Hold hold, final Species species, final Fisher fisher,
        final Regulation regulation, final FishState state
    ) {
        return marketList[species.getIndex()].sellFish(hold, fisher, regulation, state, species);
    }

    public double getSpeciesPrice(final int speciesIndex) {
        return marketList[speciesIndex].getMarginalPrice();
    }

    public double[] getPrices() {
        return Arrays.stream(marketList).mapToDouble(Market::getMarginalPrice).toArray();
    }

    public Collection<Market> getMarkets() {
        return Arrays.asList(marketList);
    }

}
