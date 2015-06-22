package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.DiffusingLogisticInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates a DiffusingLogisticInitializer
 * Created by carrknight on 6/22/15.
 */
public class DiffusingLogisticFactory implements StrategyFactory<DiffusingLogisticInitializer>
{


    private DoubleParameter carryingCapacity = new FixedDoubleParameter(5000);

    private double minSteepness = 0.6;

    private double maxSteepness=0.8;

    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private double percentageLimitOnDailyMovement = 0.01;

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private double differentialPercentageToMove = 0.0005;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DiffusingLogisticInitializer apply(FishState state) {
        return new DiffusingLogisticInitializer(carryingCapacity.apply(state.random),
                                                minSteepness,
                                                maxSteepness,
                                                percentageLimitOnDailyMovement,
                                                differentialPercentageToMove);
    }

    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(DoubleParameter carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    public double getMinSteepness() {
        return minSteepness;
    }

    public void setMinSteepness(double minSteepness) {
        this.minSteepness = minSteepness;
    }

    public double getMaxSteepness() {
        return maxSteepness;
    }

    public void setMaxSteepness(double maxSteepness) {
        this.maxSteepness = maxSteepness;
    }

    public double getPercentageLimitOnDailyMovement() {
        return percentageLimitOnDailyMovement;
    }

    public void setPercentageLimitOnDailyMovement(double percentageLimitOnDailyMovement) {
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
    }

    public double getDifferentialPercentageToMove() {
        return differentialPercentageToMove;
    }

    public void setDifferentialPercentageToMove(double differentialPercentageToMove) {
        this.differentialPercentageToMove = differentialPercentageToMove;
    }
}
