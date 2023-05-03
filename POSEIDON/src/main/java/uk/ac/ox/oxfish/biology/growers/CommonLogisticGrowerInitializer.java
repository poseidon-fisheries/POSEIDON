package uk.ac.ox.oxfish.biology.growers;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Collection;
import java.util.Map;

public class CommonLogisticGrowerInitializer implements LogisticGrowerInitializer {

    private final DoubleParameter steepness;
    private final double distributionalWeight;


    public CommonLogisticGrowerInitializer(final DoubleParameter steepness, final double distributeProportionally) {
        this.steepness = steepness;
        this.distributionalWeight = distributeProportionally;
    }

    @Override
    public void initializeGrower(
        final Map<SeaTile, BiomassLocalBiology> tiles,
        final FishState state,
        final MersenneTwisterFast random,
        final Species species
    ) {

        final Collection<BiomassLocalBiology> biologies = tiles.values();
        if (biologies.isEmpty())
            return;
        //initialize the malthusian parameter

        final CommonLogisticGrower grower = new CommonLogisticGrower(
            steepness.applyAsDouble(random),
            species,
            distributionalWeight
        );

        //add all the biologies
        for (final BiomassLocalBiology biology : biologies)
            grower.getBiologies().add(biology);
        state.registerStartable(grower);

    }
}
