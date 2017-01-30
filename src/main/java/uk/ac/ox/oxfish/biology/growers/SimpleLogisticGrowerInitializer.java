package uk.ac.ox.oxfish.biology.growers;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LogisticLocalBiology;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Collection;
import java.util.Map;

/**
 * Creates ONE IndependentLogisticBiomassGrower and assigns every logisticLocalBiology to it.
 * Created by carrknight on 1/31/17.
 */
public class SimpleLogisticGrowerInitializer implements LogisticGrowerInitializer {


    private final DoubleParameter steepness;


    public SimpleLogisticGrowerInitializer(DoubleParameter steepness) {
        this.steepness = steepness;
    }

    @Override
    public void initializeGrower(
            Map<SeaTile, LogisticLocalBiology> tiles, FishState state, MersenneTwisterFast random) {
        Collection<LogisticLocalBiology> biologies = tiles.values();
        if(biologies.isEmpty())
            return;
        //initialize the malthusian parameter
        int dimension = biologies.iterator().next().getCurrentBiomass().length;
        Double[] malthusian = new Double[dimension];
        for(int i=0 ;i<dimension; i++)
            malthusian[i]=steepness.apply(random);

        IndependentLogisticBiomassGrower grower = new IndependentLogisticBiomassGrower(malthusian);

        //add all the biologies
        for(LogisticLocalBiology biology : biologies)
            grower.getBiologies().add(biology);
        state.registerStartable(grower);

    }
}
