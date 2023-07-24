package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public abstract class ScheduledBiologicalProcessesFactory<B extends LocalBiology>
    implements AlgorithmFactory<ScheduledBiologicalProcesses<B>> {
    private AlgorithmFactory<Reallocator<B>> reallocator;

    public ScheduledBiologicalProcessesFactory() {
    }

    public ScheduledBiologicalProcessesFactory(final AlgorithmFactory<Reallocator<B>> reallocator) {
        this.reallocator = reallocator;
    }

    public AlgorithmFactory<Reallocator<B>> getReallocator() {
        return reallocator;
    }

    public void setReallocator(final AlgorithmFactory<Reallocator<B>> reallocator) {
        this.reallocator = reallocator;
    }
}
