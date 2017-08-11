package uk.ac.ox.oxfish.biology;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

/**
 * decorates any local biology by intercepting calls to "reactToxBeingFished" but only up to a certain year
 */
public class UnfishableLocalBiologyDecorator implements LocalBiology {

    private final int yearAfterWhichBiomassStopsBeingProtected;

    /**
     * reference needed to check the time
     */
    private FishState state;

    private final LocalBiology decorated;

    public UnfishableLocalBiologyDecorator(
            int yearAfterWhichBiomassStopsBeingProtected, LocalBiology decorated) {
        this.yearAfterWhichBiomassStopsBeingProtected = yearAfterWhichBiomassStopsBeingProtected;
        this.decorated = decorated;
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species) {
        return decorated.getBiomass(species);
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param caught       the amount fished out of the sea
     * @param notDiscarded the amount retained in the boat (not thrown out)
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
            Catch caught, Catch notDiscarded, GlobalBiology biology) {

        Preconditions.checkArgument(state!=null, "Not started or already turned off!");
        if(state.getYear()>=yearAfterWhichBiomassStopsBeingProtected)
            decorated.reactToThisAmountOfBiomassBeingFished(caught, notDiscarded, biology);

    }

    /**
     * returns the number of male fish in this seatile belonging to this species, split into age cohorts
     *
     * @param species the species examined
     * @return the male fish array.
     */
    @Override
    public int[] getNumberOfMaleFishPerAge(Species species) {
        return decorated.getNumberOfMaleFishPerAge(species);
    }

    /**
     * returns the number of female fish in this seatile belonging to this species, split into age cohorts
     *
     * @param species the species examined
     * @return the female fish array.
     */
    @Override
    public int[] getNumberOfFemaleFishPerAge(Species species) {
        return decorated.getNumberOfFemaleFishPerAge(species);
    }

    /**
     * returns the number of fish in this seatile belonging to this species, split into age cohorts
     *
     * @param species the species examined
     * @return the fish array.
     */
    @Override
    public int[] getNumberOfFishPerAge(Species species) {
        return decorated.getNumberOfFishPerAge(species);

    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        this.state = model;
        decorated.start(model);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        this.state = null;
        decorated.turnOff();
    }
}
