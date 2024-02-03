package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

public abstract class BiologicalProcessesFactory<B extends LocalBiology>
    implements AlgorithmFactory<BiologicalProcesses> {
    private InputPath inputFolder;
    private BiologyInitializerFactory<B> biologyInitializer;
    private RestorerFactory<B> restorer;
    private ScheduledBiologicalProcessesFactory<B> scheduledProcesses;

    public BiologicalProcessesFactory(
        final InputPath inputFolder,
        final BiologyInitializerFactory<B> biologyInitializer,
        final RestorerFactory<B> restorer,
        final ScheduledBiologicalProcessesFactory<B> scheduledProcesses
    ) {
        this.inputFolder = inputFolder;
        this.biologyInitializer = biologyInitializer;
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

    @Override
    public BiologicalProcesses apply(final FishState fishState) {
        final BiologyInitializer biologyInitializer = getBiologyInitializer().apply(fishState);
        return new BiologicalProcesses(
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
}
