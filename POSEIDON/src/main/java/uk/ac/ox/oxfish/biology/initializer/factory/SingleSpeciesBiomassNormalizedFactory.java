/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

import uk.ac.ox.oxfish.biology.NoMovement;
import uk.ac.ox.oxfish.biology.SmoothMovementRule;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.ConstantInitialBiomass;
import uk.ac.ox.oxfish.biology.initializer.PerCellInitialBiomass;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantAllocatorFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.RandomAllocatorFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * basically tightly bounds the amount of K and initial biomass available by normalizing the allocators
 */
public class SingleSpeciesBiomassNormalizedFactory implements AlgorithmFactory<SingleSpeciesBiomassInitializer> {


    private AlgorithmFactory<? extends BiomassAllocator> initialBiomassAllocator = new
        RandomAllocatorFactory(
        0,
        1
    );

    private AlgorithmFactory<? extends BiomassAllocator> initialCapacityAllocator =
        new ConstantAllocatorFactory(1);


    private DoubleParameter carryingCapacity = new FixedDoubleParameter(5000);


    /**
     * when this is true, the initial biomass and carrying capacity provided are meant to be taken as "per cell" (that is, are multiplied by the number of tiles available).
     * When this is false, the initial biomass and carrying capacity are assumed to be ocean-wide
     */
    private boolean biomassSuppliedPerCell = true;


    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private DoubleParameter percentageLimitOnDailyMovement = new FixedDoubleParameter(0.01);

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private DoubleParameter differentialPercentageToMove = new FixedDoubleParameter(0.001);

    private String speciesName = "Species 0";


    private boolean unfishable = false;
    private AlgorithmFactory<? extends LogisticGrowerInitializer> grower = new SimpleLogisticGrowerFactory(0.6, 0.8);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SingleSpeciesBiomassInitializer apply(final FishState state) {


        final double movementRate = differentialPercentageToMove.applyAsDouble(state.getRandom());
        final double movementLimit = percentageLimitOnDailyMovement.applyAsDouble(state.getRandom());


        final Double actualCarryingCapacity = carryingCapacity.applyAsDouble(state.getRandom());
        return new SingleSpeciesBiomassInitializer(
            new ConstantInitialBiomass(Double.NaN),
            initialBiomassAllocator.apply(state),
            biomassSuppliedPerCell ? new PerCellInitialBiomass(actualCarryingCapacity) :
                new ConstantInitialBiomass(actualCarryingCapacity),
            initialCapacityAllocator.apply(state),
            movementRate > 0 & movementLimit > 0 ?
                new SmoothMovementRule(
                    percentageLimitOnDailyMovement.applyAsDouble(state.getRandom()),
                    differentialPercentageToMove.applyAsDouble(state.getRandom())
                ) :
                new NoMovement(),
            speciesName,
            speciesName,
            grower.apply(state),
            true,
            unfishable
        );


    }

    /**
     * Getter for property 'initialBiomassAllocator'.
     *
     * @return Value for property 'initialBiomassAllocator'.
     */
    public AlgorithmFactory<? extends BiomassAllocator> getInitialBiomassAllocator() {
        return initialBiomassAllocator;
    }

    /**
     * Setter for property 'initialBiomassAllocator'.
     *
     * @param initialBiomassAllocator Value to set for property 'initialBiomassAllocator'.
     */
    public void setInitialBiomassAllocator(
        final AlgorithmFactory<? extends BiomassAllocator> initialBiomassAllocator
    ) {
        this.initialBiomassAllocator = initialBiomassAllocator;
    }

    /**
     * Getter for property 'initialCapacityAllocator'.
     *
     * @return Value for property 'initialCapacityAllocator'.
     */
    public AlgorithmFactory<? extends BiomassAllocator> getInitialCapacityAllocator() {
        return initialCapacityAllocator;
    }

    /**
     * Setter for property 'initialCapacityAllocator'.
     *
     * @param initialCapacityAllocator Value to set for property 'initialCapacityAllocator'.
     */
    public void setInitialCapacityAllocator(
        final AlgorithmFactory<? extends BiomassAllocator> initialCapacityAllocator
    ) {
        this.initialCapacityAllocator = initialCapacityAllocator;
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
     * Getter for property 'speciesName'.
     *
     * @return Value for property 'speciesName'.
     */
    public String getSpeciesName() {
        return speciesName;
    }

    /**
     * Setter for property 'speciesName'.
     *
     * @param speciesName Value to set for property 'speciesName'.
     */
    public void setSpeciesName(final String speciesName) {
        this.speciesName = speciesName;
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
     * Getter for property 'biomassSuppliedPerCell'.
     *
     * @return Value for property 'biomassSuppliedPerCell'.
     */
    public boolean isBiomassSuppliedPerCell() {
        return biomassSuppliedPerCell;
    }

    /**
     * Setter for property 'biomassSuppliedPerCell'.
     *
     * @param biomassSuppliedPerCell Value to set for property 'biomassSuppliedPerCell'.
     */
    public void setBiomassSuppliedPerCell(final boolean biomassSuppliedPerCell) {
        this.biomassSuppliedPerCell = biomassSuppliedPerCell;
    }

    /**
     * Getter for property 'unfishable'.
     *
     * @return Value for property 'unfishable'.
     */
    public boolean isUnfishable() {
        return unfishable;
    }

    /**
     * Setter for property 'unfishable'.
     *
     * @param unfishable Value to set for property 'unfishable'.
     */
    public void setUnfishable(final boolean unfishable) {
        this.unfishable = unfishable;
    }
}
