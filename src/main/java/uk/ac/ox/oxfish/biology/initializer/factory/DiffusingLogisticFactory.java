package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.DiffusingLogisticInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * Creates a DiffusingLogisticInitializer
 * Created by carrknight on 6/22/15.
 */
public class DiffusingLogisticFactory implements AlgorithmFactory<DiffusingLogisticInitializer>
{


    private DoubleParameter carryingCapacity = new FixedDoubleParameter(5000);

    private DoubleParameter steepness = new UniformDoubleParameter(0.6,0.8);


    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private DoubleParameter percentageLimitOnDailyMovement = new FixedDoubleParameter(0.01);

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private DoubleParameter differentialPercentageToMove = new FixedDoubleParameter(0.001);


    private DoubleParameter maxInitialCapacity = new FixedDoubleParameter(1d);

    private DoubleParameter minInitialCapacity = new FixedDoubleParameter(0d);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DiffusingLogisticInitializer apply(FishState state) {
        return new DiffusingLogisticInitializer(carryingCapacity,
                                                steepness,
                                                maxInitialCapacity,
                                                minInitialCapacity,
                                                percentageLimitOnDailyMovement.apply(state.random),
                                                differentialPercentageToMove.apply(state.random));
    }

    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(DoubleParameter carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }


    public DoubleParameter getSteepness() {
        return steepness;
    }

    public void setSteepness(DoubleParameter steepness) {
        this.steepness = steepness;
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

    /**
     * Getter for property 'maxInitialCapacity'.
     *
     * @return Value for property 'maxInitialCapacity'.
     */
    public DoubleParameter getMaxInitialCapacity() {
        return maxInitialCapacity;
    }

    /**
     * Setter for property 'maxInitialCapacity'.
     *
     * @param maxInitialCapacity Value to set for property 'maxInitialCapacity'.
     */
    public void setMaxInitialCapacity(DoubleParameter maxInitialCapacity) {
        this.maxInitialCapacity = maxInitialCapacity;
    }

    /**
     * Getter for property 'minInitialCapacity'.
     *
     * @return Value for property 'minInitialCapacity'.
     */
    public DoubleParameter getMinInitialCapacity() {
        return minInitialCapacity;
    }

    /**
     * Setter for property 'minInitialCapacity'.
     *
     * @param minInitialCapacity Value to set for property 'minInitialCapacity'.
     */
    public void setMinInitialCapacity(DoubleParameter minInitialCapacity) {
        this.minInitialCapacity = minInitialCapacity;
    }
}
