package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FishBiomassAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LogisticFishBiomassAttractor;
import uk.ac.ox.oxfish.model.FishState;

public class BiomassFadInitializerFactory
    extends FadInitializerFactory<BiomassLocalBiology, BiomassFad> {

    /**
     * Empty constructor for YAML
     */
    @SuppressWarnings("unused")
    public BiomassFadInitializerFactory() {
    }

    public BiomassFadInitializerFactory(final String... speciesNames) {
        super(speciesNames);
    }

    @Override
    public BiomassFadInitializer apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        final double totalCarryingCapacity = getTotalCarryingCapacity().apply(rng);
        return new BiomassFadInitializer(
            fishState.getBiology(),
            totalCarryingCapacity,
            makeFishAttractor(fishState, rng),
            getFishReleaseProbabilityInPercent().apply(rng) / 100d,
            fishState::getStep
        );
    }

    private FishBiomassAttractor makeFishAttractor(
        final FishState fishState,
        final MersenneTwisterFast rng
    ) {
        return new LogisticFishBiomassAttractor(
            fishState.getRandom(),
            processParameterMap(getCompressionExponents(), fishState.getBiology(), rng),
            processParameterMap(getAttractableBiomassCoefficients(), fishState.getBiology(), rng),
            processParameterMap(getBiomassInteractionsCoefficients(), fishState.getBiology(), rng),
            processParameterMap(getGrowthRates(), fishState.getBiology(), rng)
        );
    }

}
