package uk.ac.ox.poseidon.agents.behaviours.disposition;

import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Content;

public interface DispositionProcess<C extends Content<C>> {

    Disposition<C> partition(
        Disposition<C> currentDisposition,
        double availableCapacityInKg
    );

    default Disposition<C> partition(
        final Bucket<C> grossCatch,
        final double availableCapacityInKg
    ) {
        return partition(
            new Disposition<>(grossCatch, Bucket.empty(), Bucket.empty()),
            availableCapacityInKg
        );
    }

}
