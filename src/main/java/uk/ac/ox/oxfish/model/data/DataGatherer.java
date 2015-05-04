package uk.ac.ox.oxfish.model.data;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.*;
import java.util.function.Function;

/**
 * Basically a map<String,Double> to collect data about an object of type T
 * Created by carrknight on 5/2/15.
 */
public class DataGatherer<T> implements Steppable
{
    final private Map<String,List<Double>> data;

    final private Map<String,List<Double>> dataView;

    /**
     * the functions to run on studied object to gather their data
     */
    final private Map<String,Function<T,Double>> gatherers;

    /**
     * if this is true, gather data every year. Otherwise gather data step every step
     */
    private final boolean yearly;

    private T observed;

    /**
     * Create a new data column
     * @param title the title of the column
     * @param gatherer the function to call in order to fill the rows when the datagatherer is stepped
     * @param defaultValue the value to fill the rows with if this gatherer is added after other columns already have
     *                     some rows filled
     */
    public void registerGather(String  title, Function<T,Double> gatherer, double defaultValue)
    {
        Preconditions.checkArgument(!data.containsKey(title), "Column already exists");
        int size =noGatherers() ? 0 : numberOfObservations();
        LinkedList<Double> column = new LinkedList<>();
        data.put(title, column);
        //fill if needed
        for(int i=0; i<size; i++)
            column.add(defaultValue);
        gatherers.put(title,gatherer);

        assert consistencyCheck();
    }


    /**
     * call this to start the observation
     * @param state model
     * @param observed the object to observe
     */
    public void start(FishState state, T observed)
    {
        assert  this.observed == null;
        this.observed = observed;

        if(yearly)
            state.scheduleEveryYear(this,StepOrder.DATA_GATHERING);
        else
            state.scheduleEveryStep(this,StepOrder.DATA_GATHERING);
    }

    @Override
    public void step(SimState simState) {

        for(Map.Entry<String,List<Double>> columns : data.entrySet())
        {
            columns.getValue().add(gatherers.get(columns.getKey()).apply(observed));
        }
        assert consistencyCheck();

    }



    public DataGatherer(boolean yearly) {
        this.yearly = yearly;
        data = new HashMap<>();
        dataView = Collections.unmodifiableMap(data);
        gatherers = new HashMap<>();
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

    public Map<String, List<Double>> getDataView() {
        return dataView;
    }
}
