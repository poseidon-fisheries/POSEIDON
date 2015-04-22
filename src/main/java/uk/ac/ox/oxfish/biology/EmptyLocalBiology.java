package uk.ac.ox.oxfish.biology;

/**
 * Just a biology that has 0 biomass of everything
 * Created by carrknight on 4/11/15.
 */
public class EmptyLocalBiology implements LocalBiology
{

    /**
     * the biomass is 0 for everything
     *
     * @param specie the specie you care about
     * @return the biomass of this specie
     */
    @Override
    public Double getBiomass(Specie specie) {
        return 0d;
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param specie        the specie fished
     * @param biomassFished the biomass fished
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Specie specie, Double biomassFished) {
        throw new IllegalStateException("It's impossible to take biomass from the empty biology");
    }
}
