package uk.ac.ox.oxfish.geography.fads;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.CompressedExponentialAttractionProbability;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FishAbundanceAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LinearFishAbundanceAttractor;
import uk.ac.ox.oxfish.model.FishState;

public class LinearAbundanceFadInitializerFactory extends AbundanceFadInitializerFactory {
    public LinearAbundanceFadInitializerFactory() {
    }

    public LinearAbundanceFadInitializerFactory(String... speciesNames) {
        super(speciesNames);
    }

    @NotNull
    @Override
    FishAbundanceAttractor makeFishAttractor(
        FishState fishState,
        double[] compressionExponents,
        double[] attractableBiomassCoefficients,
        double[] biomassInteractionCoefficients,
        double[] attractionRates
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
            getSelectivityFilters()
        );
    }
}
