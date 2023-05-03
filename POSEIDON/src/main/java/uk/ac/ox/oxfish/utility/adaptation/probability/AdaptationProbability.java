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

package uk.ac.ox.oxfish.utility.adaptation.probability;

import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * The object managing and evolving the adaptation probabilities
 * Created by carrknight on 8/28/15.
 */
public interface AdaptationProbability extends FisherStartable
{

    /**
     * get probability of exploring
     */
    double getExplorationProbability();


    /**
     * get probability of imitating
     */
    double getImitationProbability();

    /**
     * react to what the result of the exploration was and see if it changes your probabilities.
     * @param previousFitness pre-exploration fitness
     * @param currentFitness post-exploration fitness
     */
    void judgeExploration(double previousFitness, double currentFitness);


}
