package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.*;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractCompressedAbundanceFadInitializerFactory
    extends CompressedExponentialFadInitializerFactory<AbundanceLocalBiology, AbundanceAggregatingFad> {

    private AbundanceFiltersFactory abundanceFiltersFactory;

    public AbstractCompressedAbundanceFadInitializerFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final String... speciesNames
    ) {
        super(speciesCodesSupplier, speciesNames);
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
        checkNotNull(getSpeciesCodesSupplier());
        final MersenneTwisterFast rng = fishState.getRandom();

        return new AbundanceAggregatingFadInitializer(
            fishState.getBiology(),
            makeFishAttractor(fishState, rng),
            getFishReleaseProbabilityInPercent().applyAsDouble(rng) / 100d,
            fishState::getStep,
            new GlobalCarryingCapacityInitializer(0, getTotalCarryingCapacity())
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

    @NotNull
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



