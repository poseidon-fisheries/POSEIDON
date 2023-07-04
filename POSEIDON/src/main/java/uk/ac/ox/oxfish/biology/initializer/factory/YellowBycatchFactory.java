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

import uk.ac.ox.oxfish.biology.initializer.YellowBycatchInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 1/21/17.
 */
public class YellowBycatchFactory implements AlgorithmFactory<YellowBycatchInitializer> {
    @SuppressWarnings("deprecation")
    private final uk.ac.ox.oxfish.utility.Locker<String, YellowBycatchInitializer> instance =
        new uk.ac.ox.oxfish.utility.Locker<>();
    private boolean separateBycatchStock = false;
    private String targetSpeciesName = "Sablefish";
    private String bycatchSpeciesName = "Yelloweye Rockfish";
    private DoubleParameter bycatchRho = new FixedDoubleParameter(0.981194230283006d);
    private DoubleParameter bycatchNaturalSurvivalRate = new FixedDoubleParameter(0.95504);
    private DoubleParameter bycatchRecruitmentSteepness = new FixedDoubleParameter(0.44056);
    private DoubleParameter bycatchRecruitmentLag = new FixedDoubleParameter(14);
    private DoubleParameter bycatchWeightAtRecruitment = new FixedDoubleParameter(1.11910);
    private DoubleParameter bycatchWeightAtRecruitmentMinus1 = new FixedDoubleParameter(1.01604);
    private DoubleParameter bycatchVirginBiomass = new FixedDoubleParameter(8883d * 1000d);
    private DoubleParameter bycatchInitialRecruits = new FixedDoubleParameter(111.1395982902 * 1000d);
    private DoubleParameter targetRho = new FixedDoubleParameter(0.813181970802262);
    private DoubleParameter targetNaturalSurvivalRate = new FixedDoubleParameter(0.92311);
    private DoubleParameter targetRecruitmentSteepness = new FixedDoubleParameter(0.6);
    private DoubleParameter targetRecruitmentLag = new FixedDoubleParameter(3);
    private DoubleParameter targetWeightAtRecruitment = new FixedDoubleParameter(1.03313);
    private DoubleParameter targetWeightAtRecruitmentMinus1 = new FixedDoubleParameter(0.63456);
    private DoubleParameter targetVirginBiomass = new FixedDoubleParameter(527154d * 1000d);
    //this is the recruits on the year before the start of the simulation!
    private DoubleParameter targetInitialRecruits = new FixedDoubleParameter(16713267);
    private DoubleParameter northSouthSeparator = new FixedDoubleParameter(50);
    /**
     * any cell with x >= verticalSeparator will include the bycatch species
     */
    private DoubleParameter verticalSeparator
        = new FixedDoubleParameter(25);
    private DoubleParameter proportionOfBycatchNorth = new FixedDoubleParameter(1d);
    private DoubleParameter diffusingRate = new FixedDoubleParameter(.0001);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public YellowBycatchInitializer apply(final FishState state) {
        final int northSouthSeparator = (int) this.northSouthSeparator.applyAsDouble(state.getRandom());
        return instance.presentKey(
            state.getUniqueID(),
            () -> new YellowBycatchInitializer(
                separateBycatchStock,
                targetSpeciesName,
                bycatchSpeciesName,
                bycatchRho.applyAsDouble(state.getRandom()),
                bycatchNaturalSurvivalRate.applyAsDouble(state.getRandom()),
                bycatchRecruitmentSteepness.applyAsDouble(state.getRandom()),
                (int) bycatchRecruitmentLag.applyAsDouble(state.getRandom()),
                bycatchWeightAtRecruitment.applyAsDouble(state.getRandom()),
                bycatchWeightAtRecruitmentMinus1.applyAsDouble(state.getRandom()),
                bycatchVirginBiomass.applyAsDouble(state.getRandom()),
                bycatchInitialRecruits.applyAsDouble(state.getRandom()),
                targetRho.applyAsDouble(state.getRandom()),
                targetNaturalSurvivalRate.applyAsDouble(state.getRandom()),
                targetRecruitmentSteepness.applyAsDouble(state.getRandom()),
                (int) targetRecruitmentLag.applyAsDouble(state.getRandom()),
                targetWeightAtRecruitment.applyAsDouble(state.getRandom()),
                targetWeightAtRecruitmentMinus1.applyAsDouble(state.getRandom()),
                targetVirginBiomass.applyAsDouble(state.getRandom()),
                targetInitialRecruits.applyAsDouble(state.getRandom()),
                (int) verticalSeparator.applyAsDouble(state.getRandom()),
                northSouthSeparator,
                seaTile -> {
                    if (seaTile.getGridY() < northSouthSeparator)
                        return proportionOfBycatchNorth.applyAsDouble(state.getRandom());
                    else
                        return 1d;
                },
                diffusingRate.applyAsDouble(state.getRandom())
            )
        );

    }


