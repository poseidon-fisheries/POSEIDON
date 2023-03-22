package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public abstract class ScheduledBiologicalProcessesFactory<B extends LocalBiology>
    implements AlgorithmFactory<ScheduledBiologicalProcesses<B>> {
    private Reallocator<B> reallocator;

    public Reallocator<B> getReallocator() {
        return reallocator;
    }

    public void setReallocator(final Reallocator<B> reallocator) {
        this.reallocator = reallocator;
    }
}
