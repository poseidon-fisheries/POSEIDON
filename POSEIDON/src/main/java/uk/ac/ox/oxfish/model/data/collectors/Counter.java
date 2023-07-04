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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A simple map String--->Double that auto-resets once started
 * Created by carrknight on 6/8/15.
 */
public class Counter implements Startable, Steppable {

    private static final long serialVersionUID = 6991537107843888670L;
    final private Map<String, Double> data;

    final private IntervalPolicy policy;

    private Stoppable receipt = null;

    //some counters spend an inordinate amount of time
    //updating the same column over and over again
    //we can speed up the processing a lot by not inserting
    //the last column (and just track its counts separately)
    //until we absolutely need it
    private String lazyColumnToInsert = null;
    private double lazyValueToAdd = 0;

    public Counter(final IntervalPolicy policy) {
        this.data = new LinkedHashMap<>();
        this.policy = policy;
    }

    public void start(final FishState state) {
        Preconditions.checkState(receipt == null, "Already Started!");
        receipt = state.schedulePerPolicy(this, StepOrder.DATA_RESET, policy);
    }

    @Override
    public void step(final SimState simState) {
        /**
         * reset all stuff
         */
        data.entrySet().forEach(datum -> datum.setValue(0d));
    }

    /**
     * adds a new data column, ready to be counted. It can't be already there
     *
     * @param columnName the name of the column
     */
    public void addColumn(final String columnName) {
        Preconditions.checkArgument(!hasColumn(columnName), columnName + " column already exists!");
        data.put(columnName, 0d);
    }

    /**
     * Does this column already exist?
     *
     * @param columnName
     * @return
     */
    public boolean hasColumn(final String columnName) {
        return data.containsKey(columnName);
    }

    /**
     * increment column by this
     *
     * @param columnName the column to increment
     * @param add        by how much to increment
     */
    public void count(final String columnName, final double add) {

        if (add == 0)
            return;

        if (lazyColumnToInsert != null && lazyColumnToInsert != columnName)
            flushLazyValueInCounter();

        lazyColumnToInsert = columnName;
        lazyValueToAdd = lazyValueToAdd + add;


    }

    private void flushLazyValueInCounter() {
        assert lazyColumnToInsert != null;
        assert Double.isFinite(lazyValueToAdd);
        data.compute(
            lazyColumnToInsert,
            (s, oldValue) -> {
                if (oldValue == null)
                    throw new NullPointerException("No column exists");
                else
                    return oldValue + lazyValueToAdd;

            }
        );
        lazyColumnToInsert = null;
        lazyValueToAdd = 0;

    }

    /**
     * turnOff resetting
     */
    @Override
    public void turnOff() {
        if (receipt != null)
            receipt.stop();
    }


    public Double getColumn(final String columnName) {
        if (lazyColumnToInsert != null)
            flushLazyValueInCounter();
        return data.get(columnName);
    }


    public Set<String> getValidCounters() {
        return data.keySet();
    }

}
