package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.model.Startable;

/**
 * This is supposed to be a container of biological information we can attach to one or multiple sea tiles (because there
 * will probably be issues with data not having the same resolution as our model). For now it holds just local biomass
 * information
 * Created by carrknight on 4/11/15.
 */
public interface LocalBiology extends Startable
{





    /**
     * the biomass at this location for a single species.
     * @param species  the species you care about
     * @return the biomass of this species
     */
    Double getBiomass(Species species);



    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     * @param species the species fished
     * @param biomassFished the biomass fished
     */
    void reactToThisAmountOfBiomassBeingFished(Species species, Double biomassFished);


}
