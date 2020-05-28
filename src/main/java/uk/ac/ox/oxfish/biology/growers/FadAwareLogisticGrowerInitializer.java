package uk.ac.ox.oxfish.biology.growers;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

public class FadAwareLogisticGrowerInitializer implements LogisticGrowerInitializer {

    private DoubleParameter malthusianParameter;
    private DoubleParameter distributionalWeight;
    private boolean useLastYearBiomass;

    FadAwareLogisticGrowerInitializer(
        final DoubleParameter malthusianParameter,
        final DoubleParameter distributionalWeight,
        final boolean useLastYearBiomass
    ) {
        this.malthusianParameter = malthusianParameter;
        this.distributionalWeight = distributionalWeight;
        this.useLastYearBiomass = useLastYearBiomass;
    }

    public DoubleParameter getMalthusianParameter() { return malthusianParameter; }

    public void setMalthusianParameter(final DoubleParameter malthusianParameter) {
        this.malthusianParameter = malthusianParameter;
    }

    public DoubleParameter getDistributionalWeight() { return distributionalWeight; }

    public void setDistributionalWeight(final DoubleParameter distributionalWeight) {
        this.distributionalWeight = distributionalWeight;
    }

    public boolean isUseLastYearBiomass() { return useLastYearBiomass; }

    public void setUseLastYearBiomass(final boolean useLastYearBiomass) {
        this.useLastYearBiomass = useLastYearBiomass;
    }

    @Override
    public void initializeGrower(
        Map<SeaTile, BiomassLocalBiology> tiles,
        FishState state,
        MersenneTwisterFast random,
        Species species
    ) {
        state.registerStartable(new FadAwareLogisticGrower(
            species,
            malthusianParameter.apply(random),
            distributionalWeight.apply(random),
            useLastYearBiomass,
            tiles.values()
        ));
    }

}
