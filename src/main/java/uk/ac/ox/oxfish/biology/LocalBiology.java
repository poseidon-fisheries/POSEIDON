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

    /**
     * Tells the local biology that a fisher (or something anyway) fished these many fish (grouped by age) from this
     * location
     * @param species the species fished
     * @param maleCatches the biomass fished
     * @param femaleCatches
     */
    void reactToThisAmountOfFishBeingCaught(Species species, int[] maleCatches, int[] femaleCatches);

    /**
     * returns the number of male fish in this seatile belonging to this species, split into age cohorts
     * @param species the species examined
     * @return the male fish array.
     */
    int[] getNumberOfMaleFishPerAge(Species species);

    /**
     * returns the number of female fish in this seatile belonging to this species, split into age cohorts
     * @param species the species examined
     * @return the female fish array.
     */
    int[] getNumberOfFemaleFishPerAge(Species species);

    /**
     * returns the number of fish in this seatile belonging to this species, split into age cohorts
     * @param species the species examined
     * @return the fish array.
     */
    int[] getNumberOfFishPerAge(Species species);



}
