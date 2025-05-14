package uk.ac.ox.poseidon.agents.behaviours.disposition;

import uk.ac.ox.poseidon.biology.Content;

import java.util.Collection;

public class CompositeDispositionProcess<C extends Content<C>> implements DispositionProcess<C> {

    private final DispositionProcess<C>[] dispositionStrategies;

    @SuppressWarnings("unchecked")
    public CompositeDispositionProcess(final Collection<DispositionProcess<? super C>> dispositionStrategies) {
        this.dispositionStrategies = dispositionStrategies.toArray(DispositionProcess[]::new);
    }

    @Override
    public Disposition<C> partition(
        final Disposition<C> currentDisposition,
        final double availableCapacityInKg
    ) {
        Disposition<C> disposition = currentDisposition;
        for (final DispositionProcess<C> dispositionProcess : dispositionStrategies) {
            disposition = dispositionProcess.partition(disposition, availableCapacityInKg);
        }
        return disposition;
    }
}
