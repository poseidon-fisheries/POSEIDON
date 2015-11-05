package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.model.FishState;

/**
 * A local biology where multiple species exist. Much like ConstantLocalBiology they do not change their
 * total biomass even after being fished out
 * Created by carrknight on 5/6/15.
 */
public class ConstantHeterogeneousLocalBiology implements LocalBiology {

    private final double[] biomasses;

    public ConstantHeterogeneousLocalBiology(double... biomasses) {
        this.biomasses = biomasses;
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species) {
        return biomasses[species.getIndex()];
    }

    /**
     *  nothing
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Species species, Double biomassFished) {

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
