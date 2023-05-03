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

import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.complicated.factory.*;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantAllocatorFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.CatchesHistogrammer;
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


    private boolean daily = false;

    private boolean rounding = true;

    private boolean histogrammerOutput = false;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SingleSpeciesAbundanceInitializer apply(final FishState state) {

        if (histogrammerOutput)
            state.getOutputPlugins().add(new CatchesHistogrammer());

        return new SingleSpeciesAbundanceInitializer(
            speciesName,
            initialAbundanceFactory.apply(state),
            initialAbundanceAllocator.apply(state),
            aging.apply(state),
            meristics.apply(state),
            scaling.applyAsDouble(state.getRandom()),
            recruitment.apply(state),
            diffuser.apply(state),
            recruitAllocator.apply(state),
            habitabilityAllocator.apply(state),
            mortalityProcess.apply(state), daily, rounding
        );


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
     * Setter for property 'speciesName'.
     *
     * @param speciesName Value to set for property 'speciesName'.
     */
    public void setSpeciesName(final String speciesName) {
        this.speciesName = speciesName;
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
        final AlgorithmFactory<? extends AgingProcess> aging
    ) {
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
        final AlgorithmFactory<? extends BiomassAllocator> initialAbundanceAllocator
    ) {
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
        final AlgorithmFactory<? extends Meristics> meristics
    ) {
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
        final AlgorithmFactory<? extends RecruitmentProcess> recruitment
    ) {
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
        final AlgorithmFactory<? extends AbundanceDiffuser> diffuser
    ) {
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
        final AlgorithmFactory<? extends BiomassAllocator> recruitAllocator
    ) {
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
    public void setScaling(final DoubleParameter scaling) {
        this.scaling = scaling;
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
        final AlgorithmFactory<? extends InitialAbundance> initialAbundanceFactory
    ) {
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
        final AlgorithmFactory<? extends BiomassAllocator> habitabilityAllocator
    ) {
        this.habitabilityAllocator = habitabilityAllocator;
    }

    /**
     * Getter for property 'mortalityProcess'.
     *
     * @return Value for property 'mortalityProcess'.
     */
    public AlgorithmFactory<? extends NaturalMortalityProcess> getMortalityProcess() {
        return mortalityProcess;
    }

    /**
     * Setter for property 'mortalityProcess'.
     *
     * @param mortalityProcess Value to set for property 'mortalityProcess'.
     */
    public void setMortalityProcess(
        final AlgorithmFactory<? extends NaturalMortalityProcess> mortalityProcess
    ) {
        this.mortalityProcess = mortalityProcess;
    }

    /**
     * Getter for property 'daily'.
     *
     * @return Value for property 'daily'.
     */
    public boolean isDaily() {
        return daily;
    }

    /**
     * Setter for property 'daily'.
     *
     * @param daily Value to set for property 'daily'.
     */
    public void setDaily(final boolean daily) {
        this.daily = daily;
    }

    /**
     * Getter for property 'rounding'.
     *
     * @return Value for property 'rounding'.
     */
    public boolean isRounding() {
        return rounding;
    }

    /**
     * Setter for property 'rounding'.
     *
     * @param rounding Value to set for property 'rounding'.
     */
    public void setRounding(final boolean rounding) {
        this.rounding = rounding;
    }


    /**
     * Getter for property 'histogrammerOutput'.
     *
     * @return Value for property 'histogrammerOutput'.
     */
    public boolean isHistogrammerOutput() {
        return histogrammerOutput;
    }

    /**
     * Setter for property 'histogrammerOutput'.
     *
     * @param histogrammerOutput Value to set for property 'histogrammerOutput'.
     */
    public void setHistogrammerOutput(final boolean histogrammerOutput) {
        this.histogrammerOutput = histogrammerOutput;
    }
}
