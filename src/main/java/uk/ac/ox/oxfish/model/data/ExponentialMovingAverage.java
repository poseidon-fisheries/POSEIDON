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

package uk.ac.ox.oxfish.model.data;

import com.google.common.base.Preconditions;

/**
 * EMA: each observation makes average = (1-alpha)average+(alpha)*new_observation
 * Created by carrknight on 11/9/16.
 */
public class ExponentialMovingAverage<T extends Number> implements Averager<T> {


    private double average = Double.NaN;

    private final double alpha;

    public ExponentialMovingAverage(double alpha) {
        this.alpha = alpha;
        Preconditions.checkArgument(alpha>=0);
        Preconditions.checkArgument(alpha<=1);
    }


    @Override
    public void addObservation(T observation) {
        if(Double.isFinite(average))
            average = (1-alpha)*average + alpha*observation.doubleValue();
        else
            average=observation.doubleValue();
    }

    /**
     * returns the average
     */
    @Override
    public double getSmoothedObservation() {
        return average;
    }
}
