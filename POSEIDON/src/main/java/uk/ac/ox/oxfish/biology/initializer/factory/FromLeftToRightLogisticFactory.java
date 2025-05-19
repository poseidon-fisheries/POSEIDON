/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.SmoothMovementRule;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Create biomass slope but with diffusion
 * Created by carrknight on 2/12/16.
 */
public class FromLeftToRightLogisticFactory implements AlgorithmFactory<SingleSpeciesBiomassInitializer> {


    DoubleParameter minCapacityRatio = new FixedDoubleParameter(.2);
    DoubleParameter exponent = new FixedDoubleParameter(1);
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

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SingleSpeciesBiomassInitializer apply(final FishState state) {

        final double exponent = getExponent().applyAsDouble(state.getRandom());
        final double minCapacityRatio = getMinCapacityRatio().applyAsDouble(state.getRandom());
        final double maxCapacity = getCarryingCapacity().applyAsDouble(state.getRandom());
        final BiomassAllocator leftToRightAllocator =
            (tile, map, random) -> {

                final double correctRatio = Math.max(
                    Math.pow(
                        (map.getWidth() - tile.getGridX())
                            /
                            (float) map.getWidth(),
                        exponent
                    ),
                    minCapacityRatio
                );
                return correctRatio * maxCapacity;
            };


        return new SingleSpeciesBiomassInitializer(
            leftToRightAllocator,
            leftToRightAllocator,
            new SmoothMovementRule(
                percentageLimitOnDailyMovement.applyAsDouble(state.getRandom()),
                differentialPercentageToMove.applyAsDouble(state.getRandom())
            ),
            "Species 0",
            getGrower().apply(state),

            false
        );
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
     * Getter for property 'minCapacityRatio'.
     *
     * @return Value for property 'minCapacityRatio'.
     */
    public DoubleParameter getMinCapacityRatio() {
        return minCapacityRatio;
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
    public void setCarryingCapacity(final DoubleParameter carryingCapacity) {
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
        final AlgorithmFactory<? extends LogisticGrowerInitializer> grower
    ) {
        this.grower = grower;
    }

    /**
     * Setter for property 'minCapacityRatio'.
     *
     * @param minCapacityRatio Value to set for property 'minCapacityRatio'.
     */
    public void setMinCapacityRatio(final DoubleParameter minCapacityRatio) {
        this.minCapacityRatio = minCapacityRatio;
    }

    /**
     * Setter for property 'exponent'.
     *
     * @param exponent Value to set for property 'exponent'.
     */
    public void setExponent(final DoubleParameter exponent) {
        this.exponent = exponent;
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
        final DoubleParameter percentageLimitOnDailyMovement
    ) {
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
    public void setDifferentialPercentageToMove(final DoubleParameter differentialPercentageToMove) {
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
    public void setMaxInitialCapacity(final DoubleParameter maxInitialCapacity) {
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
    public void setMinInitialCapacity(final DoubleParameter minInitialCapacity) {
        this.minInitialCapacity = minInitialCapacity;
    }
}
