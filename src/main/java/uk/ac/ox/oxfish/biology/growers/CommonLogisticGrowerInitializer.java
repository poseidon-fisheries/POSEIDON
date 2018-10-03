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


    public CommonLogisticGrowerInitializer(DoubleParameter steepness, double distributeProportionally) {
        this.steepness = steepness;
        this.distributionalWeight = distributeProportionally;
    }

    @Override
    public void initializeGrower(
            Map<SeaTile, BiomassLocalBiology> tiles, FishState state, MersenneTwisterFast random, Species species)
    {

        Collection<BiomassLocalBiology> biologies = tiles.values();
        if(biologies.isEmpty())
            return;
        //initialize the malthusian parameter

        CommonLogisticGrower grower = new CommonLogisticGrower(
                steepness.apply(random),
                species, distributionalWeight);

        //add all the biologies
        for(BiomassLocalBiology biology : biologies)
            grower.getBiologies().add(biology);
        state.registerStartable(grower);

    }
}
