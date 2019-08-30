package uk.ac.ox.oxfish.biology.growers;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

public class FadAwareCommonLogisticGrowerInitializer implements LogisticGrowerInitializer {

    private final DoubleParameter malthusianParameter;
    private final double distributionalWeight;

    FadAwareCommonLogisticGrowerInitializer(
        DoubleParameter malthusianParameter,
        double distributeProportionally
    ) {
        this.malthusianParameter = malthusianParameter;
        this.distributionalWeight = distributeProportionally;
    }

    @Override
    public void initializeGrower(
        Map<SeaTile, BiomassLocalBiology> tiles,
        FishState state,
        MersenneTwisterFast random,
        Species species
    ) {
        CommonLogisticGrower grower = new FadAwareCommonLogisticGrower(
            malthusianParameter.apply(random),
            species,
            distributionalWeight,
            ImmutableList.copyOf(tiles.values())
        );
        state.registerStartable(grower);
    }
}
