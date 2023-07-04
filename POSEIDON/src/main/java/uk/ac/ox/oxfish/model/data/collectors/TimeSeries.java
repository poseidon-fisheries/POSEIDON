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

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static uk.ac.ox.oxfish.utility.Measures.DOLLAR;
import static uk.ac.ox.oxfish.utility.Measures.Money;

/**
 * Basically a map<String,Double> to collect data about an object of type T
 * Created by carrknight on 5/2/15.
 */
public class TimeSeries<T> implements Steppable {
    private static final long serialVersionUID = -3947845104831065973L;
    final private LinkedHashMap<String, ColumnGatherer<T>> data;


    /**
     * if this is true, gather data every year. Otherwise gather data step every step
     */
    private final IntervalPolicy policy;

    private final StepOrder stepOrder;
    // The currency to use to set the unit of money related columns.
    Unit<Money> currency = DOLLAR;
    private T observed;
    private Stoppable receipt = null;

    public TimeSeries(final IntervalPolicy policy) {
        this(
            policy,
            policy.equals(IntervalPolicy.EVERY_YEAR) ? StepOrder.YEARLY_DATA_GATHERING : StepOrder.DAILY_DATA_GATHERING
        );
    }


    public TimeSeries(final IntervalPolicy policy, final StepOrder stepOrder) {
        this.policy = policy;
        this.stepOrder = stepOrder;
        data = new LinkedHashMap<>();
    }

    /**
     * Create a new data column
     *
     * @param title        the title of the column
     * @param gatherer     the function to call in order to fill the rows when the datagatherer is stepped
     * @param defaultValue the value to fill the rows with if this gatherer is added after other columns already have
     *                     some rows filled
     */
    public DataColumn registerGatherer(final String title, final Gatherer<T> gatherer, final double defaultValue) {
        return registerGatherer(title, gatherer, defaultValue, null, "");
    }

    /**
     * Create a new data column
     *
     * @param title        the title of the column
     * @param gatherer     the function to call in order to fill the rows when the datagatherer is stepped
     * @param defaultValue the value to fill the rows with if this gatherer is added after other columns already have
     *                     some rows filled
     * @param unit         the unit of measure that the data is stored in. If this is not null, the new DataColumn will be
     *                     an instance of MeasuredDataColumn and store the unit so it can be used for display or conversion.
     */
    public <Q extends Quantity<Q>> DataColumn registerGatherer(
        final String title,
        final Gatherer<T> gatherer,
        final double defaultValue,
        final Unit<Q> unit,
        final String yLabel
    ) {
        Preconditions.checkArgument(!data.containsKey(title), "Column already exists: " + title);
        final int size = noGatherers() ? 0 : numberOfObservations();
        final DataColumn column = new DataColumn(title, unit, yLabel);
        //fill if needed
        for (int i = 0; i < size; i++)
            column.add(defaultValue);
        data.put(title, new ColumnGatherer<>(column, gatherer));

        assert consistencyCheck();
        return column;
    }

    public boolean noGatherers() {
        return data.isEmpty();
    }

    public int numberOfObservations() {
        return data.values().iterator().next().getColumn().size();
    }

    private boolean consistencyCheck() {
        //all elements have the same size
        if (isEmpty())
            return true;
        else {


            final int size = numberOfObservations();
            return data.values().stream().allMatch(column -> column.getColumn().size() == size);
        }
    }

    public boolean isEmpty() {
        return noGatherers() || numberOfObservations() == 0;
    }

    public DataColumn removeGatherer(final String title) {
        final DataColumn removed = data.remove(title).getColumn();
        assert removed != null;

        return removed;


    }

    /**
     * call this to start the observation
     *
     * @param state    model
     * @param observed the object to observe
     */
    public void start(final FishState state, final T observed) {
        assert this.observed == null;
        this.observed = observed;

        receipt = state.schedulePerPolicy(this, stepOrder, policy);


    }

    public void turnOff() {
        if (receipt != null) {
            receipt.stop();
        }
        reset();

    }

    protected void reset() {
        for (final ColumnGatherer<T> value : data.values()) {
            value.getColumn().clear();
        }
        data.clear();
    }

    @Override
    public void step(final SimState simState) {

        for (final Map.Entry<String, ColumnGatherer<T>> columns : data.entrySet()) {
            final Gatherer<T> gatherer = columns.getValue().getGatherer();
            columns.getValue().getColumn().add(gatherer.apply(observed));
        }
        assert consistencyCheck();

    }

    public int getNumberOfColumns() {
        return data.size();
    }

    public Double getLatestObservation(final String columnName) {
        return data.get(columnName).getColumn().getLatest();
    }

    /**
     * get a specific column
     */
    public DataColumn getColumn(final String name) {
        final ColumnGatherer<T> current = data.get(name);
        if (current == null) {
            return null;
        }
        return current.getColumn();
    }

    public Collection<DataColumn> getColumns() {

        final ArrayList<DataColumn> columns = new ArrayList<>(data.size());
        for (final ColumnGatherer<T> gatherer : data.values()) {
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

    public Unit<Money> getCurrency() {
        return currency;
    }

    public void setCurrency(final Unit<Money> currency) {
        this.currency = currency;
    }

}


/**
 * a pairing of gatherer and column it fills. Makes it faster to loop at step time
 */
final class ColumnGatherer<T> {


    private final DataColumn column;

    private final Gatherer<T> gatherer;

    public ColumnGatherer(final DataColumn column, final Gatherer<T> gatherer) {
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
