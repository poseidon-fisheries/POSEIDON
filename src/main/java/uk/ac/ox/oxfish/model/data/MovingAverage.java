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

/**
 An object to compute the moving average of whatever is put in.
 * <p/> It accepts any number but the computations are all done through double value call
 * * Created by carrknight on 8/14/15.
 */
public class MovingAverage<T extends Number> implements Averager<T>{

    final private MovingSum<T> sum;

    /**
     * the constructor that creates the moving average object
     */
    public MovingAverage(int movingAverageSize) {
        sum = new MovingSum<T>(movingAverageSize);
    }



    public double getSmoothedObservation()
    {
        if(!isReady())
        {
            assert sum.numberOfObservations() == 0;
            return Double.NaN;
        }
        assert sum.numberOfObservations()>0;
        assert sum.numberOfObservations() <= sum.getSize();
        return sum.getSmoothedObservation()/(sum.numberOfObservations());


    }



    public String toString() {
        return String.valueOf(getSmoothedObservation());
    }

    /**
     * Add a new observation to the moving average
     */
    public void addObservation(T observation) {
        sum.addObservation(observation);
    }

    public int getSize() {
        return sum.getSize();
    }

    /**
     * If it can predict (that is, if I call getSmoothedObservation i don't get a NaN)
     *
     */
    public boolean isReady() {
        return sum.isReady();
    }

    public int numberOfObservations() {
        return sum.numberOfObservations();
    }
}