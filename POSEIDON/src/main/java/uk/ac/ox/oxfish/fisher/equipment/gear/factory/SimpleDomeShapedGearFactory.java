package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticSimpleFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.SimplifiedDoubleNormalFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class SimpleDomeShapedGearFactory implements HomogeneousGearFactory {



    private boolean rounding = false;


    private DoubleParameter litersOfGasConsumedPerHour = new FixedDoubleParameter(0);

    private DoubleParameter averageCatchability = new FixedDoubleParameter(0);

    private DoubleParameter slopeLeft = new FixedDoubleParameter(5);
    private DoubleParameter slopeRight = new FixedDoubleParameter(10);
    private DoubleParameter lengthFullSelectivity = new FixedDoubleParameter(30);




    @Override
    public HomogeneousAbundanceGear apply(FishState fishState) {

        MersenneTwisterFast random = fishState.getRandom();

        return new HomogeneousAbundanceGear(
                litersOfGasConsumedPerHour.apply(random),
                new FixedProportionFilter(averageCatchability.apply(random), rounding),
                new SimplifiedDoubleNormalFilter(
                        true,
                        rounding,
                        lengthFullSelectivity.apply(random),
                        slopeLeft.apply(random),
                        slopeRight.apply(random)));
    }

    public boolean isRounding() {
        return rounding;
    }

    public void setRounding(boolean rounding) {
        this.rounding = rounding;
    }

    public DoubleParameter getLitersOfGasConsumedPerHour() {
        return litersOfGasConsumedPerHour;
    }

    public void setLitersOfGasConsumedPerHour(DoubleParameter litersOfGasConsumedPerHour) {
        this.litersOfGasConsumedPerHour = litersOfGasConsumedPerHour;
    }

    public DoubleParameter getAverageCatchability() {
        return averageCatchability;
    }

    public void setAverageCatchability(DoubleParameter averageCatchability) {
        this.averageCatchability = averageCatchability;
    }

    public DoubleParameter getSlopeLeft() {
        return slopeLeft;
    }

    public void setSlopeLeft(DoubleParameter slopeLeft) {
        this.slopeLeft = slopeLeft;
    }

    public DoubleParameter getSlopeRight() {
        return slopeRight;
    }

    public void setSlopeRight(DoubleParameter slopeRight) {
        this.slopeRight = slopeRight;
    }

    public DoubleParameter getLengthFullSelectivity() {
        return lengthFullSelectivity;
    }

    public void setLengthFullSelectivity(DoubleParameter lengthFullSelectivity) {
        this.lengthFullSelectivity = lengthFullSelectivity;
    }


}
