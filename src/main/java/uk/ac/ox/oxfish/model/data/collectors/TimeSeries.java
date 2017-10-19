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
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basically a map<String,Double> to collect data about an object of type T
 * Created by carrknight on 5/2/15.
 */
public class TimeSeries<T> implements Steppable
{
    final private LinkedHashMap<String,DataColumn> data;

    final private Map<String,DataColumn> dataView;

    /**
     * the functions to lspiRun on studied object to gather their data
     */
    final private LinkedHashMap<String,Gatherer<T>> gatherers;

    /**
     * if this is true, gather data every year. Otherwise gather data step every step
     */
    private final IntervalPolicy policy;

    private final StepOrder stepOrder;

    private T observed;

    /**
     * Create a new data column
     * @param title the title of the column
     * @param gatherer the function to call in order to fill the rows when the datagatherer is stepped
     * @param defaultValue the value to fill the rows with if this gatherer is added after other columns already have
     *                     some rows filled
     */
    public DataColumn registerGatherer(String title, Gatherer<T> gatherer, double defaultValue)
    {
        Preconditions.checkArgument(!data.containsKey(title), "Column already exists");
        int size =noGatherers() ? 0 : numberOfObservations();
        DataColumn column = new DataColumn(title);
        data.put(title, column);
        //fill if needed
        for(int i=0; i<size; i++)
            column.add(defaultValue);
        gatherers.put(title,  gatherer);

        assert consistencyCheck();
        return column;
    }


    public DataColumn removeGatherer(String title)
    {
        DataColumn removed = data.remove(title);
        assert removed!=null;
        gatherers.remove(title);

        return removed;



    }


    private Stoppable receipt = null;
    /**
     * call this to start the observation
     * @param state model
     * @param observed the object to observe
     */
    public void start(FishState state, T observed)
    {
        assert  this.observed == null;
        this.observed = observed;

        receipt = state.schedulePerPolicy(this,stepOrder,policy);


    }

    public void turnOff()
    {
        receipt.stop();
    }

    @Override
    public void step(SimState simState) {

        for(Map.Entry<String,DataColumn> columns : data.entrySet())
        {
            columns.getValue().add(gatherers.get(columns.getKey()).apply(observed));
        }
        assert consistencyCheck();

    }


    public TimeSeries(IntervalPolicy policy, StepOrder stepOrder) {
        this.policy = policy;
        this.stepOrder = stepOrder;
        data = new LinkedHashMap<>();
        dataView = Collections.unmodifiableMap(data);
        gatherers = new LinkedHashMap<>();
    }

    public TimeSeries(IntervalPolicy policy) {
       this(policy, policy.equals(IntervalPolicy.EVERY_YEAR) ? StepOrder.YEARLY_DATA_GATHERING : StepOrder.DAILY_DATA_GATHERING);
    }

    public boolean isEmpty(){
        return noGatherers() || numberOfObservations() == 0;
    }

    public boolean noGatherers() {
        return data.isEmpty();
    }

    public int numberOfObservations() {
        return data.values().iterator().next().size();
    }

    private boolean consistencyCheck()
    {
        //all elements have the same size
        if(isEmpty())
            return true;
        else
        {
            if(data.size() != gatherers.size())
                return false;

            int size = numberOfObservations();
            return data.values().stream().allMatch(column -> column.size()==size);
        }
    }

    /**
     * get an unmodifiable map showing the data
     */
    public Map<String, DataColumn> getDataView() {
        return dataView;
    }


    public Double getLatestObservation(String columnName)
    {
        return data.get(columnName).getLatest();
    }
    /**
     * get a specific column
     */
    public DataColumn getColumn(String name){
        return dataView.get(name);
    }

   public Collection<DataColumn> getColumns(){
       return dataView.values();
   }

    public IntervalPolicy getPolicy() {
        return policy;
    }

    protected T getObserved() {
        return observed;
    }
}
