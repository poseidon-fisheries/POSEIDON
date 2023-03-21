package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public abstract class ScheduledBiologicalProcessesFactory<K, B extends LocalBiology>
    implements AlgorithmFactory<ScheduledBiologicalProcesses<B>> {
    private Reallocator<K, B> reallocator;

    public Reallocator<K, B> getReallocator() {
        return reallocator;
    }

    public void setReallocator(final Reallocator<K, B> reallocator) {
        this.reallocator = reallocator;
    }
}
