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

package uk.ac.ox.oxfish.biology.initializer.factory;

import fr.ird.osmose.process.mortality.MortalityProcess;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.complicated.factory.*;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantAllocatorFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/11/17.
 */

public class SingleSpeciesAbundanceFactory implements AlgorithmFactory<SingleSpeciesAbundanceInitializer> {


    private String speciesName = "Red Fish";
    private AlgorithmFactory<? extends InitialAbundance> initialAbundanceFactory = new InitialAbundanceFromListFactory();
    private AlgorithmFactory<? extends AgingProcess> aging = new ProportionalAgingFactory();

    private AlgorithmFactory<? extends BiomassAllocator> initialAbundanceAllocator = new ConstantAllocatorFactory();

    private AlgorithmFactory<? extends Meristics> meristics = new ListMeristicFactory();

    private AlgorithmFactory<? extends RecruitmentProcess> recruitment = new LogisticRecruitmentFactory();

    private AlgorithmFactory<? extends AbundanceDiffuser> diffuser = new NoDiffuserFactory();

    private AlgorithmFactory<? extends BiomassAllocator> recruitAllocator = new ConstantAllocatorFactory();

    private AlgorithmFactory<? extends BiomassAllocator> habitabilityAllocator = new ConstantAllocatorFactory();

    private AlgorithmFactory<? extends NaturalMortalityProcess> mortalityProcess =
            new ExponentialMortalityFactory();


    private DoubleParameter scaling = new FixedDoubleParameter(1.0);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SingleSpeciesAbundanceInitializer apply(FishState state) {

        return new SingleSpeciesAbundanceInitializer(
                speciesName,
                initialAbundanceFactory,
                initialAbundanceAllocator.apply(state),
                aging.apply(state),
                meristics.apply(state),
                scaling.apply(state.getRandom()),
                recruitment.apply(state),
                diffuser.apply(state),
                recruitAllocator.apply(state),
                habitabilityAllocator.apply(state),
                mortalityProcess.apply(state));


    }


    /**
     * Getter for property 'speciesName'.
     *
     * @return Value for property 'speciesName'.
     */
    public String getSpeciesName() {
        return speciesName;
    }

    /**
     * Getter for property 'aging'.
     *
     * @return Value for property 'aging'.
     */
    public AlgorithmFactory<? extends AgingProcess> getAging() {
        return aging;
    }

    /**
     * Setter for property 'aging'.
     *
     * @param aging Value to set for property 'aging'.
     */
    public void setAging(
            AlgorithmFactory<? extends AgingProcess> aging) {
        this.aging = aging;
    }

    /**
     * Getter for property 'initialAbundanceAllocator'.
     *
     * @return Value for property 'initialAbundanceAllocator'.
     */
    public AlgorithmFactory<? extends BiomassAllocator> getInitialAbundanceAllocator() {
        return initialAbundanceAllocator;
    }

    /**
     * Setter for property 'initialAbundanceAllocator'.
     *
     * @param initialAbundanceAllocator Value to set for property 'initialAbundanceAllocator'.
     */
    public void setInitialAbundanceAllocator(
            AlgorithmFactory<? extends BiomassAllocator> initialAbundanceAllocator) {
        this.initialAbundanceAllocator = initialAbundanceAllocator;
    }

    /**
     * Getter for property 'meristics'.
     *
     * @return Value for property 'meristics'.
     */
    public AlgorithmFactory<? extends Meristics> getMeristics() {
        return meristics;
    }

    /**
     * Setter for property 'meristics'.
     *
     * @param meristics Value to set for property 'meristics'.
     */
    public void setMeristics(
            AlgorithmFactory<? extends Meristics> meristics) {
        this.meristics = meristics;
    }

    /**
     * Getter for property 'recruitment'.
     *
     * @return Value for property 'recruitment'.
     */
    public AlgorithmFactory<? extends RecruitmentProcess> getRecruitment() {
        return recruitment;
    }

    /**
     * Setter for property 'recruitment'.
     *
     * @param recruitment Value to set for property 'recruitment'.
     */
    public void setRecruitment(
            AlgorithmFactory<? extends RecruitmentProcess> recruitment) {
        this.recruitment = recruitment;
    }

    /**
     * Getter for property 'diffuser'.
     *
     * @return Value for property 'diffuser'.
     */
    public AlgorithmFactory<? extends AbundanceDiffuser> getDiffuser() {
        return diffuser;
    }

    /**
     * Setter for property 'diffuser'.
     *
     * @param diffuser Value to set for property 'diffuser'.
     */
    public void setDiffuser(
            AlgorithmFactory<? extends AbundanceDiffuser> diffuser) {
        this.diffuser = diffuser;
    }

    /**
     * Getter for property 'recruitAllocator'.
     *
     * @return Value for property 'recruitAllocator'.
     */
    public AlgorithmFactory<? extends BiomassAllocator> getRecruitAllocator() {
        return recruitAllocator;
    }

    /**
     * Setter for property 'recruitAllocator'.
     *
     * @param recruitAllocator Value to set for property 'recruitAllocator'.
     */
    public void setRecruitAllocator(
            AlgorithmFactory<? extends BiomassAllocator> recruitAllocator) {
        this.recruitAllocator = recruitAllocator;
    }

    /**
     * Getter for property 'scaling'.
     *
     * @return Value for property 'scaling'.
     */
    public DoubleParameter getScaling() {
        return scaling;
    }

    /**
     * Setter for property 'scaling'.
     *
     * @param scaling Value to set for property 'scaling'.
     */
    public void setScaling(DoubleParameter scaling) {
        this.scaling = scaling;
    }

    /**
     * Setter for property 'speciesName'.
     *
     * @param speciesName Value to set for property 'speciesName'.
     */
    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }


    /**
     * Getter for property 'initialAbundanceFactory'.
     *
     * @return Value for property 'initialAbundanceFactory'.
     */
    public AlgorithmFactory<? extends InitialAbundance> getInitialAbundanceFactory() {
        return initialAbundanceFactory;
    }

    /**
     * Setter for property 'initialAbundanceFactory'.
     *
     * @param initialAbundanceFactory Value to set for property 'initialAbundanceFactory'.
     */
    public void setInitialAbundanceFactory(
            AlgorithmFactory<? extends InitialAbundance> initialAbundanceFactory) {
        this.initialAbundanceFactory = initialAbundanceFactory;
    }

    /**
     * Getter for property 'habitabilityAllocator'.
     *
     * @return Value for property 'habitabilityAllocator'.
     */
    public AlgorithmFactory<? extends BiomassAllocator> getHabitabilityAllocator() {
        return habitabilityAllocator;
    }

    /**
     * Setter for property 'habitabilityAllocator'.
     *
     * @param habitabilityAllocator Value to set for property 'habitabilityAllocator'.
     */
    public void setHabitabilityAllocator(
            AlgorithmFactory<? extends BiomassAllocator> habitabilityAllocator) {
        this.habitabilityAllocator = habitabilityAllocator;
    }
}
