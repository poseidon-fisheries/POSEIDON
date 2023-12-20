package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observers;
import uk.ac.ox.oxfish.model.data.monitors.observers.PurseSeinerActionObserver;

import java.util.Map;
import java.util.Set;

/**
 * A decorator for {@link MutableLocationValues} objects, removing all values below average when stepped if the total
 * number of values is above the stated maximum. Currently only used by the "gravity" algorithms in the EPO scenarios.
 *
 * @param <A> the class of objects observed for updating the location values, most likely some kind of action
 */
public class CappedLocationValuesDecorator<A>
    extends PurseSeinerActionObserver<A>
    implements LocationValues, Steppable {
    private static final long serialVersionUID = -5414686841889286464L;
    private final MutableLocationValues<? super A> delegate;
    private final int maximumNumberOfValues;

    @SuppressWarnings("BoundedWildcard")
    CappedLocationValuesDecorator(
        final MutableLocationValues<A> delegate,
        final int maximumNumberOfValues
    ) {
        super(delegate.getObservedClass());
        this.delegate = delegate;
        this.maximumNumberOfValues = maximumNumberOfValues;
    }

    @Override
    public double getValueAt(final Int2D location) {
        return delegate.getValueAt(location);
    }

    @Override
    public Set<Map.Entry<Int2D, Double>> getValues() {
        return delegate.getValues();
    }

    @Override
    public Observers getObservers() {
        return delegate.getObservers();
    }

    @Override
    public void step(final SimState simState) {
        if (delegate.getValues().size() > maximumNumberOfValues) {
            // when reaching the limit, forget all the values that are below average
            delegate.getValues().stream().mapToDouble(Map.Entry::getValue).average().ifPresent(average ->
                delegate.removeIf(entry -> entry.getValue() < average)
            );
        }
        delegate.step(simState);
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
        delegate.start(model, fisher);
    }

    @Override
    public void observe(final A observable) {
        delegate.observe(observable);
    }
}
