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
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.DiffusingLogisticInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates a DiffusingLogisticInitializer
 * Created by carrknight on 6/22/15.
 */
public class DiffusingLogisticFactory implements AlgorithmFactory<DiffusingLogisticInitializer>
{


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

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DiffusingLogisticInitializer apply(FishState state) {
        return new DiffusingLogisticInitializer(carryingCapacity,
                                                minInitialCapacity,
                                                maxInitialCapacity,
                                                percentageLimitOnDailyMovement.apply(state.random),
                                                differentialPercentageToMove.apply(state.random),
                                                grower.apply(state));
    }

    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(DoubleParameter carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }


    private AlgorithmFactory<? extends LogisticGrowerInitializer> grower = new SimpleLogisticGrowerFactory(0.6, 0.8);

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
