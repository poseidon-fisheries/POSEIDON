package uk.ac.ox.oxfish.biology.complicated;

import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;

/**
 * a natural process that actually does nothing
 * Created by carrknight on 3/22/17.
 */
public class MockNaturalProcess extends SingleSpeciesNaturalProcesses {
    public MockNaturalProcess(
            Species species) {
        super(null, null, species, null);
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
     */
    @Override
    public boolean add(AbundanceBasedLocalBiology abundanceBasedLocalBiology) {
        return true;
    }

    @Override
    public int getLastRecruits() {
        return 0;
    }

    /**
     * Getter for property 'fixedRecruitmentWeight'.
     *
     * @return Value for property 'fixedRecruitmentWeight'.
     */
    @Override
    public HashMap<AbundanceBasedLocalBiology, Double> getFixedRecruitmentWeight() {
        return super.getFixedRecruitmentWeight();
    }

    /**
     * Setter for property 'fixedRecruitmentWeight'.
     *
     * @param fixedRecruitmentWeight Value to set for property 'fixedRecruitmentWeight'.
     */
    @Override
    public void setFixedRecruitmentWeight(
            HashMap<AbundanceBasedLocalBiology, Double> fixedRecruitmentWeight) {
    }

    /**
     * give a function to generate noise as % of recruits this year
     *
     * @param noiseMaker the function that generates percentage changes. 1 means no noise.
     */
    @Override
    public void addNoise(NoiseMaker noiseMaker) {
    }
}
