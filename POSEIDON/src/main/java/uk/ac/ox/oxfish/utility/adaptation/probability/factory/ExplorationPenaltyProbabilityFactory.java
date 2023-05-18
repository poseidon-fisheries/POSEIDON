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

package uk.ac.ox.oxfish.utility.adaptation.probability.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.ExplorationPenaltyProbability;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates an adaptation probability that increases as exploration works and decreases otherwise
 * Created by carrknight on 8/28/15.
 */
public class ExplorationPenaltyProbabilityFactory
    implements AlgorithmFactory<ExplorationPenaltyProbability> {

    private DoubleParameter explorationProbability = new FixedDoubleParameter(.8);

    private DoubleParameter imitationProbability = new FixedDoubleParameter(1);

    private DoubleParameter incrementMultiplier = new FixedDoubleParameter(.02);

    private DoubleParameter explorationProbabilityMinimum = new FixedDoubleParameter(.01);


    public ExplorationPenaltyProbabilityFactory() {
    }


    public ExplorationPenaltyProbabilityFactory(
        double explorationProbability, double imitationProbability,
        double incrementMultiplier,
        double explorationProbabilityMinimum
    ) {
        this.explorationProbability = new FixedDoubleParameter(explorationProbability);
        this.imitationProbability = new FixedDoubleParameter(imitationProbability);
        this.incrementMultiplier = new FixedDoubleParameter(incrementMultiplier);
        this.explorationProbabilityMinimum = new FixedDoubleParameter(explorationProbabilityMinimum);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ExplorationPenaltyProbability apply(FishState state) {
        return new ExplorationPenaltyProbability(
            explorationProbability.applyAsDouble(state.getRandom()),
            imitationProbability.applyAsDouble(state.getRandom()),
            incrementMultiplier.applyAsDouble(state.getRandom()),
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

    public DoubleParameter getIncrementMultiplier() {
        return incrementMultiplier;
    }

    public void setIncrementMultiplier(DoubleParameter incrementMultiplier) {
        this.incrementMultiplier = incrementMultiplier;
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
