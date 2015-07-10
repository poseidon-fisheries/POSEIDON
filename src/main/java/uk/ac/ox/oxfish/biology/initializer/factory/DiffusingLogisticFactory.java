package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.DiffusingLogisticInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates a DiffusingLogisticInitializer
 * Created by carrknight on 6/22/15.
 */
public class DiffusingLogisticFactory implements AlgorithmFactory<DiffusingLogisticInitializer>
{


    private DoubleParameter carryingCapacity = new FixedDoubleParameter(5000);

    private DoubleParameter minSteepness = new FixedDoubleParameter(0.6);

    private DoubleParameter maxSteepness=new FixedDoubleParameter(0.8);

    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private DoubleParameter percentageLimitOnDailyMovement =new FixedDoubleParameter(0.01);

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private DoubleParameter differentialPercentageToMove =new FixedDoubleParameter(0.0005);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DiffusingLogisticInitializer apply(FishState state) {
        return new DiffusingLogisticInitializer(carryingCapacity.apply(state.random),
                                                minSteepness.apply(state.random),
                                                maxSteepness.apply(state.random),
                                                percentageLimitOnDailyMovement.apply(state.random),
                                                differentialPercentageToMove.apply(state.random));
    }

    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(DoubleParameter carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    public DoubleParameter getMinSteepness() {
        return minSteepness;
    }

    public void setMinSteepness(DoubleParameter minSteepness) {
        this.minSteepness = minSteepness;
    }

    public DoubleParameter getMaxSteepness() {
        return maxSteepness;
    }

    public void setMaxSteepness(DoubleParameter maxSteepness) {
        this.maxSteepness = maxSteepness;
    }

    public DoubleParameter getPercentageLimitOnDailyMovement() {
        return percentageLimitOnDailyMovement;
    }

    public void setPercentageLimitOnDailyMovement(
            DoubleParameter percentageLimitOnDailyMovement) {
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
    }

    public DoubleParameter getDifferentialPercentageToMove() {
        return differentialPercentageToMove;
    }

    public void setDifferentialPercentageToMove(
            DoubleParameter differentialPercentageToMove) {
        this.differentialPercentageToMove = differentialPercentageToMove;
    }
}
