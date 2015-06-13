package uk.ac.ox.oxfish.model.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * basically a linked-list for double values that cannot be modified easily
 * Created by carrknight on 6/9/15.
 */
public class DataColumn implements Iterable<Double>{

    private final LinkedList<Double> data = new LinkedList<>();

    private final String name;

    public DataColumn(String name) {
        this.name = name;
    }

    /**
     * name of the column
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * add latest observation
     * @param newValue latest observation to add
     */
    public void add(Double newValue)
    {
        data.addLast(newValue);
    }

    /**
     * the latest value added or NaN if there is none
     * @return the latest value added or NaN if there is none
     */
    public Double getLatest()
    {
        return data.isEmpty() ? Double.NaN : data.peekLast();
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public Double get(int index) {
        return data.get(index);
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
     */
    @Override
    public void forEach(Consumer<? super Double> action) {
        data.forEach(action);
    }

    /**
     */
    @Override
    public Spliterator<Double> spliterator() {
        return data.spliterator();
    }
}
