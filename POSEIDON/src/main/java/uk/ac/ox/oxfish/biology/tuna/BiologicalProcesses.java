package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;

public class BiologicalProcesses<B extends LocalBiology> {

    private final BiologyInitializer biologyInitializer;
    private final Reallocator<B> reallocator;
    private final Restorer<B> restorer;
    private final ScheduledBiologicalProcesses<B> scheduledProcesses;

    public BiologicalProcesses(
        final BiologyInitializer biologyInitializer,
        final Reallocator<B> reallocator,
        final Restorer<B> restorer,
        final ScheduledBiologicalProcesses<B> scheduledProcesses
    ) {
        this.biologyInitializer = biologyInitializer;
        this.reallocator = reallocator;
        this.restorer = restorer;
        this.scheduledProcesses = scheduledProcesses;
    }
}
