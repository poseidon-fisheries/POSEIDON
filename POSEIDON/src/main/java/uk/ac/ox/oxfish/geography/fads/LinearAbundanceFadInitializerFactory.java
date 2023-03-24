package uk.ac.ox.oxfish.geography.fads;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.CompressedExponentialAttractionProbability;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FishAbundanceAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LinearFishAbundanceAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Supplier;

public class LinearAbundanceFadInitializerFactory extends AbundanceFadInitializerFactory {
    public LinearAbundanceFadInitializerFactory() {
    }

    public LinearAbundanceFadInitializerFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final String... speciesNames
    ) {
        super(abundanceFiltersFactory, speciesCodesSupplier, speciesNames);
    }

    @NotNull
    @Override
    FishAbundanceAttractor makeFishAttractor(
        final FishState fishState,
        final double[] compressionExponents,
        final double[] attractableBiomassCoefficients,
        final double[] biomassInteractionCoefficients,
        final double[] attractionRates
    ) {
        return new LinearFishAbundanceAttractor(
            fishState.getBiology().getSpecies(),
            new CompressedExponentialAttractionProbability<>(
                compressionExponents,
                attractableBiomassCoefficients,
                biomassInteractionCoefficients
            ),
            attractionRates,
            fishState.getRandom(),
            getAbundanceFiltersFactory().apply(fishState).get(FadSetAction.class)
        );
    }
}
