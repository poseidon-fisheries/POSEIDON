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

package uk.ac.ox.oxfish.utility.adaptation.probability;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * The probability of exploring increases whenever an exploration is successful and decreases otherwise
 * Created by carrknight on 8/28/15.
 */
public class ExplorationPenaltyProbability implements AdaptationProbability {


    private final FixedProbability delegate;
    /**
     * whenever the exploration succeeds, exploration probability is multiplied by by 1+incrementMultiplier. Whenever exploration
     * fails the exploration probability decreases by 1-incrementMultiplier
     */
    private double incrementMultiplier;
    private double explorationMinimum;


    public ExplorationPenaltyProbability(
        double explorationProbability, double imitationProbability,
        double incrementMultiplier, double explorationMinimum
    ) {
        this.delegate = new FixedProbability(explorationProbability, imitationProbability);
        this.incrementMultiplier = incrementMultiplier;
        this.explorationMinimum = explorationMinimum;
    }

    @Override
    public double getImitationProbability() {
        return delegate.getImitationProbability();
    }

    public void setImitationProbability(double imitationProbability) {
        delegate.setImitationProbability(imitationProbability);
    }

    /**
     * register ata gatherer
     *
     * @param model
     */
    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model, fisher);
        fisher.getDailyData().registerGatherer(
            "Exploration Probability",
            (Gatherer<Fisher>) fisher1 -> ExplorationPenaltyProbability.this.getExplorationProbability(),
            Double.NaN
        );
    }

    @Override
    public double getExplorationProbability() {
        return delegate.getExplorationProbability();
    }

    public void setExplorationProbability(double explorationProbability) {
        delegate.setExplorationProbability(explorationProbability);
    }

    /**
     * ignored
     *
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
    }

    @Override
    public void judgeExploration(double previousFitness, double currentFitness) {
        if (currentFitness > previousFitness + FishStateUtilities.EPSILON)
            delegate.setExplorationProbability(Math.min(
                delegate.getExplorationProbability() * (1d + incrementMultiplier),
                1
            ));
        if (currentFitness < previousFitness - FishStateUtilities.EPSILON)
            delegate.setExplorationProbability(
                Math.max(delegate.getExplorationProbability() * (1d - incrementMultiplier), explorationMinimum));

        assert delegate.getExplorationProbability() >= explorationMinimum;
        assert delegate.getExplorationProbability() >= 0;

        delegate.judgeExploration(previousFitness, currentFitness);
    }
}
