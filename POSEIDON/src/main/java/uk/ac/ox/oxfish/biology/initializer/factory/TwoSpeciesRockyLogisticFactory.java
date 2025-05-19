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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.TwoSpeciesRockyLogisticInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/26/16.
 */
public class TwoSpeciesRockyLogisticFactory implements AlgorithmFactory<TwoSpeciesRockyLogisticInitializer> {


    private DoubleParameter sandyCarryingCapacity = new FixedDoubleParameter(2000);

    private DoubleParameter rockyCarryingCapacity = new FixedDoubleParameter(10000);


    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private DoubleParameter percentageLimitOnDailyMovement = new FixedDoubleParameter(0.01);

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private DoubleParameter differentialPercentageToMove = new FixedDoubleParameter(0.001);
    private AlgorithmFactory<? extends LogisticGrowerInitializer> grower = new SimpleLogisticGrowerFactory(0.6, 0.8);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public TwoSpeciesRockyLogisticInitializer apply(final FishState state) {
        final MersenneTwisterFast random = state.getRandom();
        return new TwoSpeciesRockyLogisticInitializer(
            rockyCarryingCapacity,
            sandyCarryingCapacity,
            percentageLimitOnDailyMovement.applyAsDouble(random),
            differentialPercentageToMove.applyAsDouble(random),
            grower.apply(state)
        );
    }

    public DoubleParameter getSandyCarryingCapacity() {
        return sandyCarryingCapacity;
    }

    public void setSandyCarryingCapacity(final DoubleParameter sandyCarryingCapacity) {
        this.sandyCarryingCapacity = sandyCarryingCapacity;
    }

    public DoubleParameter getRockyCarryingCapacity() {
        return rockyCarryingCapacity;
    }

    public void setRockyCarryingCapacity(final DoubleParameter rockyCarryingCapacity) {
        this.rockyCarryingCapacity = rockyCarryingCapacity;
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


}
