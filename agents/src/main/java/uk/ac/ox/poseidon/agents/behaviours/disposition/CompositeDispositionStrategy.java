package uk.ac.ox.poseidon.agents.behaviours.disposition;

import uk.ac.ox.poseidon.biology.Content;

import java.util.Collection;

public class CompositeDispositionStrategy<C extends Content<C>> implements DispositionStrategy<C> {

    private final DispositionStrategy<C>[] dispositionStrategies;

    @SuppressWarnings("unchecked")
    public CompositeDispositionStrategy(final Collection<DispositionStrategy<? super C>> dispositionStrategies) {
        this.dispositionStrategies = dispositionStrategies.toArray(DispositionStrategy[]::new);
    }

    @Override
    public Disposition<C> partition(
        final Disposition<C> currentDisposition,
        final double availableCapacityInKg
    ) {
        Disposition<C> disposition = currentDisposition;
        for (final DispositionStrategy<C> dispositionStrategy : dispositionStrategies) {
            disposition = dispositionStrategy.partition(disposition, availableCapacityInKg);
        }
        return disposition;
    }
}
