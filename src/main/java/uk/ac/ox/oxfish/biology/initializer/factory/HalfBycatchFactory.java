package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.DiffusingLogisticInitializer;
import uk.ac.ox.oxfish.biology.initializer.HalfBycatchInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * Creates a biology initializer that fills half map with 2 species and the other half with only 1 specie
 * Created by carrknight on 7/30/15.
 */
public class HalfBycatchFactory implements AlgorithmFactory<HalfBycatchInitializer> {


    private DoubleParameter carryingCapacity = new FixedDoubleParameter(5000);

    private DoubleParameter steepness = new UniformDoubleParameter(0.6,0.8);


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
    public HalfBycatchInitializer apply(FishState state) {
        return new HalfBycatchInitializer(carryingCapacity,
                                                steepness,
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
}
