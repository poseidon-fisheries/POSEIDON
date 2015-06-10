package uk.ac.ox.oxfish.model.data;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        this.data = new HashMap<>();
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
        data.entrySet().stream().forEach(datum -> datum.setValue(0d));
    }

    /**
     * adds a new data column, ready to be counted. It can't be already there
     * @param columnName the name of the column
     */
    public void addColumn(String columnName)
    {
        Preconditions.checkArgument(!data.containsKey(columnName));
        data.put(columnName,0d);
    }

    /**
     * increment column by this
     * @param columnName the column to increment
     * @param add by how much to increment
     */
    public void count(String columnName, double add)
    {
        data.put(columnName, data.get(columnName) + add);
    }

    /**
     * stop resetting
     */
    @Override
    public void turnOff() {
        receipt.stop();
    }


    public double getColumn(String columnName){
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
