package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.model.data.monitors.observers.Observers;

public abstract class ObservableLocationValues implements LocationValues {
    private final Observers observers = new Observers();

    @Override
    public Observers getObservers() {
        return observers;
    }
}
