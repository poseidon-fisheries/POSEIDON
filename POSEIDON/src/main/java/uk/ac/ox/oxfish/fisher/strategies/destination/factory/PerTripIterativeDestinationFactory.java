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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.selfanalysis.HourlyProfitInTripObjective;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.PerTripIterativeDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.maximization.DefaultBeamHillClimbing;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * creates a per-trip iterative destination strategy
 * Created by carrknight on 6/19/15.
 */
public class PerTripIterativeDestinationFactory implements AlgorithmFactory<PerTripIterativeDestinationStrategy> {


    DoubleParameter stepSize = new FixedDoubleParameter(5d);

    DoubleParameter stayingStillProbability = new FixedDoubleParameter(0d);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PerTripIterativeDestinationStrategy apply(final FishState state) {

        final MersenneTwisterFast random = state.getRandom();
        final NauticalMap map = state.getMap();


        final DefaultBeamHillClimbing algorithm = new DefaultBeamHillClimbing(
            (int) stepSize.applyAsDouble(random),
            20
        );
        return new PerTripIterativeDestinationStrategy(
            new FavoriteDestinationStrategy(map, random),
            algorithm,
            1d - stayingStillProbability.applyAsDouble(random),
            0d,
            new HourlyProfitInTripObjective(),
            a -> true
        );

    }

    public DoubleParameter getStepSize() {
        return stepSize;
    }

    public void setStepSize(final DoubleParameter stepSize) {
        this.stepSize = stepSize;
    }


    public DoubleParameter getStayingStillProbability() {
        return stayingStillProbability;
    }

    public void setStayingStillProbability(final DoubleParameter stayingStillProbability) {
        this.stayingStillProbability = stayingStillProbability;
    }
}
