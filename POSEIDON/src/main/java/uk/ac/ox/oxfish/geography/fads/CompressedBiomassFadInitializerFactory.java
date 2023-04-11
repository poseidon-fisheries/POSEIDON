package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.*;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;
import java.util.function.Supplier;

public class CompressedBiomassFadInitializerFactory
    extends CompressedExponentialFadInitializerFactory<BiomassLocalBiology, BiomassAggregatingFad> {

    /**
     * Empty constructor for YAML
     */
    @SuppressWarnings("unused")
    public CompressedBiomassFadInitializerFactory() {
    }

    public CompressedBiomassFadInitializerFactory(
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final String... speciesNames
    ) {
        super(speciesCodesSupplier, speciesNames);
    }

    public CompressedBiomassFadInitializerFactory(
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final Map<String, Double> compressionExponents,
        final Map<String, Double> attractableBiomassCoefficients,
        final Map<String, Double> biomassInteractionsCoefficients,
        final Map<String, Double> growthRates
    ) {
        super(
            speciesCodesSupplier,
            compressionExponents,
            attractableBiomassCoefficients,
            biomassInteractionsCoefficients,
            growthRates
        );
    }

    @Override
    public BiomassFadInitializer apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        final double totalCarryingCapacity = getTotalCarryingCapacity().applyAsDouble(rng);
        return new BiomassFadInitializer(
            fishState.getBiology(),
            makeFishAttractor(fishState, rng),
            getFishReleaseProbabilityInPercent().applyAsDouble(rng) / 100d,
            fishState::getStep,
            new GlobalCarryingCapacityInitializer(0, getTotalCarryingCapacity())
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
            new CompressedExponentialAttractionProbability(
                compressionExponents,
                attractableBiomassCoefficients,
                biomassInteractionCoefficients
            ),
            attractionRates, fishState.getRandom()
        );
    }

}
