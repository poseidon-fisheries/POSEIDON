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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.SmoothMovementRule;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Create biomass slope but with diffusion
 * Created by carrknight on 2/12/16.
 */
public class FromLeftToRightLogisticFactory implements AlgorithmFactory<SingleSpeciesBiomassInitializer> {


    private DoubleParameter carryingCapacity = new FixedDoubleParameter(5000);

    private AlgorithmFactory<? extends LogisticGrowerInitializer> grower = new SimpleLogisticGrowerFactory(0.6, 0.8);


    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private DoubleParameter percentageLimitOnDailyMovement = new FixedDoubleParameter(0.01);

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private DoubleParameter differentialPercentageToMove = new FixedDoubleParameter(0.001);

    private DoubleParameter maxInitialCapacity = new FixedDoubleParameter(1.0);

    private DoubleParameter minInitialCapacity = new FixedDoubleParameter(0.0);


    DoubleParameter minCapacityRatio = new FixedDoubleParameter(.2);

    DoubleParameter exponent = new FixedDoubleParameter(1);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SingleSpeciesBiomassInitializer apply(FishState state) {

        double exponent = getExponent().apply(state.getRandom());
        double minCapacityRatio = getMinCapacityRatio().apply(state.getRandom());
        double maxCapacity = getCarryingCapacity().apply(state.getRandom());
        BiomassAllocator leftToRightAllocator =
                new BiomassAllocator() {
                    @Override
                    public double allocate(
                            SeaTile tile, NauticalMap map, MersenneTwisterFast random)
                    {

                        double correctRatio = Math.max(
                                Math.pow(
                                        (map.getWidth()-tile.getGridX())
                                                /
                                                (float)map.getWidth(),
                                        exponent),
                                minCapacityRatio);
                        return correctRatio * maxCapacity;
                    }
                };


        return new SingleSpeciesBiomassInitializer(
                leftToRightAllocator,
                leftToRightAllocator,
                new SmoothMovementRule(

                        percentageLimitOnDailyMovement.apply(state.getRandom()),
                        differentialPercentageToMove.apply(state.getRandom())
                ),
                "Species 0",
                getGrower().apply(state),

                false);
    }


    /**
     * Getter for property 'carryingCapacity'.
     *
     * @return Value for property 'carryingCapacity'.
     */
    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }

    /**
     * Setter for property 'carryingCapacity'.
     *
     * @param carryingCapacity Value to set for property 'carryingCapacity'.
     */
    public void setCarryingCapacity(DoubleParameter carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    /**
     * Getter for property 'grower'.
     *
     * @return Value for property 'grower'.
     */
    public AlgorithmFactory<? extends LogisticGrowerInitializer> getGrower() {
        return grower;
    }

    /**
     * Setter for property 'grower'.
     *
     * @param grower Value to set for property 'grower'.
     */
    public void setGrower(
            AlgorithmFactory<? extends LogisticGrowerInitializer> grower) {
        this.grower = grower;
    }

    /**
     * Getter for property 'percentageLimitOnDailyMovement'.
     *
     * @return Value for property 'percentageLimitOnDailyMovement'.
     */
    public DoubleParameter getPercentageLimitOnDailyMovement() {
        return percentageLimitOnDailyMovement;
    }

    /**
     * Setter for property 'percentageLimitOnDailyMovement'.
     *
     * @param percentageLimitOnDailyMovement Value to set for property 'percentageLimitOnDailyMovement'.
     */
    public void setPercentageLimitOnDailyMovement(
            DoubleParameter percentageLimitOnDailyMovement) {
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
    }

    /**
     * Getter for property 'differentialPercentageToMove'.
     *
     * @return Value for property 'differentialPercentageToMove'.
     */
    public DoubleParameter getDifferentialPercentageToMove() {
        return differentialPercentageToMove;
    }

    /**
     * Setter for property 'differentialPercentageToMove'.
     *
     * @param differentialPercentageToMove Value to set for property 'differentialPercentageToMove'.
     */
    public void setDifferentialPercentageToMove(DoubleParameter differentialPercentageToMove) {
        this.differentialPercentageToMove = differentialPercentageToMove;
    }

    /**
     * Getter for property 'minCapacityRatio'.
     *
     * @return Value for property 'minCapacityRatio'.
     */
    public DoubleParameter getMinCapacityRatio() {
        return minCapacityRatio;
    }

    /**
     * Setter for property 'minCapacityRatio'.
     *
     * @param minCapacityRatio Value to set for property 'minCapacityRatio'.
     */
    public void setMinCapacityRatio(DoubleParameter minCapacityRatio) {
        this.minCapacityRatio = minCapacityRatio;
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
