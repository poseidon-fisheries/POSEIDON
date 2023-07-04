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

import javax.measure.Unit;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static tech.units.indriya.AbstractUnit.ONE;

/**
 * basically a linked-list for double values that cannot be modified easily
 * Created by carrknight on 6/9/15.
 */
public class DataColumn implements Iterable<Double>, Serializable {

    private static final long serialVersionUID = 6931226317085531711L;
    private final LinkedList<Double> data = new LinkedList<>();

    private final String name;
    private final Unit<?> unit;
    private final String yLabel;

    public DataColumn(final String name) {
        this(name, null, null);
    }

    public DataColumn(final String name, final Unit<?> unit, final String yLabel) {
        this.name = name;
        this.unit = unit == null ? ONE : unit;
        this.yLabel = yLabel == null ? "" : yLabel;
    }

    /**
     * name of the column
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * <p>The unit in which the data is this column is specified.
     * Currently purely for exporting the symbol to help in
     * labelling externally produced plots.
     *
     * <p>Dimensionless quantities should have a unit of
     * {@code tech.units.indriya.AbstractUnit#ONE}.
     */
    public Unit<?> getUnit() {
        return unit;
    }

    /**
     * The suggested y-label for externally produced plots.
     */
    public String getYLabel() {
        return yLabel;
    }

    /**
     * add latest observation
     *
     * @param newValue latest observation to add
     */
    public void add(final Double newValue) {
        data.addLast(newValue);
    }

    /**
     * the latest value added or NaN if there is none
     *
     * @return the latest value added or NaN if there is none
     */
    public Double getLatest() {
        return data.isEmpty() ? Double.NaN : data.peekLast();
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public Double get(final int index) {
        return data.get(index);
    }

    public Double getDatumXStepsAgo(final int daysAgo) {
        return data.get(data.size() - daysAgo - 1);
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return data.size();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Double> iterator() {
        return data.iterator();
    }


    /**
     * @since 1.6
     */
    public Iterator<Double> descendingIterator() {
        return data.descendingIterator();
    }

    /**
     *
     */
    @Override
    public void forEach(final Consumer<? super Double> action) {
        data.forEach(action);
    }

    /**
     *
     */
    @Override
    public Spliterator<Double> spliterator() {
        return data.spliterator();
    }


    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     *
     * <p>This method should be overridden when the {@link #spliterator()}
     * method cannot return a spliterator that is {@code IMMUTABLE},
     * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
     * for details.)
     *
     * @return a sequential {@code Stream} over the elements in this collection
     * @implSpec The default implementation creates a sequential {@code Stream} from the
     * collection's {@code Spliterator}.
     * @since 1.8
     */
    public Stream<Double> stream() {
        return data.stream();
    }


    public LinkedList<Double> copy() {
        return new LinkedList<>(data);
    }


    @Override
    public String toString() {
        final String sb = "DataColumn{" + "data=" + data +
            '}';
        return sb;
    }

    public void clear() {
        data.clear();
    }
}
