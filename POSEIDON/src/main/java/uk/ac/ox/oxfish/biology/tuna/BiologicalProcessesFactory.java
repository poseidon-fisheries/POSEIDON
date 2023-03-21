package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.List;

public abstract class BiologicalProcessesFactory<B extends LocalBiology> {
    private InputPath inputFolder;
    private SpeciesCodesFromFileFactory speciesCodesSupplier;

    public BiologicalProcessesFactory(
        final InputPath inputFolder,
        final SpeciesCodesFromFileFactory speciesCodesSupplier
    ) {
        this.inputFolder = inputFolder;
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    public BiologicalProcessesFactory() {

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

    public static class Processes {
        public final BiologyInitializer biologyInitializer;
        public final List<AlgorithmFactory<? extends Startable>> startableFactories;

        public Processes(
            final BiologyInitializer biologyInitializer,
            final List<AlgorithmFactory<? extends Startable>> startableFactories
        ) {
            this.biologyInitializer = biologyInitializer;
            this.startableFactories = startableFactories;
        }
    }
}
