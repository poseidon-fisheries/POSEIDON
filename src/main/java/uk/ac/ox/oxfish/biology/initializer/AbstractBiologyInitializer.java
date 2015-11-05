package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Basically an abstract class that creates GlobalBiology objects from a list of names; useful for all the simple
 * biologies where species are just names and nothing else
 * Created by carrknight on 11/5/15.
 */
public  abstract class AbstractBiologyInitializer implements BiologyInitializer {

    /**
     * creates the global biology object for the model
     *
     * @param random                the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return a global biology object
     */
    @Override
    public GlobalBiology generateGlobal(MersenneTwisterFast random, FishState modelBeingInitialized) {
        //turn list of names into list of species
        String[] names = getSpeciesNames();
        Species[] speciesArray = new Species[names.length];
        for(int i=0; i< names.length; i++)
        {
            speciesArray[i] = new Species(names[i]);
        }
        return new GlobalBiology(speciesArray);
    }

    /**
     * Get a list of the species with their names. The size of this array determines the size of the model array
     *
     * @return
     */
     abstract public String[] getSpeciesNames();
}
