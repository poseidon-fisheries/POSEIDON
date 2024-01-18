package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.*;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.processSpeciesNameToDoubleParameterMap;

public abstract class AbstractCompressedAbundanceFadInitializerFactory
    extends CompressedExponentialFadInitializerFactory<AbundanceLocalBiology, AbundanceAggregatingFad> {

    private AbundanceFiltersFactory abundanceFiltersFactory;

    AbstractCompressedAbundanceFadInitializerFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final DoubleParameter totalCarryingCapacity,
        final String... speciesNames
    ) {
        super(totalCarryingCapacity, speciesNames);
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    /**
     * Empty constructor for YAML
     */
    public AbstractCompressedAbundanceFadInitializerFactory() {
    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad> apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();

        return new AbundanceAggregatingFadInitializer(
            fishState.getBiology(),
            makeFishAttractor(fishState, rng),
            fishState::getStep,
            new GlobalCarryingCapacityInitializer(getTotalCarryingCapacity()),
            processSpeciesNameToDoubleParameterMap(getFishReleaseProbabilities(), fishState.getBiology(), rng)
        );
    }

    private FishAbundanceAttractor makeFishAttractor(
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
        return makeFishAttractor(
            fishState,
            compressionExponents,
            attractableBiomassCoefficients,
            biomassInteractionCoefficients,
            attractionRates
        );
    }

    FishAbundanceAttractor makeFishAttractor(
        final FishState fishState,
        final double[] compressionExponents,
        final double[] attractableBiomassCoefficients,
        final double[] biomassInteractionCoefficients,
        final double[] attractionRates
    ) {
        return new LogisticFishAbundanceAttractor(
            fishState.getBiology().getSpecies(),
            new CompressedExponentialAttractionProbability(
                compressionExponents,
                attractableBiomassCoefficients,
                biomassInteractionCoefficients
            ),
            attractionRates,
            fishState.getRandom(),
            abundanceFiltersFactory.apply(fishState).get(FadSetAction.class)
        );
    }
}



