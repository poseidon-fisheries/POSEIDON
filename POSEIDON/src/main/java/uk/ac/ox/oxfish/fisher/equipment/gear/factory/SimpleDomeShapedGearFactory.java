package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.SimplifiedDoubleNormalFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class SimpleDomeShapedGearFactory implements HomogeneousGearFactory {


    private boolean rounding = false;


    private DoubleParameter litersOfGasConsumedPerHour = new FixedDoubleParameter(0);

    private DoubleParameter averageCatchability = new FixedDoubleParameter(0);

    private DoubleParameter slopeLeft = new FixedDoubleParameter(5);
    private DoubleParameter slopeRight = new FixedDoubleParameter(10);
    private DoubleParameter lengthFullSelectivity = new FixedDoubleParameter(30);


    @Override
    public HomogeneousAbundanceGear apply(final FishState fishState) {

        final MersenneTwisterFast random = fishState.getRandom();

        return new HomogeneousAbundanceGear(
            litersOfGasConsumedPerHour.applyAsDouble(random),
            new FixedProportionFilter(averageCatchability.applyAsDouble(random), rounding),
            new SimplifiedDoubleNormalFilter(
                true,
                rounding,
                lengthFullSelectivity.applyAsDouble(random),
                slopeLeft.applyAsDouble(random),
                slopeRight.applyAsDouble(random)
            )
        );
    }

    public boolean isRounding() {
        return rounding;
    }

    public void setRounding(final boolean rounding) {
        this.rounding = rounding;
    }

    public DoubleParameter getLitersOfGasConsumedPerHour() {
        return litersOfGasConsumedPerHour;
    }

    public void setLitersOfGasConsumedPerHour(final DoubleParameter litersOfGasConsumedPerHour) {
        this.litersOfGasConsumedPerHour = litersOfGasConsumedPerHour;
    }

    public DoubleParameter getAverageCatchability() {
        return averageCatchability;
    }

    public void setAverageCatchability(final DoubleParameter averageCatchability) {
        this.averageCatchability = averageCatchability;
    }

    public DoubleParameter getSlopeLeft() {
        return slopeLeft;
    }

    public void setSlopeLeft(final DoubleParameter slopeLeft) {
        this.slopeLeft = slopeLeft;
    }

    public DoubleParameter getSlopeRight() {
        return slopeRight;
    }

    public void setSlopeRight(final DoubleParameter slopeRight) {
        this.slopeRight = slopeRight;
    }

    public DoubleParameter getLengthFullSelectivity() {
        return lengthFullSelectivity;
    }

    public void setLengthFullSelectivity(final DoubleParameter lengthFullSelectivity) {
        this.lengthFullSelectivity = lengthFullSelectivity;
    }


}
