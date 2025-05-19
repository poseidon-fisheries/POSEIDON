/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.biology.initializer.YellowBycatchInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Like the yellow-bycatch but it allows for user stories
 * Created by carrknight on 3/17/17.
 */
public class YellowBycatchWithHistoryFactory implements AlgorithmFactory<YellowBycatchInitializer> {


    private YellowBycatchFactory factory = new YellowBycatchFactory();


    private List<Double> historicalTargetBiomass = Lists.newArrayList(1d, 2d, 3d);


    private List<Double> historicalBycatchBiomass = Lists.newArrayList(1d, 2d, 3d);


    private List<Double> historicalTargetSurvival = null;


    private List<Double> historicalBycatchSurvival = null;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public YellowBycatchInitializer apply(FishState state) {
        YellowBycatchInitializer product = factory.apply(state);
        if (historicalTargetBiomass != null)
            product.setHistoricalTargetBiomass(new ArrayList<>(historicalTargetBiomass));
        if (historicalBycatchBiomass != null)
            product.setHistoricalBycatchBiomass(new ArrayList<>(historicalBycatchBiomass));
        if (historicalTargetSurvival != null)
            product.setHistoricalTargetSurvivalRate(new ArrayList<>(historicalTargetSurvival));
        if (historicalBycatchSurvival != null)
            product.setHistoricalBycatchSurvivalRate(new ArrayList<>(historicalBycatchSurvival));


        return product;
    }

    public boolean isSeparateBycatchStock() {
        return factory.isSeparateBycatchStock();
    }

    public void setSeparateBycatchStock(boolean separateBycatchStock) {
        factory.setSeparateBycatchStock(separateBycatchStock);
    }

    public String getTargetSpeciesName() {
        return factory.getTargetSpeciesName();
    }

    public void setTargetSpeciesName(String targetSpeciesName) {
        factory.setTargetSpeciesName(targetSpeciesName);
    }

    public String getBycatchSpeciesName() {
        return factory.getBycatchSpeciesName();
    }

    public void setBycatchSpeciesName(String bycatchSpeciesName) {
        factory.setBycatchSpeciesName(bycatchSpeciesName);
    }

    public DoubleParameter getBycatchRho() {
        return factory.getBycatchRho();
    }

    public void setBycatchRho(DoubleParameter bycatchRho) {
        factory.setBycatchRho(bycatchRho);
    }

    public DoubleParameter getBycatchNaturalSurvivalRate() {
        return factory.getBycatchNaturalSurvivalRate();
    }

    public void setBycatchNaturalSurvivalRate(DoubleParameter bycatchNaturalSurvivalRate) {
        factory.setBycatchNaturalSurvivalRate(bycatchNaturalSurvivalRate);
    }

    public DoubleParameter getBycatchRecruitmentSteepness() {
        return factory.getBycatchRecruitmentSteepness();
    }

    public void setBycatchRecruitmentSteepness(
        DoubleParameter bycatchRecruitmentSteepness
    ) {
        factory.setBycatchRecruitmentSteepness(bycatchRecruitmentSteepness);
    }

    public DoubleParameter getBycatchRecruitmentLag() {
        return factory.getBycatchRecruitmentLag();
    }

    public void setBycatchRecruitmentLag(DoubleParameter bycatchRecruitmentLag) {
        factory.setBycatchRecruitmentLag(bycatchRecruitmentLag);
    }

    public DoubleParameter getBycatchWeightAtRecruitment() {
        return factory.getBycatchWeightAtRecruitment();
    }

    public void setBycatchWeightAtRecruitment(DoubleParameter bycatchWeightAtRecruitment) {
        factory.setBycatchWeightAtRecruitment(bycatchWeightAtRecruitment);
    }

    public DoubleParameter getBycatchWeightAtRecruitmentMinus1() {
        return factory.getBycatchWeightAtRecruitmentMinus1();
    }

    public void setBycatchWeightAtRecruitmentMinus1(
        DoubleParameter bycatchWeightAtRecruitmentMinus1
    ) {
        factory.setBycatchWeightAtRecruitmentMinus1(bycatchWeightAtRecruitmentMinus1);
    }

    public DoubleParameter getBycatchVirginBiomass() {
        return factory.getBycatchVirginBiomass();
    }

    public void setBycatchVirginBiomass(DoubleParameter bycatchVirginBiomass) {
        factory.setBycatchVirginBiomass(bycatchVirginBiomass);
    }


    public DoubleParameter getBycatchInitialRecruits() {
        return factory.getBycatchInitialRecruits();
    }

    public void setBycatchInitialRecruits(DoubleParameter bycatchInitialRecruits) {
        factory.setBycatchInitialRecruits(bycatchInitialRecruits);
    }

    public DoubleParameter getTargetInitialRecruits() {
        return factory.getTargetInitialRecruits();
    }

    public void setTargetInitialRecruits(DoubleParameter targetInitialRecruits) {
        factory.setTargetInitialRecruits(targetInitialRecruits);
    }

    public DoubleParameter getTargetRho() {
        return factory.getTargetRho();
    }

    public void setTargetRho(DoubleParameter targetRho) {
        factory.setTargetRho(targetRho);
    }

    public DoubleParameter getTargetNaturalSurvivalRate() {
        return factory.getTargetNaturalSurvivalRate();
    }

    public void setTargetNaturalSurvivalRate(DoubleParameter targetNaturalSurvivalRate) {
        factory.setTargetNaturalSurvivalRate(targetNaturalSurvivalRate);
    }

    public DoubleParameter getTargetRecruitmentSteepness() {
        return factory.getTargetRecruitmentSteepness();
    }

