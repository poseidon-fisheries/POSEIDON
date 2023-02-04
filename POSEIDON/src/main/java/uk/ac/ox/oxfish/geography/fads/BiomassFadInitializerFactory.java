package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import java.util.Map;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FishBiomassAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.CompressedExponentialAttractionProbability;
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

    BiomassFadInitializerFactory(final String... speciesNames) {
        super(speciesNames);
    }

    public BiomassFadInitializerFactory(
        final Map<String, Double> compressionExponents,
        final Map<String, Double> attractableBiomassCoefficients,
        final Map<String, Double> biomassInteractionsCoefficients,
        final Map<String, Double> growthRates
    ) {
        super(
            compressionExponents,
            attractableBiomassCoefficients,
            biomassInteractionsCoefficients,
            growthRates
        );
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
        final double[] compressionExponents =
            processParameterMap(
                getCompressionExponents(),
                fishState.getBiology(), rng
            );
        final double[] attractableBiomassCoefficients =
            processParameterMap(
                getAttractableBiomassCoefficients(),
                fishState.getBiology(),
                rng
            );
        final double[] biomassInteractionCoefficients =
            processParameterMap(
                getBiomassInteractionsCoefficients(),
                fishState.getBiology(),
                rng
            );
        final double[] attractionRates =
            processParameterMap(getGrowthRates(), fishState.getBiology(), rng);
        return new LogisticFishBiomassAttractor(
            fishState.getBiology().getSpecies(),
            new CompressedExponentialAttractionProbability<>(
                compressionExponents,
                attractableBiomassCoefficients,
                biomassInteractionCoefficients
            ),
            attractionRates, fishState.getRandom()
        );
    }

}
