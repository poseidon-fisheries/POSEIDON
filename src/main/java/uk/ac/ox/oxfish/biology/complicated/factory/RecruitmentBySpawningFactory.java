package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomass;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomassDelayed;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/8/17.
 */
public class RecruitmentBySpawningFactory  implements AlgorithmFactory<RecruitmentProcess>{

    /**
     * the number of recruits you'd get in a "virgin" state.
     */
    private DoubleParameter virginRecruits = new FixedDoubleParameter(40741397);



    /**
     * logistic growth parameter
     */
    private DoubleParameter steepness = new FixedDoubleParameter(0.6);


    private DoubleParameter cumulativePhi = new FixedDoubleParameter(14.2444066771724);

    /**
     * if true the spawning biomass counts relative fecundity (this is true for yelloweye rockfish)
     */
    private boolean  addRelativeFecundityToSpawningBiomass = false;

    /**
     * whether there is a delay between the recruit being computed and they actually being the recruits for that year
     */
    private DoubleParameter yearlyDelay = new FixedDoubleParameter(0);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RecruitmentProcess apply(FishState state) {
        int delay = yearlyDelay.apply(state.getRandom()).intValue();
        if(delay<=0)
            return new RecruitmentBySpawningBiomass(
                    virginRecruits.apply(state.getRandom()).intValue(),
                    steepness.apply(state.getRandom()),
                    cumulativePhi.apply(state.getRandom()),
                    addRelativeFecundityToSpawningBiomass
            );
        else
            return new RecruitmentBySpawningBiomassDelayed(
                    virginRecruits.apply(state.getRandom()).intValue(),
                    steepness.apply(state.getRandom()),
                    cumulativePhi.apply(state.getRandom()),

                    addRelativeFecundityToSpawningBiomass,
                    delay);
    }

    /**
     * Getter for property 'virginRecruits'.
     *
     * @return Value for property 'virginRecruits'.
     */
    public DoubleParameter getVirginRecruits() {
        return virginRecruits;
    }

    /**
     * Setter for property 'virginRecruits'.
     *
     * @param virginRecruits Value to set for property 'virginRecruits'.
     */
    public void setVirginRecruits(DoubleParameter virginRecruits) {
        this.virginRecruits = virginRecruits;
    }

    /**
     * Getter for property 'steepness'.
     *
     * @return Value for property 'steepness'.
     */
    public DoubleParameter getSteepness() {
        return steepness;
    }

    /**
     * Setter for property 'steepness'.
     *
     * @param steepness Value to set for property 'steepness'.
     */
    public void setSteepness(DoubleParameter steepness) {
        this.steepness = steepness;
    }

    /**
     * Getter for property 'addRelativeFecundityToSpawningBiomass'.
     *
     * @return Value for property 'addRelativeFecundityToSpawningBiomass'.
     */
    public boolean isAddRelativeFecundityToSpawningBiomass() {
        return addRelativeFecundityToSpawningBiomass;
    }

    /**
     * Setter for property 'addRelativeFecundityToSpawningBiomass'.
     *
     * @param addRelativeFecundityToSpawningBiomass Value to set for property 'addRelativeFecundityToSpawningBiomass'.
     */
    public void setAddRelativeFecundityToSpawningBiomass(boolean addRelativeFecundityToSpawningBiomass) {
        this.addRelativeFecundityToSpawningBiomass = addRelativeFecundityToSpawningBiomass;
    }

    /**
     * Getter for property 'cumulativePhi'.
     *
     * @return Value for property 'cumulativePhi'.
     */
    public DoubleParameter getCumulativePhi() {
        return cumulativePhi;
    }

    /**
     * Setter for property 'cumulativePhi'.
     *
     * @param cumulativePhi Value to set for property 'cumulativePhi'.
     */
    public void setCumulativePhi(DoubleParameter cumulativePhi) {
        this.cumulativePhi = cumulativePhi;
    }

    /**
     * Getter for property 'yearlyDelay'.
     *
     * @return Value for property 'yearlyDelay'.
     */
    public DoubleParameter getYearlyDelay() {
        return yearlyDelay;
    }

    /**
     * Setter for property 'yearlyDelay'.
     *
     * @param yearlyDelay Value to set for property 'yearlyDelay'.
     */
    public void setYearlyDelay(DoubleParameter yearlyDelay) {
        this.yearlyDelay = yearlyDelay;
    }
}
