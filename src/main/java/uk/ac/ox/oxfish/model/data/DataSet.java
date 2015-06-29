package uk.ac.ox.oxfish.model.data;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.*;
import java.util.function.Function;

/**
 * Basically a map<String,Double> to collect data about an object of type T
 * Created by carrknight on 5/2/15.
 */
public class DataSet<T> implements Steppable
{
    final private Map<String,DataColumn> data;

    final private Map<String,DataColumn> dataView;

    /**
     * the functions to run on studied object to gather their data
     */
    final private Map<String,Function<T,Double>> gatherers;

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
    public void registerGatherer(String title, Function<T, Double> gatherer, double defaultValue)
    {
        Preconditions.checkArgument(!data.containsKey(title), "Column already exists");
        int size =noGatherers() ? 0 : numberOfObservations();
        DataColumn column = new DataColumn(title);
        data.put(title, column);
        //fill if needed
        for(int i=0; i<size; i++)
            column.add(defaultValue);
        gatherers.put(title,gatherer);

        assert consistencyCheck();
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

    public void stop()
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


    public DataSet(IntervalPolicy policy, StepOrder stepOrder) {
        this.policy = policy;
        this.stepOrder = stepOrder;
        data = new LinkedHashMap<>();
        dataView = Collections.unmodifiableMap(data);
        gatherers = new HashMap<>();
    }

    public DataSet(IntervalPolicy policy) {
       this(policy,StepOrder.INDIVIDUAL_DATA_GATHERING);
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


    public double getLatestObservation(String columnName)
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
}
