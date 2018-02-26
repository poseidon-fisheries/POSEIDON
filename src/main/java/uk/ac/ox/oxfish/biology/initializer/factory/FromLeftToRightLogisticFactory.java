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

    DoubleParameter exponent = new FixedDoubleParameter(1);

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public FromLeftToRightLogisticInitializer apply(FishState fishState) {
        return new FromLeftToRightLogisticInitializer(delegate.apply(fishState),
                                                      minCapacityRatio.apply(fishState.getRandom()),
                                                      exponent.apply(fishState.getRandom()));
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

    /**
     * Getter for property 'exponent'.
     *
     * @return Value for property 'exponent'.
     */
    public DoubleParameter getExponent() {
        return exponent;
    }

    /**
     * Setter for property 'exponent'.
     *
     * @param exponent Value to set for property 'exponent'.
     */
    public void setExponent(DoubleParameter exponent) {
        this.exponent = exponent;
    }
}
