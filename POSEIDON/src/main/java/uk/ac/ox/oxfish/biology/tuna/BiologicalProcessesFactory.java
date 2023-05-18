package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.List;

public abstract class BiologicalProcessesFactory<B extends LocalBiology> {
    private InputPath inputFolder;
    private SpeciesCodesFromFileFactory speciesCodesSupplier;
    private BiologyInitializerFactory<B> biologyInitializerFactory;
    private ReallocatorFactory<B, Reallocator<B>> reallocatorFactory;
    private RestorerFactory<B> restorerFactory;
    private ScheduledBiologicalProcessesFactory<B> scheduledProcessesFactory;

    public BiologicalProcessesFactory(
        final InputPath inputFolder,
        final SpeciesCodesFromFileFactory speciesCodesSupplier,
        final BiologyInitializerFactory<B> biologyInitializerFactory,
        final ReallocatorFactory<B, Reallocator<B>> reallocatorFactory,
        final RestorerFactory<B> restorerFactory,
        final ScheduledBiologicalProcessesFactory<B> scheduledProcessesFactory
    ) {
        this.inputFolder = inputFolder;
        this.speciesCodesSupplier = speciesCodesSupplier;
        this.biologyInitializerFactory = biologyInitializerFactory;
        this.reallocatorFactory = reallocatorFactory;
        this.restorerFactory = restorerFactory;
        this.scheduledProcessesFactory = scheduledProcessesFactory;
    }

    public BiologicalProcessesFactory() {

    }

    public ScheduledBiologicalProcessesFactory<B> getScheduledProcessesFactory() {
        return scheduledProcessesFactory;
    }

    public void setScheduledProcessesFactory(final ScheduledBiologicalProcessesFactory<B> scheduledProcessesFactory) {
        this.scheduledProcessesFactory = scheduledProcessesFactory;
    }

    public RestorerFactory<B> getRestorerFactory() {
        return restorerFactory;
    }

    public void setRestorerFactory(final RestorerFactory<B> restorerFactory) {
        this.restorerFactory = restorerFactory;
    }

    public InputPath getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(final InputPath inputFolder) {
        this.inputFolder = inputFolder;
    }

    public SpeciesCodesFromFileFactory getSpeciesCodesSupplier() {
        return speciesCodesSupplier;
    }

    public void setSpeciesCodesSupplier(final SpeciesCodesFromFileFactory speciesCodesSupplier) {
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    public Processes initProcesses(final NauticalMap nauticalMap, final FishState fishState) {
        getReallocatorFactory().setMapExtent(nauticalMap.getMapExtent());

        final Reallocator<B> reallocator = reallocatorFactory.apply(fishState);
        scheduledProcessesFactory.setReallocator(reallocator);
        restorerFactory.setReallocator(reallocator);
        biologyInitializerFactory.setReallocator(reallocator);

        final BiologyInitializer biologyInitializer = getBiologyInitializerFactory().apply(fishState);
        return new Processes(
            biologyInitializer,
            biologyInitializer.generateGlobal(fishState.getRandom(), fishState),
            ImmutableList.of(
                scheduledProcessesFactory,
                restorerFactory
            )
        );
    }

    public ReallocatorFactory<B, Reallocator<B>> getReallocatorFactory() {
        return reallocatorFactory;
    }

    public void setReallocatorFactory(final ReallocatorFactory<B, Reallocator<B>> reallocatorFactory) {
        this.reallocatorFactory = reallocatorFactory;
    }

    public BiologyInitializerFactory<B> getBiologyInitializerFactory() {
        return biologyInitializerFactory;
    }

    public void setBiologyInitializerFactory(final BiologyInitializerFactory<B> biologyInitializerFactory) {
        this.biologyInitializerFactory = biologyInitializerFactory;
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
