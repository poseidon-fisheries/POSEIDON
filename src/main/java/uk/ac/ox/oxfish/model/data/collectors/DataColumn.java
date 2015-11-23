package uk.ac.ox.oxfish.model.data.collectors;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

    public Double getDatumXDaysAgo(int daysAgo)
    {
        return data.get(data.size()-daysAgo-1);
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


    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     *
     * <p>This method should be overridden when the {@link #spliterator()}
     * method cannot return a spliterator that is {@code IMMUTABLE},
     * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
     * for details.)
     *
     * @implSpec
     * The default implementation creates a sequential {@code Stream} from the
     * collection's {@code Spliterator}.
     *
     * @return a sequential {@code Stream} over the elements in this collection
     * @since 1.8
     */
    public Stream<Double> stream() {
        return data.stream();
    }


    public LinkedList<Double> copy(){
        return new LinkedList<>(data);
    }
}
