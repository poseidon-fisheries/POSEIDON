package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;

public interface FileBiomassAllocatorFactory extends AlgorithmFactory<FileBiomassAllocator> {
    @Override FileBiomassAllocator apply(FishState fishState);
    Path getBiomassPath();
    void setBiomassPath(Path biomassPath);
    boolean isInputFileHasHeader();
    void setInputFileHasHeader(boolean inputFileHasHeader);
}
