package uk.ac.ox.oxfish.geography.fads;

import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FishAttractor;

import java.util.function.Function;

public interface FadInitializer<B extends LocalBiology, F extends Fad<B, F>> extends Function<FadManager<B, F>, F> {


    B makeBiology(GlobalBiology globalBiology);

    F makeFad(
            FadManager<B, F> owner,
            B biology,
            FishAttractor<B, F> fishAttractor,
            double fishReleaseProbability,
            int stepDeployed,
            Int2D locationDeployed
    );

    double generateCarryingCapacity();
}
