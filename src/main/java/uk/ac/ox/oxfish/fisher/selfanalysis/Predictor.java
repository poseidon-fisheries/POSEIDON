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

package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * An object that is supposed to track a value in order to predict it
 * Created by carrknight on 8/18/15.
 */
public interface Predictor extends FisherStartable{

    /**
     * ask the predictor the expected value of the variable it is tracking
     * @return the expected value
     */
    double predict();

    /**
     * ask the predictor what is the probability the variable it is tracking is below a given level
     * @param level the level
     * @return P(x < level)
     */
    double probabilityBelowThis(double level);


    /**
     * Asks the predictor what is the probability that a sum of #elementsInSum of identically distributed elements of
     * this predictor is below the given level
     * @param level the level the sum has to be below of
     * @param elementsInSum the number of i.i.d independent variables given by the predictor summed together
     * @return a probability value
     */
    double probabilitySumBelowThis(double level, int elementsInSum);


    /**
     * this is called if something happens (gear change for example) that makes us think the old predictors are full of garbage
     * data and need to be reset
     */
    void reset();

}
