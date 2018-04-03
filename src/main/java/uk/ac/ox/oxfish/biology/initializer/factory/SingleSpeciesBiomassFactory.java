/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

import uk.ac.ox.oxfish.biology.NoMovement;
import uk.ac.ox.oxfish.biology.SmoothMovementRule;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class SingleSpeciesBiomassFactory implements AlgorithmFactory<SingleSpeciesBiomassInitializer>{



    private AlgorithmFactory<? extends BiomassAllocator> initialBiomassAllocator = new
            RandomAllocatorFactory(
            0,
            5000
    );

    private AlgorithmFactory<? extends BiomassAllocator> initialCapacityAllocator =
            new ConstantAllocatorFactory(5000d);


    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private DoubleParameter percentageLimitOnDailyMovement = new FixedDoubleParameter(0.01);

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private DoubleParameter differentialPercentageToMove = new FixedDoubleParameter(0.001);

    private String speciesName = "Species 0";

    private AlgorithmFactory<? extends LogisticGrowerInitializer> grower = new SimpleLogisticGrowerFactory(0.6, 0.8);


    /**Si
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SingleSpeciesBiomassInitializer apply(FishState state) {


        double  movementRate = differentialPercentageToMove.apply(state.getRandom());
        double  movementLimit = percentageLimitOnDailyMovement.apply(state.getRandom());

        return new SingleSpeciesBiomassInitializer(
                initialBiomassAllocator.apply(state),
                initialCapacityAllocator.apply(state),
                movementRate > 0 & movementLimit > 0 ?
                        new SmoothMovementRule(

                                percentageLimitOnDailyMovement.apply(state.getRandom()),
                                differentialPercentageToMove.apply(state.getRandom())
                        ) :
                        new NoMovement(),
                speciesName,
                grower.apply(state)


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
            AlgorithmFactory<? extends BiomassAllocator> initialBiomassAllocator) {
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
            AlgorithmFactory<? extends BiomassAllocator> initialCapacityAllocator) {
        this.initialCapacityAllocator = initialCapacityAllocator;
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
    public void setSpeciesName(String speciesName) {
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
            AlgorithmFactory<? extends LogisticGrowerInitializer> grower) {
        this.grower = grower;
    }
}
