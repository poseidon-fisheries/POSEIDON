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

import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Deque;
import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p>  Here I am using the algorithm from: http://stackoverflow.com/a/14638138/975904
 * <p>  Basically a "fast" way to keep the variance measured
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-06
 * @see
 */
public class MovingVariance<T extends Number> {

    private final Deque<T> observations = new LinkedList<>();
    private final int size;
    private double average = Double.NaN;
    private double variance = Double.NaN;


    public MovingVariance(int size) {
        this.size = size;
    }

    /**
     * adds a new observation to the filter!
     *
     * @param newObservation a new observation!
     */
    public void addObservation(T newObservation) {

        assert observations.size() < size || (observations.size() == size && Double.isFinite(variance)) :
            variance + "----" + newObservation + " ---- " + (observations.size() < size);

        observations.addLast(newObservation);
        if (observations.size() == size && Double.isNaN(average)) {
            average = computeBatchAverage();
            variance = computeInitialVarianceThroughCompensatedSummation(average);
        } else if (observations.size() > size) {
            //need to correct!
            double oldestValue = observations.pop().doubleValue();
            final double newValue = newObservation.doubleValue();
            double oldAverage = average;
            average = average + (newValue - oldestValue) / size;
            variance = variance + (newValue - average + oldestValue - oldAverage) * (newValue - oldestValue) / (size);
            //might have to add a Max(0,variance) if there are numerical issues!
            assert Double.isFinite(variance) : average;
        }
    }

    private double computeBatchAverage() {
        double sum = 0;
        for (T n : observations)
            sum += n.doubleValue();

        return sum / observations.size();
    }

    //from the wikipedia.
    private double computeInitialVarianceThroughCompensatedSummation(final double currentAverage) {
        assert observations.size() == size ^ Double.isNaN(average);
        assert Double.isFinite(currentAverage);

        double squaredSum = 0;
        double compensatingSum = 0;
        for (T observation : observations) {
            squaredSum += Math.pow(observation.doubleValue() - currentAverage, 2);
            compensatingSum += observation.doubleValue() - currentAverage;
        }

        return (double) ((squaredSum - Math.pow(compensatingSum, 2) / observations.size()) / observations.size());

    }

    /**
     * the variance. If variance is below .0001 it returns 0.
     *
     * @return the smoothed observation
     */
    public double getSmoothedObservation() {


        double currentVariance = variance;

        //use preliminary variance
        if (observations.size() >= 2 && Double.isNaN(variance))
            variance = computeInitialVarianceThroughCompensatedSummation(computeBatchAverage());

        //if variance is very small,
        if (variance < FishStateUtilities.EPSILON)
            return 0;
        return variance;
    }

    /**
     * If it can predict (that is, if I call getSmoothedObservation i don't get a NaN)
     */
    public boolean isReady() {
        return Double.isFinite(variance);
    }

    public double getAverage() {

        if (Double.isFinite(average))
            return average;
        else
            return computeBatchAverage();
    }


    public int getSize() {
        return size;
    }
}