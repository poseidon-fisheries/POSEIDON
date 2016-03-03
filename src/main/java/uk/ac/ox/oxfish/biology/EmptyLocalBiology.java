package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.model.FishState;

/**
 * Just a biology that has 0 biomass of everything
 * Created by carrknight on 4/11/15.
 */
public class EmptyLocalBiology implements LocalBiology
{

    /**
     * the biomass is 0 for everything
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species) {
        return 0d;
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param species        the species fished
     * @param biomassFished the biomass fished
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Species species, Double biomassFished) {
        throw new IllegalStateException("It's impossible to take biomass from the empty biology");
    }

    /**
     * Empty array
     */
    @Override
    public int[] getNumberOfMaleFishPerAge(Species species) {
        return new int[0];
    }

    /**
     * Empty array
     */
    @Override
    public int[] getNumberOfFemaleFishPerAge(Species species) {
        return new int[0];
    }

    /**
     * Empty array
     */
    @Override
    public int[] getNumberOfFishPerAge(Species species) {
        return new int[0];
    }


    /**
     * Tells the local biology that a fisher (or something anyway) fished these many fish (grouped by age) from this
     * location
     *  @param species the species fished
     * @param maleCatches the biomass fished
     * @param femaleCatches
     */
    @Override
    public void reactToThisAmountOfFishBeingCaught(Species species, int[] maleCatches, int[] femaleCatches) {
        throw new IllegalStateException("It's impossible to catch fish in the empty biology");
    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }
}