    public void setTargetRecruitmentSteepness(DoubleParameter targetRecruitmentSteepness) {
        factory.setTargetRecruitmentSteepness(targetRecruitmentSteepness);
    }

    public DoubleParameter getTargetRecruitmentLag() {
        return factory.getTargetRecruitmentLag();
    }

    public void setTargetRecruitmentLag(DoubleParameter targetRecruitmentLag) {
        factory.setTargetRecruitmentLag(targetRecruitmentLag);
    }

    public DoubleParameter getTargetWeightAtRecruitment() {
        return factory.getTargetWeightAtRecruitment();
    }

    public void setTargetWeightAtRecruitment(DoubleParameter targetWeightAtRecruitment) {
        factory.setTargetWeightAtRecruitment(targetWeightAtRecruitment);
    }

    public DoubleParameter getTargetWeightAtRecruitmentMinus1() {
        return factory.getTargetWeightAtRecruitmentMinus1();
    }

    public void setTargetWeightAtRecruitmentMinus1(
        DoubleParameter targetWeightAtRecruitmentMinus1
    ) {
        factory.setTargetWeightAtRecruitmentMinus1(targetWeightAtRecruitmentMinus1);
    }

    public DoubleParameter getTargetVirginBiomass() {
        return factory.getTargetVirginBiomass();
    }

    public void setTargetVirginBiomass(DoubleParameter targetVirginBiomass) {
        factory.setTargetVirginBiomass(targetVirginBiomass);
    }

    public DoubleParameter getVerticalSeparator() {
        return factory.getVerticalSeparator();
    }

    public void setVerticalSeparator(DoubleParameter verticalSeparator) {
        factory.setVerticalSeparator(verticalSeparator);
    }


    /**
     * Getter for property 'historicalTargetBiomass'.
     *
     * @return Value for property 'historicalTargetBiomass'.
     */
    public List<Double> getHistoricalTargetBiomass() {
        return historicalTargetBiomass;
    }

    /**
     * Setter for property 'historicalTargetBiomass'.
     *
     * @param historicalTargetBiomass Value to set for property 'historicalTargetBiomass'.
     */
    public void setHistoricalTargetBiomass(List<Double> historicalTargetBiomass) {
        this.historicalTargetBiomass = historicalTargetBiomass;
    }

    /**
     * Getter for property 'historicalBycatchBiomass'.
     *
     * @return Value for property 'historicalBycatchBiomass'.
     */
    public List<Double> getHistoricalBycatchBiomass() {
        return historicalBycatchBiomass;
    }

    /**
     * Setter for property 'historicalBycatchBiomass'.
     *
     * @param historicalBycatchBiomass Value to set for property 'historicalBycatchBiomass'.
     */
    public void setHistoricalBycatchBiomass(List<Double> historicalBycatchBiomass) {
        this.historicalBycatchBiomass = historicalBycatchBiomass;
    }

    /**
     * Getter for property 'historicalTargetSurvival'.
     *
     * @return Value for property 'historicalTargetSurvival'.
     */
    public List<Double> getHistoricalTargetSurvival() {
        return historicalTargetSurvival;
    }

    /**
     * Setter for property 'historicalTargetSurvival'.
     *
     * @param historicalTargetSurvival Value to set for property 'historicalTargetSurvival'.
     */
    public void setHistoricalTargetSurvival(List<Double> historicalTargetSurvival) {
        this.historicalTargetSurvival = historicalTargetSurvival;
    }

    /**
     * Getter for property 'historicalBycatchSurvival'.
     *
     * @return Value for property 'historicalBycatchSurvival'.
     */
    public List<Double> getHistoricalBycatchSurvival() {
        return historicalBycatchSurvival;
    }

    /**
     * Setter for property 'historicalBycatchSurvival'.
     *
     * @param historicalBycatchSurvival Value to set for property 'historicalBycatchSurvival'.
     */
    public void setHistoricalBycatchSurvival(List<Double> historicalBycatchSurvival) {
        this.historicalBycatchSurvival = historicalBycatchSurvival;
    }

    /**
     * Getter for property 'northSouthSeparator'.
     *
     * @return Value for property 'northSouthSeparator'.
     */
    public DoubleParameter getNorthSouthSeparator() {
        return factory.getNorthSouthSeparator();
    }

    /**
     * Setter for property 'northSouthSeparator'.
     *
     * @param northSouthSeparator Value to set for property 'northSouthSeparator'.
     */
    public void setNorthSouthSeparator(DoubleParameter northSouthSeparator) {
        factory.setNorthSouthSeparator(northSouthSeparator);
    }

    /**
     * Getter for property 'proportionOfBycatchNorth'.
     *
     * @return Value for property 'proportionOfBycatchNorth'.
     */
    public DoubleParameter getProportionOfBycatchNorth() {
        return factory.getProportionOfBycatchNorth();
    }

    /**
     * Setter for property 'proportionOfBycatchNorth'.
     *
     * @param proportionOfBycatchNorth Value to set for property 'proportionOfBycatchNorth'.
     */
    public void setProportionOfBycatchNorth(DoubleParameter proportionOfBycatchNorth) {
        factory.setProportionOfBycatchNorth(proportionOfBycatchNorth);
    }

    public YellowBycatchInitializer retrieveLastMade() {
        return factory.retrieveLastMade();
    }


    /**
     * Getter for property 'diffusingRate'.
     *
     * @return Value for property 'diffusingRate'.
     */
    public DoubleParameter getDiffusingRate() {
        return factory.getDiffusingRate();
    }

    /**
     * Setter for property 'diffusingRate'.
     *
     * @param diffusingRate Value to set for property 'diffusingRate'.
     */
    public void setDiffusingRate(DoubleParameter diffusingRate) {
        factory.setDiffusingRate(diffusingRate);
    }
}
