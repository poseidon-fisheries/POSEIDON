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

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * Anything that predicts over a geographical map
 * Created by carrknight on 6/27/16.
 */
public interface GeographicalRegression<V> extends FisherStartable {


    /**
     * predict numerical value here
     *
     * @param tile
     * @param time
     * @param fisher
     * @param model
     * @return
     */
    public double predict(SeaTile tile, double time, Fisher fisher, FishState model);

    /**
     * learn from this observation
     *
     * @param observation
     * @param fisher
     * @param model
     */
    public void addObservation(GeographicalObservation<V> observation, Fisher fisher, FishState model);

    /**
     * turn the "V" value of the geographical observation into a number
     *
     * @param observation
     * @param fisher
     * @return
     */
    public double extractNumericalYFromObservation(GeographicalObservation<V> observation, Fisher fisher);

    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     *
     * @return an array containing all the parameters of the model
     */
    public double[] getParametersAsArray();

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    public void setParameters(double[] parameterArray);

}
