package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;

@FunctionalInterface
public interface FadInitializer<B extends LocalBiology, F extends Fad<B, F>> {

    F makeFad(
        final FadManager fadManager,
        Fisher owner,
        SeaTile initialLocation,
        MersenneTwisterFast rng
    );

}
