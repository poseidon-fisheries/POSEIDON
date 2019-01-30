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

package uk.ac.ox.oxfish.model.data.collectors;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A simple map String--->Double that auto-resets once started
 * Created by carrknight on 6/8/15.
 */
public class Counter implements Startable, Steppable
{

    final private Map<String,Double> data;

    final private Map<String,Double> view;

    final private IntervalPolicy policy;

    private Stoppable receipt = null;

    public Counter(IntervalPolicy policy) {
        this.data = new LinkedHashMap<>();
        view = Collections.unmodifiableMap(data);
        this.policy = policy;
    }

    public void start(FishState state)
    {
        Preconditions.checkState(receipt==null, "Already Started!");
        receipt = state.schedulePerPolicy(this, StepOrder.DATA_RESET,policy);
    }

    @Override
    public void step(SimState simState) {
        /**
         * reset all stuff
         */
        data.entrySet().forEach(datum -> datum.setValue(0d));
    }

    /**
     * adds a new data column, ready to be counted. It can't be already there
     * @param columnName the name of the column
     */
    public void addColumn(String columnName)
    {
        Preconditions.checkArgument(!hasColumn(columnName), columnName + " column already exists!");
        data.put(columnName,0d);
    }


    /**
     * Does this column already exist?
     * @param columnName
     * @return
     */
    public boolean hasColumn(String columnName){
        return data.containsKey(columnName);
    }

    /**
     * increment column by this
     * @param columnName the column to increment
     * @param add by how much to increment
     */
    public void count(String columnName, double add)
    {

        data.compute(columnName,
                     (s, oldValue) -> {
                         if(oldValue==null)
                             throw new NullPointerException("No column exists");
                         else
                             return oldValue + add;

                     });
    }

    /**
     * turnOff resetting
     */
    @Override
    public void turnOff() {
        if(receipt!=null)
            receipt.stop();
    }


    public Double getColumn(String columnName){
        return data.get(columnName);
    }

    /**
     * returns an unmodifiable view of the data
     * @return unmodifiable view of the data
     */
    public Map<String,Double> getData()
    {
        return view;
    }

}
