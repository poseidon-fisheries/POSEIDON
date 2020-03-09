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

package uk.ac.ox.oxfish.fisher.equipment.fads;

import org.apache.commons.collections15.set.ListOrderedSet;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

/**
 * This provides convenience implementations for the various classes that need to access the
 * FadManager instance buried in the fisher's PurseSeineGear.
 */
public interface FadManagerUtils {

    static Optional<Fad> oneOfDeployedFads(Fisher fisher) {
        final ListOrderedSet<Fad> deployedFads = getFadManager(fisher).getDeployedFads();
        return deployedFads.isEmpty() ?
            Optional.empty() :
            Optional.of(oneOf(deployedFads, fisher.grabRandomizer()));
    }

    static FadManager getFadManager(Fisher fisher) {
        if (fisher.getGear() instanceof PurseSeineGear)
            return ((PurseSeineGear) fisher.getGear()).getFadManager();
        else throw new IllegalArgumentException(
            "PurseSeineGear required to get FadManager instance. Fisher " +
                fisher + " is using " + fisher.getGear().getClass() + "."
        );
    }
    static Stream<Fad> fadsHere(Fisher fisher) { return bagToStream(getFadManager(fisher).getFadsHere()); }

    static Stream<Fad> fadsAt(Fisher fisher, SeaTile seaTile) {
        return bagToStream(getFadManager(fisher).getFadMap().fadsAt(seaTile));
    }

    static Collection<Market> getMarkets(Fisher fisher) {
        return fisher.getHomePort().getMarketMap(fisher).getMarkets();
    }

    static double priceOfFishHere(LocalBiology biology, Collection<Market> markets) {
        return markets.stream().mapToDouble(market ->
            biology.getBiomass(market.getSpecies()) * market.getMarginalPrice()
        ).sum();
    }

}
