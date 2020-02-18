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

    static FadManager getFadManager(Fisher fisher) {
        if (fisher.getGear() instanceof PurseSeineGear)
            return ((PurseSeineGear) fisher.getGear()).getFadManager();
        else throw new IllegalArgumentException(
            "PurseSeineGear required to get FadManager instance. Fisher " +
                fisher + " is using " + fisher.getGear().getClass() + "."
        );
    }

    static Optional<Fad> oneOfFadsHere(Fisher fisher) {
        return getFadManager(fisher).oneOfFadsHere();
    }

    static Optional<Fad> oneOfDeployedFads(Fisher fisher) {
        final ListOrderedSet<Fad> deployedFads = getFadManager(fisher).getDeployedFads();
        return deployedFads.isEmpty() ?
            Optional.empty() :
            Optional.of(oneOf(deployedFads, fisher.grabRandomizer()));
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