    public boolean isSeparateBycatchStock() {
        return separateBycatchStock;
    }

    public void setSeparateBycatchStock(final boolean separateBycatchStock) {
        this.separateBycatchStock = separateBycatchStock;
    }

    public String getTargetSpeciesName() {
        return targetSpeciesName;
    }

    public void setTargetSpeciesName(final String targetSpeciesName) {
        this.targetSpeciesName = targetSpeciesName;
    }

    public String getBycatchSpeciesName() {
        return bycatchSpeciesName;
    }

    public void setBycatchSpeciesName(final String bycatchSpeciesName) {
        this.bycatchSpeciesName = bycatchSpeciesName;
    }

    public DoubleParameter getBycatchRho() {
        return bycatchRho;
    }

    public void setBycatchRho(final DoubleParameter bycatchRho) {
        this.bycatchRho = bycatchRho;
    }

    public DoubleParameter getBycatchNaturalSurvivalRate() {
        return bycatchNaturalSurvivalRate;
    }

    public void setBycatchNaturalSurvivalRate(final DoubleParameter bycatchNaturalSurvivalRate) {
        this.bycatchNaturalSurvivalRate = bycatchNaturalSurvivalRate;
    }

    public DoubleParameter getBycatchRecruitmentSteepness() {
        return bycatchRecruitmentSteepness;
    }

    public void setBycatchRecruitmentSteepness(final DoubleParameter bycatchRecruitmentSteepness) {
        this.bycatchRecruitmentSteepness = bycatchRecruitmentSteepness;
    }

    public DoubleParameter getBycatchRecruitmentLag() {
        return bycatchRecruitmentLag;
    }

    public void setBycatchRecruitmentLag(final DoubleParameter bycatchRecruitmentLag) {
        this.bycatchRecruitmentLag = bycatchRecruitmentLag;
    }

    public DoubleParameter getBycatchWeightAtRecruitment() {
        return bycatchWeightAtRecruitment;
    }

    public void setBycatchWeightAtRecruitment(final DoubleParameter bycatchWeightAtRecruitment) {
        this.bycatchWeightAtRecruitment = bycatchWeightAtRecruitment;
    }

    public DoubleParameter getBycatchWeightAtRecruitmentMinus1() {
        return bycatchWeightAtRecruitmentMinus1;
    }

    public void setBycatchWeightAtRecruitmentMinus1(
        final DoubleParameter bycatchWeightAtRecruitmentMinus1
    ) {
        this.bycatchWeightAtRecruitmentMinus1 = bycatchWeightAtRecruitmentMinus1;
    }

    public DoubleParameter getBycatchVirginBiomass() {
        return bycatchVirginBiomass;
    }

    public void setBycatchVirginBiomass(final DoubleParameter bycatchVirginBiomass) {
        this.bycatchVirginBiomass = bycatchVirginBiomass;
    }

    public DoubleParameter getBycatchInitialRecruits() {
        return bycatchInitialRecruits;
    }

    public void setBycatchInitialRecruits(final DoubleParameter bycatchInitialRecruits) {
        this.bycatchInitialRecruits = bycatchInitialRecruits;
    }

    public DoubleParameter getTargetRho() {
        return targetRho;
    }

    public void setTargetRho(final DoubleParameter targetRho) {
        this.targetRho = targetRho;
    }

