package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.model.FishState;

public class BiomassFadInitializerFactory
    extends FadInitializerFactory<BiomassLocalBiology, BiomassFad> {

    @Override
    public BiomassFadInitializer apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        final double totalCarryingCapacity = getTotalCarryingCapacity().apply(rng);
        return new BiomassFadInitializer(
            fishState.getBiology(),
            totalCarryingCapacity,
            makeFishAttractor(fishState, rng, totalCarryingCapacity),
            getFishReleaseProbabilityInPercent().apply(rng) / 100d,
            fishState::getStep
        );
    }

}
