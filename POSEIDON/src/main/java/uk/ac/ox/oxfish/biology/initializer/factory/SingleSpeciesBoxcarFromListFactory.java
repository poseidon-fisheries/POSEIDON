/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.initializer.factory;

import com.beust.jcommander.internal.Lists;
import uk.ac.ox.oxfish.biology.boxcars.BoxCarSimulator;
import uk.ac.ox.oxfish.biology.complicated.AbundanceDiffuser;
import uk.ac.ox.oxfish.biology.complicated.GrowthBinByList;
import uk.ac.ox.oxfish.biology.complicated.factory.InitialAbundanceFromListFactory;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.LinkedList;
import java.util.List;

/**
 * like the Single Species Boxcar factories but this one forces initial abundance to be whatever is given
 * from the list (needs to be of the same size) <br>
 * The way it works however is by intercepting the SingleSpeciesBoxcarAbstractFactory output
 * and replacing the abundance
 */
public class SingleSpeciesBoxcarFromListFactory implements AlgorithmFactory<SingleSpeciesAbundanceInitializer> {

    private final SingleSpeciesRegularBoxcarFactory delegate = new SingleSpeciesRegularBoxcarFactory();


    private List<Double> initialNumbersInEachBin = Lists.newArrayList(10d, 30d, 100d);

    @Override
    public SingleSpeciesAbundanceInitializer apply(FishState fishState) {
        delegate.setNumberOfBins(initialNumbersInEachBin.size());

        final SingleSpeciesAbundanceInitializer intercepted = delegate.apply(fishState);

        InitialAbundanceFromListFactory forcedAbundance = new InitialAbundanceFromListFactory();
        forcedAbundance.setFishPerBinPerSex(new LinkedList<>(initialNumbersInEachBin));

        return new SingleSpeciesAbundanceInitializer(
            intercepted.getSpeciesName(),
            forcedAbundance.apply(fishState),
            intercepted.getIntialAbundanceAllocator(),
            intercepted.getAging(),
            intercepted.getMeristics(),
            intercepted.getScaling(),
            intercepted.getRecruitmentProcess(),
            intercepted.getDiffuser(),
            intercepted.getRecruitmentAllocator(),
            intercepted.getHabitabilityAllocator(),
            intercepted.getMortality(),
            intercepted.isDaily(),
            intercepted.isRounding()
        );

    }


    public List<Double> getInitialNumbersInEachBin() {
        return initialNumbersInEachBin;
    }

    public void setInitialNumbersInEachBin(List<Double> initialNumbersInEachBin) {
        this.initialNumbersInEachBin = initialNumbersInEachBin;
    }

    public String getSpeciesName() {
        return delegate.getSpeciesName();
    }

    public void setSpeciesName(String speciesName) {
        delegate.setSpeciesName(speciesName);
    }


    public DoubleParameter getLInfinity() {
        return delegate.getLInfinity();
    }

    public void setLInfinity(DoubleParameter LInfinity) {
        delegate.setLInfinity(LInfinity);
    }

    public DoubleParameter getK() {
        return delegate.getK();
    }

    public void setK(DoubleParameter k) {
        delegate.setK(k);
    }

    public DoubleParameter getAllometricAlpha() {
        return delegate.getAllometricAlpha();
    }

    public void setAllometricAlpha(DoubleParameter allometricAlpha) {
        delegate.setAllometricAlpha(allometricAlpha);
    }

    public DoubleParameter getAllometricBeta() {
        return delegate.getAllometricBeta();
    }

    public void setAllometricBeta(DoubleParameter allometricBeta) {
        delegate.setAllometricBeta(allometricBeta);
    }

    public int getNumberOfBins() {
        return delegate.getNumberOfBins();
    }

    public void setNumberOfBins(int numberOfBins) {
        delegate.setNumberOfBins(numberOfBins);
    }

    public DoubleParameter getYearlyMortality() {
        return delegate.getYearlyMortality();
    }

    public void setYearlyMortality(DoubleParameter yearlyMortality) {
        delegate.setYearlyMortality(yearlyMortality);
    }

    public DoubleParameter getVirginRecruits() {
        return delegate.getVirginRecruits();
    }

    public void setVirginRecruits(DoubleParameter virginRecruits) {
        delegate.setVirginRecruits(virginRecruits);
    }

    public DoubleParameter getSteepness() {
        return delegate.getSteepness();
    }

    public void setSteepness(DoubleParameter steepness) {
        delegate.setSteepness(steepness);
    }

    public DoubleParameter getCumulativePhi() {
        return delegate.getCumulativePhi();
    }

    public void setCumulativePhi(DoubleParameter cumulativePhi) {
        delegate.setCumulativePhi(cumulativePhi);
    }

    public DoubleParameter getLengthAtMaturity() {
        return delegate.getLengthAtMaturity();
    }

    public void setLengthAtMaturity(DoubleParameter lengthAtMaturity) {
        delegate.setLengthAtMaturity(lengthAtMaturity);
    }

    public AlgorithmFactory<? extends BiomassAllocator> getInitialAbundanceAllocator() {
        return delegate.getInitialAbundanceAllocator();
    }

    public void setInitialAbundanceAllocator(AlgorithmFactory<? extends BiomassAllocator> initialAbundanceAllocator) {
        delegate.setInitialAbundanceAllocator(initialAbundanceAllocator);
    }

    public AlgorithmFactory<? extends AbundanceDiffuser> getDiffuser() {
        return delegate.getDiffuser();
    }

    public void setDiffuser(AlgorithmFactory<? extends AbundanceDiffuser> diffuser) {
        delegate.setDiffuser(diffuser);
    }

    public AlgorithmFactory<? extends BiomassAllocator> getRecruitAllocator() {
        return delegate.getRecruitAllocator();
    }

    public void setRecruitAllocator(AlgorithmFactory<? extends BiomassAllocator> recruitAllocator) {
        delegate.setRecruitAllocator(recruitAllocator);
    }

    public AlgorithmFactory<? extends BiomassAllocator> getHabitabilityAllocator() {
        return delegate.getHabitabilityAllocator();
    }

    public void setHabitabilityAllocator(AlgorithmFactory<? extends BiomassAllocator> habitabilityAllocator) {
        delegate.setHabitabilityAllocator(habitabilityAllocator);
    }

    public BoxCarSimulator getAbundanceSimulator() {
        return delegate.getAbundanceSimulator();
    }

    public void setAbundanceSimulator(BoxCarSimulator abundanceSimulator) {
        delegate.setAbundanceSimulator(abundanceSimulator);
    }

    public GrowthBinByList generateBins(FishState state) {
        return delegate.generateBins(state);
    }

    public double getCmPerBin() {
        return delegate.getCmPerBin();
    }

    public void setCmPerBin(double cmPerBin) {
        delegate.setCmPerBin(cmPerBin);
    }
}
