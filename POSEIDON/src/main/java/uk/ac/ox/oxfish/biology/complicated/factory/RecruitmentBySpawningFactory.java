/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomass;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomassDelayed;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;

/**
 * Created by carrknight on 7/8/17.
 */
public class RecruitmentBySpawningFactory implements AlgorithmFactory<RecruitmentProcess> {

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
    private boolean addRelativeFecundityToSpawningBiomass = false;

    /**
     * whether there is a delay between the recruit being computed and they actually being the recruits for that year
     */
    private DoubleParameter yearlyDelay = new FixedDoubleParameter(0);


    private double[] maturity = new double[2];


    private double[] relativeFecundity = null;

    private int femaleSubdivisionIndex = FEMALE;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RecruitmentProcess apply(final FishState state) {
        final int delay = (int) yearlyDelay.applyAsDouble(state.getRandom());
        if (delay <= 0)
            return new RecruitmentBySpawningBiomass(
                (int) virginRecruits.applyAsDouble(state.getRandom()),
                steepness.applyAsDouble(state.getRandom()),
                cumulativePhi.applyAsDouble(state.getRandom()),
                addRelativeFecundityToSpawningBiomass,
                maturity, relativeFecundity, femaleSubdivisionIndex, false
            );
        else
            return new RecruitmentBySpawningBiomassDelayed(
                (int) virginRecruits.applyAsDouble(state.getRandom()),
                steepness.applyAsDouble(state.getRandom()),
                cumulativePhi.applyAsDouble(state.getRandom()),
                addRelativeFecundityToSpawningBiomass,
                maturity, relativeFecundity, femaleSubdivisionIndex,
                delay
            );
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
    public void setVirginRecruits(final DoubleParameter virginRecruits) {
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
    public void setSteepness(final DoubleParameter steepness) {
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
    public void setAddRelativeFecundityToSpawningBiomass(final boolean addRelativeFecundityToSpawningBiomass) {
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
    public void setCumulativePhi(final DoubleParameter cumulativePhi) {
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
    public void setYearlyDelay(final DoubleParameter yearlyDelay) {
        this.yearlyDelay = yearlyDelay;
    }


    /**
     * Getter for property 'maturity'.
     *
     * @return Value for property 'maturity'.
     */
    public double[] getMaturity() {
        return maturity;
    }

    /**
     * Setter for property 'maturity'.
     *
     * @param maturity Value to set for property 'maturity'.
     */
    public void setMaturity(final double[] maturity) {
        this.maturity = maturity;
    }

    /**
     * Getter for property 'relativeFecundity'.
     *
     * @return Value for property 'relativeFecundity'.
     */
    public double[] getRelativeFecundity() {
        return relativeFecundity;
    }

    /**
     * Setter for property 'relativeFecundity'.
     *
     * @param relativeFecundity Value to set for property 'relativeFecundity'.
     */
    public void setRelativeFecundity(final double[] relativeFecundity) {
        this.relativeFecundity = relativeFecundity;
    }

    /**
     * Getter for property 'femaleSubdivisionIndex'.
     *
     * @return Value for property 'femaleSubdivisionIndex'.
     */
    public int getFemaleSubdivisionIndex() {
        return femaleSubdivisionIndex;
    }

    /**
     * Setter for property 'femaleSubdivisionIndex'.
     *
     * @param femaleSubdivisionIndex Value to set for property 'femaleSubdivisionIndex'.
     */
    public void setFemaleSubdivisionIndex(final int femaleSubdivisionIndex) {
        this.femaleSubdivisionIndex = femaleSubdivisionIndex;
    }
}
