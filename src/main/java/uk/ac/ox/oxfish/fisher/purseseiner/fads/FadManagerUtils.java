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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.Collection;
import java.util.Optional;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.stream;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;

/**
 * This provides convenience implementations for the various classes that need to access the
 * FadManager instance buried in the fisher's PurseSeineGear.
 */
public interface FadManagerUtils {

    static Stream<Fad> fadsHere(Fisher fisher) { return getFadManager(fisher).getFadsHere(); }

    static FadManager getFadManager(Fisher fisher) {
        return maybeGetFadManager(fisher).orElseThrow(() -> new IllegalArgumentException(
            "PurseSeineGear required to get FadManager instance. Fisher " +
                fisher + " is using " + fisher.getGear().getClass() + "."
        ));
    }

    static Optional<FadManager> maybeGetFadManager(Fisher fisher) {
        return maybeGetPurseSeineGear(fisher).map(PurseSeineGear::getFadManager);
    }

    static Optional<PurseSeineGear> maybeGetPurseSeineGear(Fisher fisher) {
        return Optional
            .of(fisher.getGear())
            .filter(gear -> gear instanceof PurseSeineGear)
            .map(gear -> (PurseSeineGear) gear);
    }

    static PurseSeineGear getPurseSeineGear(Fisher fisher) {
        return maybeGetPurseSeineGear(fisher).orElseThrow(() -> new IllegalArgumentException(
            "PurseSeineGear not available. Fisher " +
                fisher + " is using " + fisher.getGear().getClass() + "."
        ));
    }

    static Stream<Fad> fadsAt(Fisher fisher, SeaTile seaTile) {
        return bagToStream(getFadManager(fisher).getFadMap().fadsAt(seaTile));
    }

    static Collection<Market> getMarkets(Fisher fisher) {
        return fisher.getHomePort().getMarketMap(fisher).getMarkets();
    }

    static double priceOfFishHere(LocalBiology biology, Iterable<Market> markets) {
        return priceOfFishHere(biology, LocalBiology::getBiomass, markets);
    }

    static <T> double priceOfFishHere(
        T biomassContainer,
        ToDoubleBiFunction<T, Species> biomassExtractor,
        Iterable<Market> markets
    ) {
        return stream(markets).mapToDouble(market ->
            biomassExtractor.applyAsDouble(biomassContainer, market.getSpecies()) * market.getMarginalPrice()
        ).sum();
    }

    static double priceOfFishHere(Catch catchesKept, Iterable<Market> markets) {
        return priceOfFishHere(catchesKept, Catch::getWeightCaught, markets);
    }

}
