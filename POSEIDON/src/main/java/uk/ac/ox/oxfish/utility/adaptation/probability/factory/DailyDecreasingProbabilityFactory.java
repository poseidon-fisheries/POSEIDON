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

package uk.ac.ox.oxfish.utility.adaptation.probability.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.DailyDecreasingProbability;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Creates an adaptation probability that decreases over time
 * Created by carrknight on 8/28/15.
 */
public class DailyDecreasingProbabilityFactory implements AlgorithmFactory<DailyDecreasingProbability> {

    private DoubleParameter explorationProbability = new FixedDoubleParameter(.8);

    private DoubleParameter imitationProbability = new FixedDoubleParameter(1);

    private DoubleParameter dailyDecreaseMultiplier = new FixedDoubleParameter(.99);

    private DoubleParameter explorationProbabilityMinimum = new FixedDoubleParameter(.01);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DailyDecreasingProbability apply(FishState state) {
        return new DailyDecreasingProbability(
            explorationProbability.applyAsDouble(state.getRandom()),
            imitationProbability.applyAsDouble(state.getRandom()),
            dailyDecreaseMultiplier.applyAsDouble(state.getRandom()),
            explorationProbabilityMinimum.applyAsDouble(state.getRandom())
        );
    }


    public DoubleParameter getExplorationProbability() {
        return explorationProbability;
    }

    public void setExplorationProbability(DoubleParameter explorationProbability) {
        this.explorationProbability = explorationProbability;
    }

    public DoubleParameter getImitationProbability() {
        return imitationProbability;
    }

    public void setImitationProbability(DoubleParameter imitationProbability) {
        this.imitationProbability = imitationProbability;
    }

    public DoubleParameter getDailyDecreaseMultiplier() {
        return dailyDecreaseMultiplier;
    }

    public void setDailyDecreaseMultiplier(DoubleParameter dailyDecreaseMultiplier) {
        this.dailyDecreaseMultiplier = dailyDecreaseMultiplier;
    }

    public DoubleParameter getExplorationProbabilityMinimum() {
        return explorationProbabilityMinimum;
    }

    public void setExplorationProbabilityMinimum(
        DoubleParameter explorationProbabilityMinimum
    ) {
        this.explorationProbabilityMinimum = explorationProbabilityMinimum;
    }
}
