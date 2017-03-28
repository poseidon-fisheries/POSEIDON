package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.initializer.FromLeftToRightLogisticInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;

/**
 * Create biomass slope but with diffusion
 * Created by carrknight on 2/12/16.
 */
public class FromLeftToRightLogisticFactory implements AlgorithmFactory<FromLeftToRightLogisticInitializer> {


    DiffusingLogisticFactory delegate = new DiffusingLogisticFactory();


    DoubleParameter minCapacityRatio = new FixedDoubleParameter(.2);

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public FromLeftToRightLogisticInitializer apply(FishState fishState) {
        return new FromLeftToRightLogisticInitializer(delegate.apply(fishState),
                                                      minCapacityRatio.apply(fishState.getRandom()));
    }

    public DoubleParameter getPercentageLimitOnDailyMovement() {
        return delegate.getPercentageLimitOnDailyMovement();
    }

    public DoubleParameter getDifferentialPercentageToMove() {
        return delegate.getDifferentialPercentageToMove();
    }

    public void setDifferentialPercentageToMove(
            DoubleParameter differentialPercentageToMove) {
        delegate.setDifferentialPercentageToMove(differentialPercentageToMove);
    }


    public void setCarryingCapacity(DoubleParameter carryingCapacity) {
        delegate.setCarryingCapacity(carryingCapacity);
    }




    public DoubleParameter getCarryingCapacity() {
        return delegate.getCarryingCapacity();
    }

    /**
     * Returns a function that always returns its input argument.
     *
     * @return a function that always returns its input argument
     */
    public static <T> Function<T, T> identity() {
        return Function.identity();
    }

    public void setPercentageLimitOnDailyMovement(
            DoubleParameter percentageLimitOnDailyMovement) {
        delegate.setPercentageLimitOnDailyMovement(percentageLimitOnDailyMovement);
    }


    public DoubleParameter getMinCapacityRatio() {
        return minCapacityRatio;
    }

    public void setMinCapacityRatio(DoubleParameter minCapacityRatio) {
        this.minCapacityRatio = minCapacityRatio;
    }


    /**
     * Getter for property 'grower'.
     *
     * @return Value for property 'grower'.
     */
    public AlgorithmFactory<? extends LogisticGrowerInitializer> getGrower() {
        return delegate.getGrower();
    }

    /**
     * Setter for property 'grower'.
     *
     * @param grower Value to set for property 'grower'.
     */
    public void setGrower(
            AlgorithmFactory<? extends LogisticGrowerInitializer> grower) {
        delegate.setGrower(grower);
    }

    /**
     * Getter for property 'maxInitialCapacity'.
     *
     * @return Value for property 'maxInitialCapacity'.
     */
    public DoubleParameter getMaxInitialCapacity() {
        return delegate.getMaxInitialCapacity();
    }

    /**
     * Setter for property 'maxInitialCapacity'.
     *
     * @param maxInitialCapacity Value to set for property 'maxInitialCapacity'.
     */
    public void setMaxInitialCapacity(DoubleParameter maxInitialCapacity) {
        delegate.setMaxInitialCapacity(maxInitialCapacity);
    }

    /**
     * Getter for property 'minInitialCapacity'.
     *
     * @return Value for property 'minInitialCapacity'.
     */
    public DoubleParameter getMinInitialCapacity() {
        return delegate.getMinInitialCapacity();
    }

    /**
     * Setter for property 'minInitialCapacity'.
     *
     * @param minInitialCapacity Value to set for property 'minInitialCapacity'.
     */
    public void setMinInitialCapacity(DoubleParameter minInitialCapacity) {
        delegate.setMinInitialCapacity(minInitialCapacity);
    }
}
