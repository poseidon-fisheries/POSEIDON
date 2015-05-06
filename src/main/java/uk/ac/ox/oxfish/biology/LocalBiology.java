package uk.ac.ox.oxfish.biology;

import java.util.HashMap;

/**
 * This is supposed to be a container of biological information we can attach to one or multiple sea tiles (because there
 * will probably be issues with data not having the same resolution as our model). For now it holds just local biomass
 * information
 * Created by carrknight on 4/11/15.
 */
public interface LocalBiology
{





    /**
     * the biomass at this location for a single specie.
     * @param specie  the specie you care about
     * @return the biomass of this specie
     */
    public Double getBiomass(Specie specie);


    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     * @param specie the specie fished
     * @param biomassFished the biomass fished
     */
    public void reactToThisAmountOfBiomassBeingFished(Specie specie, Double biomassFished);


}
