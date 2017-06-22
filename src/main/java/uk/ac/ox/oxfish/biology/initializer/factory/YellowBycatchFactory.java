package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.YellowBycatchInitializer;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;

/**
 * Created by carrknight on 1/21/17.
 */
public class YellowBycatchFactory implements AlgorithmFactory<YellowBycatchInitializer>
{
    private boolean separateBycatchStock = false;

    private String targetSpeciesName = "Sablefish";

    private String bycatchSpeciesName  = "Yelloweye Rockfish";

    private DoubleParameter bycatchRho = new FixedDoubleParameter(1.03);

    private DoubleParameter bycatchNaturalSurvivalRate = new FixedDoubleParameter(0.95504);

    private DoubleParameter bycatchRecruitmentSteepness = new FixedDoubleParameter(0.44056);


    private DoubleParameter bycatchRecruitmentLag = new FixedDoubleParameter(14);

    private DoubleParameter bycatchWeightAtRecruitment = new FixedDoubleParameter(1.11909797520236);

    private DoubleParameter bycatchWeightAtRecruitmentMinus1 = new FixedDoubleParameter(1.01603952895487);


    private DoubleParameter bycatchVirginBiomass = new FixedDoubleParameter(8883d * 1000d);

    private DoubleParameter bycatchInitialRecruits = new FixedDoubleParameter(54.44606 * 1000d);





    private DoubleParameter  targetRho = new FixedDoubleParameter(0.92267402483245);

    private DoubleParameter targetNaturalSurvivalRate = new FixedDoubleParameter(0.923116346386636);

    private DoubleParameter targetRecruitmentSteepness = new FixedDoubleParameter(0.6);


    private DoubleParameter targetRecruitmentLag = new FixedDoubleParameter(3);

    private DoubleParameter targetWeightAtRecruitment = new FixedDoubleParameter(1.03312585773941);

    private DoubleParameter targetWeightAtRecruitmentMinus1 = new FixedDoubleParameter(0.634560212266768);


    private DoubleParameter targetVirginBiomass = new FixedDoubleParameter(527154d * 1000d);

    //this is really the "rzero" value of the DS algorithm when biomass is virgin
    private DoubleParameter targetInitialRecruits = new FixedDoubleParameter(1.2197524018851934E7);



    private DoubleParameter northSouthSeparator = new FixedDoubleParameter(50);

    /**
     * any cell with x >= verticalSeparator will include the bycatch species
     */
    private DoubleParameter verticalSeparator
            = new FixedDoubleParameter(25);


    private DoubleParameter proportionOfBycatchNorth = new FixedDoubleParameter(1d);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public YellowBycatchInitializer apply(FishState state) {
        int northSouthSeparator = this.northSouthSeparator.apply(state.getRandom()).intValue();
        return new YellowBycatchInitializer(
                separateBycatchStock,
                targetSpeciesName,
                bycatchSpeciesName,
                bycatchRho.apply(state.getRandom()),
                bycatchNaturalSurvivalRate.apply(state.getRandom()),
                bycatchRecruitmentSteepness.apply(state.getRandom()),
                bycatchRecruitmentLag.apply(state.getRandom()).intValue(),
                bycatchWeightAtRecruitment.apply(state.getRandom()),
                bycatchWeightAtRecruitmentMinus1.apply(state.getRandom()),
                bycatchVirginBiomass.apply(state.getRandom()),
                bycatchInitialRecruits.apply(state.getRandom()),
                targetRho.apply(state.getRandom()),
                targetNaturalSurvivalRate.apply(state.getRandom()),
                targetRecruitmentSteepness.apply(state.getRandom()),
                targetRecruitmentLag.apply(state.getRandom()).intValue(),
                targetWeightAtRecruitment.apply(state.getRandom()),
                targetWeightAtRecruitmentMinus1.apply(state.getRandom()),
                targetVirginBiomass.apply(state.getRandom()),
                targetInitialRecruits.apply(state.getRandom()),
                verticalSeparator.apply(state.getRandom()).intValue(),
                northSouthSeparator,
                new Function<SeaTile, Double>() {
                    @Override
                    public Double apply(SeaTile seaTile) {
                        if(seaTile.getGridY() < northSouthSeparator)
                            return proportionOfBycatchNorth.apply(state.getRandom());
                        else
                            return 1d;
                    }
                });
    }



    public boolean isSeparateBycatchStock() {
        return separateBycatchStock;
    }

    public void setSeparateBycatchStock(boolean separateBycatchStock) {
        this.separateBycatchStock = separateBycatchStock;
    }

    public String getTargetSpeciesName() {
        return targetSpeciesName;
    }

    public void setTargetSpeciesName(String targetSpeciesName) {
        this.targetSpeciesName = targetSpeciesName;
    }

    public String getBycatchSpeciesName() {
        return bycatchSpeciesName;
    }

    public void setBycatchSpeciesName(String bycatchSpeciesName) {
        this.bycatchSpeciesName = bycatchSpeciesName;
    }

    public DoubleParameter getBycatchRho() {
        return bycatchRho;
    }

