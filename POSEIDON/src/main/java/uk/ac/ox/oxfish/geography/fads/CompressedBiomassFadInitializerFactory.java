package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.Map;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.processSpeciesNameToDoubleParameterMap;

public class CompressedBiomassFadInitializerFactory
    extends CompressedExponentialFadInitializerFactory<BiomassLocalBiology, BiomassAggregatingFad> {

    /**
     * Empty constructor for YAML
     */
    @SuppressWarnings("unused")
    public CompressedBiomassFadInitializerFactory() {
    }

    public CompressedBiomassFadInitializerFactory(
        final DoubleParameter totalCarryingCapacity,
        final String... speciesNames
    ) {
        super(totalCarryingCapacity, speciesNames);
    }

    public CompressedBiomassFadInitializerFactory(
        final DoubleParameter totalCarryingCapacity,
        final Map<String, DoubleParameter> compressionExponents,
        final Map<String, DoubleParameter> attractableBiomassCoefficients,
        final Map<String, DoubleParameter> biomassInteractionsCoefficients,
        final Map<String, DoubleParameter> growthRates,
        final Map<String, DoubleParameter> fishReleaseProbabilities
    ) {
        super(
            totalCarryingCapacity,
            compressionExponents,
            attractableBiomassCoefficients,
            biomassInteractionsCoefficients,
            growthRates,
            fishReleaseProbabilities
        );
    }

    @Override
    public BiomassFadInitializer apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return new BiomassFadInitializer(
            fishState.getBiology(),
            makeFishAttractor(fishState, rng),
            fishState::getStep,
            new FixedGlobalCarryingCapacitySupplier(getTotalCarryingCapacity().applyAsDouble(rng)),
            processSpeciesNameToDoubleParameterMap(
                getFishReleaseProbabilities(),
                fishState.getBiology(),
                rng
            )
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
