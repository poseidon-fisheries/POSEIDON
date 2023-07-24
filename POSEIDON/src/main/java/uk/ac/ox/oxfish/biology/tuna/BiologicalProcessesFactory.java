package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.List;

public abstract class BiologicalProcessesFactory<B extends LocalBiology> {
    private InputPath inputFolder;
    private BiologyInitializerFactory<B> biologyInitializer;
    private ReallocatorFactory<B, Reallocator<B>> reallocator;
    private RestorerFactory<B> restorer;
    private ScheduledBiologicalProcessesFactory<B> scheduledProcesses;

    public BiologicalProcessesFactory(
        final InputPath inputFolder,
        final BiologyInitializerFactory<B> biologyInitializer,
        final ReallocatorFactory<B, Reallocator<B>> reallocator,
        final RestorerFactory<B> restorer,
        final ScheduledBiologicalProcessesFactory<B> scheduledProcesses
    ) {
        this.inputFolder = inputFolder;
        this.biologyInitializer = biologyInitializer;
        this.reallocator = reallocator;
        this.restorer = restorer;
        this.scheduledProcesses = scheduledProcesses;
    }

    public BiologicalProcessesFactory() {

    }

    public ScheduledBiologicalProcessesFactory<B> getScheduledProcesses() {
        return scheduledProcesses;
    }

    public void setScheduledProcesses(final ScheduledBiologicalProcessesFactory<B> scheduledProcesses) {
        this.scheduledProcesses = scheduledProcesses;
    }

    public RestorerFactory<B> getRestorer() {
        return restorer;
    }

    public void setRestorer(final RestorerFactory<B> restorer) {
        this.restorer = restorer;
    }

    public InputPath getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(final InputPath inputFolder) {
        this.inputFolder = inputFolder;
    }

    public Processes initProcesses(final NauticalMap nauticalMap, final FishState fishState) {

        final Reallocator<B> reallocator = this.reallocator.apply(fishState);
        scheduledProcesses.setReallocator(reallocator);
        restorer.setReallocator(reallocator);
        biologyInitializer.setReallocator(reallocator);

        final BiologyInitializer biologyInitializer = getBiologyInitializer().apply(fishState);
        return new Processes(
            biologyInitializer,
            biologyInitializer.generateGlobal(fishState.getRandom(), fishState),
            ImmutableList.of(
                scheduledProcesses,
                restorer
            )
        );
    }

    public BiologyInitializerFactory<B> getBiologyInitializer() {
        return biologyInitializer;
    }

    public void setBiologyInitializer(final BiologyInitializerFactory<B> biologyInitializer) {
        this.biologyInitializer = biologyInitializer;
    }

    public ReallocatorFactory<B, Reallocator<B>> getReallocator() {
        return reallocator;
    }

    public void setReallocator(final ReallocatorFactory<B, Reallocator<B>> reallocator) {
        this.reallocator = reallocator;
    }

    public static class Processes {
        public final BiologyInitializer biologyInitializer;
        public final GlobalBiology globalBiology;
        public final List<AlgorithmFactory<? extends Startable>> startableFactories;

        public Processes(
            final BiologyInitializer biologyInitializer,
            final GlobalBiology globalBiology,
            final List<AlgorithmFactory<? extends Startable>> startableFactories
        ) {
            this.biologyInitializer = biologyInitializer;
            this.globalBiology = globalBiology;
            this.startableFactories = startableFactories;
        }
    }
}
