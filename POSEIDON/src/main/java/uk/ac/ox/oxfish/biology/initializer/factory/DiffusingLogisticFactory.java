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

import uk.ac.ox.oxfish.biology.SmoothMovementRule;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantBiomassAllocator;
import uk.ac.ox.oxfish.biology.initializer.allocator.RandomAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates a DiffusingLogisticInitializer
 * Created by carrknight on 6/22/15.
 */
public class DiffusingLogisticFactory implements AlgorithmFactory<SingleSpeciesBiomassInitializer> {


    private DoubleParameter carryingCapacity = new FixedDoubleParameter(5000);


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
    private String speciesName = "Species 0";
    private AlgorithmFactory<? extends LogisticGrowerInitializer> grower = new SimpleLogisticGrowerFactory(0.6, 0.8);

    public DiffusingLogisticFactory() {
    }

    public DiffusingLogisticFactory(final double carryingCapacity) {
        setCarryingCapacity(new FixedDoubleParameter(carryingCapacity));
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SingleSpeciesBiomassInitializer apply(final FishState state) {
        final double carryingCapacity = getCarryingCapacity().applyAsDouble(state.getRandom());
        final double actualMaxCapacity = maxInitialCapacity.applyAsDouble(state.getRandom());
        final double actualMinCapacity = minInitialCapacity.applyAsDouble(state.getRandom());
        final SingleSpeciesBiomassInitializer initializer = new SingleSpeciesBiomassInitializer(
            //if initial capacities are the same, just always return the same thing
            actualMaxCapacity == actualMinCapacity ?
                new ConstantBiomassAllocator(
                    actualMaxCapacity * carryingCapacity
                ) :
                new RandomAllocator(
                    carryingCapacity * actualMaxCapacity,
                    carryingCapacity * actualMinCapacity
                ),
            new ConstantBiomassAllocator(carryingCapacity),
            new SmoothMovementRule(

                percentageLimitOnDailyMovement.applyAsDouble(state.getRandom()),
                differentialPercentageToMove.applyAsDouble(state.getRandom())
            ),
            speciesName,
            grower.apply(state), false
        );


        return initializer;
    }

    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }

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

    public DoubleParameter getPercentageLimitOnDailyMovement() {
        return percentageLimitOnDailyMovement;
    }

    public void setPercentageLimitOnDailyMovement(
        final DoubleParameter percentageLimitOnDailyMovement
    ) {
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
    }

    public DoubleParameter getDifferentialPercentageToMove() {
        return differentialPercentageToMove;
    }

    public void setDifferentialPercentageToMove(
        final DoubleParameter differentialPercentageToMove
    ) {
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
}
