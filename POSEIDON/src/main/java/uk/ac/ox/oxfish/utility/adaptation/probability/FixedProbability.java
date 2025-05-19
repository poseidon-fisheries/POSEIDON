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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Probability is fixed and never changes (unless set exogenously)
 * Created by carrknight on 8/28/15.
 */
public class FixedProbability implements AdaptationProbability {


    private double explorationProbability;

    private double imitationProbability;


    public FixedProbability(double explorationProbability, double imitationProbability) {
        Preconditions.checkArgument(explorationProbability <= 1);
        Preconditions.checkArgument(imitationProbability <= 1);
        Preconditions.checkArgument(explorationProbability >= 0);
        Preconditions.checkArgument(imitationProbability >= 0);
        this.explorationProbability = explorationProbability;
        this.imitationProbability = imitationProbability;
    }


    public double getExplorationProbability() {

        return explorationProbability;
    }

    public void setExplorationProbability(double explorationProbability) {
        this.explorationProbability = explorationProbability;
    }

    @Override
    public double getImitationProbability() {

        return imitationProbability;
    }

    public void setImitationProbability(double imitationProbability) {
        this.imitationProbability = imitationProbability;
    }

    @Override
    public void judgeExploration(double previousFitness, double currentFitness) {

    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model, Fisher fisher) {

    }

    /**
     * ignored
     *
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {

    }
}
