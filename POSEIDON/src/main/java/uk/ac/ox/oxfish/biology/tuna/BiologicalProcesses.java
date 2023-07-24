package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Collection;
import java.util.List;

public class BiologicalProcesses {

    private final BiologyInitializer biologyInitializer;
    private final GlobalBiology globalBiology;
    private final List<AlgorithmFactory<? extends Startable>> startableFactories;

    public BiologicalProcesses(
        final BiologyInitializer biologyInitializer,
        final GlobalBiology globalBiology,
        final Collection<AlgorithmFactory<? extends Startable>> startableFactories
    ) {
        this.biologyInitializer = biologyInitializer;
        this.globalBiology = globalBiology;
        this.startableFactories = ImmutableList.copyOf(startableFactories);
    }

    public BiologyInitializer getBiologyInitializer() {
        return biologyInitializer;
    }

    public GlobalBiology getGlobalBiology() {
        return globalBiology;
    }

    public List<AlgorithmFactory<? extends Startable>> getStartableFactories() {
        return startableFactories;
    }
}
