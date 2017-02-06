package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Function;

/**
 * An exogenous local biology object that, when asked for biomass available just calls a previously given function
 * to return the answer. Doesn't track biomass fished.
 * Created by carrknight on 2/6/17.
 */
public class GetterLocalBiology extends AbstractBiomassBasedBiology {


    /**
     * species being fished
     */
    private final Species species;

    /**
     * the function that actually returns the amount of biomass available
     */
    private final Function<FishState,Double> biomass;

    /**
     * the model
     */
    private FishState state;



    public GetterLocalBiology(
            Species species, Function<FishState, Double> biomass) {
        this.species = species;
        this.biomass = biomass;
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
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        this.state = null;
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species) {

        if(species==this.species)
            return Math.max(0,biomass.apply(state));
        return
                0d;
    }

    /**
     * ignored
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Species species, Double biomassFished) {

        //ignored
    }


    /**
     * Getter for property 'species'.
     *
     * @return Value for property 'species'.
     */
    public Species getSpecies() {
        return species;
    }

    /**
     * Getter for property 'biomass'.
     *
     * @return Value for property 'biomass'.
     */
    public Function<FishState, Double> getBiomassFunction() {
        return biomass;
    }
}
