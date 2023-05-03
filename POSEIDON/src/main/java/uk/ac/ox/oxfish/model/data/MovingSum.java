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
 *  Sum up the last x observations fed in
 * Created by carrknight on 8/14/15.
 */



import java.util.LinkedList;


public class MovingSum<T extends Number>
{

    /**
     * Where we keep all the observations
     */
    LinkedList<T> lastElements = new LinkedList<>();

    /**
     * The size of the queue
     */
    final private int size;

    public MovingSum(int size) {
        this.size = size;
    }

    /**
     * Add a new observation to the moving average
     * @param observation number to add
     */
    public void addObservation(T observation){

        //add the last observation
        lastElements.addLast(observation);
        //if the queue is full, remove the first guy
        if(lastElements.size()>size)
            lastElements.removeFirst();

        assert lastElements.size() <=size;


    }


    /**
     * the sum computed so far
     *
     * @return the smoothed observation
     */
    public double getSmoothedObservation()
    {
        if(!isReady())
            return Float.NaN;

        float total  =0;
        for(T element : lastElements)
            total += element.doubleValue();

        return total;
    }

    /**
     * If it can predict (that is, if I call getSmoothedObservation i don't get a NaN)
     *
     */
    public boolean isReady() {
        return !lastElements.isEmpty();
    }



    public int getSize() {
        return size;
    }

    public int numberOfObservations(){
        return lastElements.size();
    }
}