    public void setBycatchRho(DoubleParameter bycatchRho) {
        this.bycatchRho = bycatchRho;
    }

    public DoubleParameter getBycatchNaturalSurvivalRate() {
        return bycatchNaturalSurvivalRate;
    }

    public void setBycatchNaturalSurvivalRate(DoubleParameter bycatchNaturalSurvivalRate) {
        this.bycatchNaturalSurvivalRate = bycatchNaturalSurvivalRate;
    }

    public DoubleParameter getBycatchRecruitmentSteepness() {
        return bycatchRecruitmentSteepness;
    }

    public void setBycatchRecruitmentSteepness(DoubleParameter bycatchRecruitmentSteepness) {
        this.bycatchRecruitmentSteepness = bycatchRecruitmentSteepness;
    }

    public DoubleParameter getBycatchRecruitmentLag() {
        return bycatchRecruitmentLag;
    }

    public void setBycatchRecruitmentLag(DoubleParameter bycatchRecruitmentLag) {
        this.bycatchRecruitmentLag = bycatchRecruitmentLag;
    }

    public DoubleParameter getBycatchWeightAtRecruitment() {
        return bycatchWeightAtRecruitment;
    }

    public void setBycatchWeightAtRecruitment(DoubleParameter bycatchWeightAtRecruitment) {
        this.bycatchWeightAtRecruitment = bycatchWeightAtRecruitment;
    }

    public DoubleParameter getBycatchWeightAtRecruitmentMinus1() {
        return bycatchWeightAtRecruitmentMinus1;
    }

    public void setBycatchWeightAtRecruitmentMinus1(
            DoubleParameter bycatchWeightAtRecruitmentMinus1) {
        this.bycatchWeightAtRecruitmentMinus1 = bycatchWeightAtRecruitmentMinus1;
    }

    public DoubleParameter getBycatchVirginBiomass() {
        return bycatchVirginBiomass;
    }

    public void setBycatchVirginBiomass(DoubleParameter bycatchVirginBiomass) {
        this.bycatchVirginBiomass = bycatchVirginBiomass;
    }

    public DoubleParameter getBycatchInitialRecruits() {
        return bycatchInitialRecruits;
    }

    public void setBycatchInitialRecruits(DoubleParameter bycatchInitialRecruits) {
        this.bycatchInitialRecruits = bycatchInitialRecruits;
    }

    public DoubleParameter getTargetRho() {
        return targetRho;
    }

    public void setTargetRho(DoubleParameter targetRho) {
        this.targetRho = targetRho;
    }

    public DoubleParameter getTargetNaturalSurvivalRate() {
        return targetNaturalSurvivalRate;
    }

    public void setTargetNaturalSurvivalRate(DoubleParameter targetNaturalSurvivalRate) {
        this.targetNaturalSurvivalRate = targetNaturalSurvivalRate;
    }

    public DoubleParameter getTargetRecruitmentSteepness() {
        return targetRecruitmentSteepness;
    }

    public void setTargetRecruitmentSteepness(DoubleParameter targetRecruitmentSteepness) {
        this.targetRecruitmentSteepness = targetRecruitmentSteepness;
    }

    public DoubleParameter getTargetRecruitmentLag() {
        return targetRecruitmentLag;
    }

    public void setTargetRecruitmentLag(DoubleParameter targetRecruitmentLag) {
        this.targetRecruitmentLag = targetRecruitmentLag;
    }

    public DoubleParameter getTargetWeightAtRecruitment() {
        return targetWeightAtRecruitment;
    }

    public void setTargetWeightAtRecruitment(DoubleParameter targetWeightAtRecruitment) {
        this.targetWeightAtRecruitment = targetWeightAtRecruitment;
    }

    public DoubleParameter getTargetWeightAtRecruitmentMinus1() {
        return targetWeightAtRecruitmentMinus1;
    }

    public void setTargetWeightAtRecruitmentMinus1(
            DoubleParameter targetWeightAtRecruitmentMinus1) {
        this.targetWeightAtRecruitmentMinus1 = targetWeightAtRecruitmentMinus1;
    }

    public DoubleParameter getTargetVirginBiomass() {
        return targetVirginBiomass;
    }

    public void setTargetVirginBiomass(DoubleParameter targetVirginBiomass) {
        this.targetVirginBiomass = targetVirginBiomass;
    }

    public DoubleParameter getTargetInitialRecruits() {
        return targetInitialRecruits;
    }

    public void setTargetInitialRecruits(DoubleParameter targetInitialRecruits) {
        this.targetInitialRecruits = targetInitialRecruits;
    }

    public DoubleParameter getVerticalSeparator() {
        return verticalSeparator;
    }

    public void setVerticalSeparator(DoubleParameter verticalSeparator) {
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
    public void setNorthSouthSeparator(DoubleParameter northSouthSeparator) {
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
    public void setProportionOfBycatchNorth(DoubleParameter proportionOfBycatchNorth) {
        this.proportionOfBycatchNorth = proportionOfBycatchNorth;
    }
}
