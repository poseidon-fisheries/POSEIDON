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

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;

/**
 * This provides convenience implementations for the various classes that need to access the
 * FadManager instance buried in the fisher's PurseSeineGear.
 */
public interface FadManagerUtils {

    static Stream<Fad> fadsHere(Fisher fisher) { return bagToStream(getFadManager(fisher).getFadsHere()); }

    static FadManager getFadManager(Fisher fisher) {
        return maybeGetFadManager(fisher).orElseThrow(() -> new IllegalArgumentException(
            "PurseSeineGear required to get FadManager instance. Fisher " +
                fisher + " is using " + fisher.getGear().getClass() + "."
        ));
    }

    static Optional<FadManager> maybeGetFadManager(Fisher fisher) {
        return Optional
            .of(fisher.getGear())
            .filter(gear -> gear instanceof PurseSeineGear)
            .map(gear -> ((PurseSeineGear) fisher.getGear()).getFadManager());
    }

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
