package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;
import java.util.stream.DoubleStream;

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
     * the biomass at this location for a single specie.
     *
     * @param specie the specie you care about
     * @return the biomass of this specie
     */
    @Override
    public Double getBiomass(Specie specie) {
        return biomasses[specie.getIndex()];
    }

    /**
     *  nothing
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Specie specie, Double biomassFished) {

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
