/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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
 * a simple variant of the iterative average that can move back and forth (as in, you can "remove" observations),
 * maybe not as efficient
 * @param <T>
 */
public class IterativeAgerageBackAndForth<T extends Number> implements Averager<T> {


    private double totalSum = 0;

    private double average = 0;

    private double observations = 0;

    @Override
    public void addObservation(T observation) {
        addObservationfromDouble(observation.doubleValue());
    }


    public void addObservationfromDouble(double observation) {
        observations++;
        totalSum+=observation;
        average = totalSum/observations;
    }

    /**
     * returns the average
     */
    @Override
    public double getSmoothedObservation() {
        return average;
    }

    public void removeObservation(double observation) {
        Preconditions.checkArgument(observations>1);
        observations--;
        totalSum-=observation;
        average = totalSum/observations;
    }


}
