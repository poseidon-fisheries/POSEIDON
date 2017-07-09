package uk.ac.ox.oxfish.biology.complicated;

import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * a natural process that actually does nothing
 * Created by carrknight on 3/22/17.
 */
public class MockNaturalProcess extends SingleSpeciesNaturalProcesses {
    public MockNaturalProcess(
            Species species) {
        super(null, null, species, null, new NoAbundanceDiffusion() );
    }


    /**
     * schedules itself every year
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
    }

    /**
     * Recruitment + Mortality + Aging + Allocation of new Recruits <br>
     * New recruits are allocated proportional to the areas that have more biomass.
     *
     * @param simState
     */
    @Override
    public void step(SimState simState) {
    }

    /**
     * register this biology so that it can be accessed by recruits and so on
     *
     * @param abundanceBasedLocalBiology
     * @param tile
     */
    @Override
    public void add(AbundanceBasedLocalBiology abundanceBasedLocalBiology, SeaTile tile) {
    }

    @Override
    public int getLastRecruits() {
        return 0;
    }


    /**
     * give a function to generate noise as % of recruits this year
     *
     * @param noiseMaker the function that generates percentage changes. 1 means no noise.
     */
    @Override
    public void addNoise(NoiseMaker noiseMaker) {
    }

    /**
     * Getter for property 'diffuser'.
     *
     * @return Value for property 'diffuser'.
     */
    @Override
    public AbundanceDiffuser getDiffuser() {
        return null;
    }


    /**
     * Getter for property 'recruitsAllocator'.
     *
     * @return Value for property 'recruitsAllocator'.
     */
    @Override
    public BiomassAllocator getRecruitsAllocator() {
        return null;
    }

    /**
     * Setter for property 'recruitsAllocator'.
     *
     * @param recruitsAllocator Value to set for property 'recruitsAllocator'.
     */
    @Override
    public void setRecruitsAllocator(BiomassAllocator recruitsAllocator) {
    }
}