    public DoubleParameter getTargetNaturalSurvivalRate() {
        return targetNaturalSurvivalRate;
    }

    public void setTargetNaturalSurvivalRate(final DoubleParameter targetNaturalSurvivalRate) {
        this.targetNaturalSurvivalRate = targetNaturalSurvivalRate;
    }

    public DoubleParameter getTargetRecruitmentSteepness() {
        return targetRecruitmentSteepness;
    }

    public void setTargetRecruitmentSteepness(final DoubleParameter targetRecruitmentSteepness) {
        this.targetRecruitmentSteepness = targetRecruitmentSteepness;
    }

    public DoubleParameter getTargetRecruitmentLag() {
        return targetRecruitmentLag;
    }

    public void setTargetRecruitmentLag(final DoubleParameter targetRecruitmentLag) {
        this.targetRecruitmentLag = targetRecruitmentLag;
    }

    public DoubleParameter getTargetWeightAtRecruitment() {
        return targetWeightAtRecruitment;
    }

    public void setTargetWeightAtRecruitment(final DoubleParameter targetWeightAtRecruitment) {
        this.targetWeightAtRecruitment = targetWeightAtRecruitment;
    }

    public DoubleParameter getTargetWeightAtRecruitmentMinus1() {
        return targetWeightAtRecruitmentMinus1;
    }

    public void setTargetWeightAtRecruitmentMinus1(
        final DoubleParameter targetWeightAtRecruitmentMinus1
    ) {
        this.targetWeightAtRecruitmentMinus1 = targetWeightAtRecruitmentMinus1;
    }

    public DoubleParameter getTargetVirginBiomass() {
        return targetVirginBiomass;
    }

    public void setTargetVirginBiomass(final DoubleParameter targetVirginBiomass) {
        this.targetVirginBiomass = targetVirginBiomass;
    }

    public DoubleParameter getTargetInitialRecruits() {
        return targetInitialRecruits;
    }

    public void setTargetInitialRecruits(final DoubleParameter targetInitialRecruits) {
        this.targetInitialRecruits = targetInitialRecruits;
    }

    public DoubleParameter getVerticalSeparator() {
        return verticalSeparator;
    }

    public void setVerticalSeparator(final DoubleParameter verticalSeparator) {
        this.verticalSeparator = verticalSeparator;
    }


    /**
     * Getter for property 'northSouthSeparator'.
     *
     * @return Value for property 'northSouthSeparator'.
     */
    public DoubleParameter getNorthSouthSeparator() {
        return northSouthSeparator;
    }

    /**
     * Setter for property 'northSouthSeparator'.
     *
     * @param northSouthSeparator Value to set for property 'northSouthSeparator'.
     */
    public void setNorthSouthSeparator(final DoubleParameter northSouthSeparator) {
        this.northSouthSeparator = northSouthSeparator;
    }

    /**
     * Getter for property 'proportionOfBycatchNorth'.
     *
     * @return Value for property 'proportionOfBycatchNorth'.
     */
    public DoubleParameter getProportionOfBycatchNorth() {
        return proportionOfBycatchNorth;
    }

    /**
     * Setter for property 'proportionOfBycatchNorth'.
     *
     * @param proportionOfBycatchNorth Value to set for property 'proportionOfBycatchNorth'.
     */
    public void setProportionOfBycatchNorth(final DoubleParameter proportionOfBycatchNorth) {
        this.proportionOfBycatchNorth = proportionOfBycatchNorth;
    }

    public YellowBycatchInitializer retrieveLastMade() {
        return instance.presentKey(instance.getCurrentKey(), () -> {
            throw new RuntimeException("Not instantiated yet!");
        });
    }

    /**
     * Getter for property 'diffusingRate'.
     *
     * @return Value for property 'diffusingRate'.
     */
    public DoubleParameter getDiffusingRate() {
        return diffusingRate;
    }

    /**
     * Setter for property 'diffusingRate'.
     *
     * @param diffusingRate Value to set for property 'diffusingRate'.
     */
    public void setDiffusingRate(final DoubleParameter diffusingRate) {
        this.diffusingRate = diffusingRate;
    }
}
