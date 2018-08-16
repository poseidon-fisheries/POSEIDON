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

import java.util.*;

/**
 * Basically a map<String,Double> to collect data about an object of type T
 * Created by carrknight on 5/2/15.
 */
public class TimeSeries<T> implements Steppable
{
    final private LinkedHashMap<String,ColumnGatherer<T>> data;


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
        //fill if needed
        for(int i=0; i<size; i++)
            column.add(defaultValue);
        data.put(title, new ColumnGatherer<>(column,gatherer));

        assert consistencyCheck();
        return column;
    }


    public DataColumn removeGatherer(String title)
    {
        DataColumn removed = data.remove(title).getColumn();
        assert removed!=null;

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
        if(receipt!=null) {
            receipt.stop();
        }
        reset();

    }

    @Override
    public void step(SimState simState) {

        for(Map.Entry<String,ColumnGatherer<T>> columns : data.entrySet())
        {
            Gatherer<T> gatherer = columns.getValue().getGatherer();
            columns.getValue().getColumn().add(gatherer.apply(observed));
        }
        assert consistencyCheck();

    }


    public TimeSeries(IntervalPolicy policy, StepOrder stepOrder) {
        this.policy = policy;
        this.stepOrder = stepOrder;
        data = new LinkedHashMap<>();
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

    public int getNumberOfColumns(){
        return data.size();
    }

    public int numberOfObservations() {
        return data.values().iterator().next().getColumn().size();
    }

    private boolean consistencyCheck()
    {
        //all elements have the same size
        if(isEmpty())
            return true;
        else
        {


            int size = numberOfObservations();
            return data.values().stream().allMatch(column -> column.getColumn().size()==size);
        }
    }



    public Double getLatestObservation(String columnName)
    {
        return data.get(columnName).getColumn().getLatest();
    }
    /**
     * get a specific column
     */
    public DataColumn getColumn(String name){
        ColumnGatherer<T> current = data.get(name);
        if(current==null)
            return null;
        return current.getColumn();
    }

    public Collection<DataColumn> getColumns(){

        ArrayList<DataColumn> columns = new ArrayList<>(data.size());
        for (ColumnGatherer<T> gatherer : data.values()) {
            columns.add(gatherer.getColumn());
        }
        return columns;
    }

    public IntervalPolicy getPolicy() {
        return policy;
    }

    protected T getObserved() {
        return observed;
    }


    protected void reset(){
        data.clear();
    }
}


/**
 * a pairing of gatherer and column it fills. Makes it faster to loop at step time
 */
final class ColumnGatherer<T>{


    private final DataColumn column;

    private final Gatherer<T> gatherer;

    public ColumnGatherer(DataColumn column, Gatherer<T> gatherer) {
        this.column = column;
        this.gatherer = gatherer;
    }


    public DataColumn getColumn() {
        return column;
    }

    public Gatherer<T> getGatherer() {
        return gatherer;
    }
}
