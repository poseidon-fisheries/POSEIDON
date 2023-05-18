package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CoordinateFileAllocatorFactory implements FileBiomassAllocatorFactory {


    private Path biomassPath = Paths.get("inputs", "tests", "fake_indo_abundance.csv");

    private boolean inputFileHasHeader = true;


    @Override
    public CoordinateFileBiomassAllocator apply(FishState fishState) {
        return new CoordinateFileBiomassAllocator(
            biomassPath,
            inputFileHasHeader
        );
    }

    @Override
    public Path getBiomassPath() {
        return biomassPath;
    }

    @Override
    public void setBiomassPath(Path biomassPath) {
        this.biomassPath = biomassPath;
    }

    @Override
    public boolean isInputFileHasHeader() {
        return inputFileHasHeader;
    }

    @Override
    public void setInputFileHasHeader(boolean inputFileHasHeader) {
        this.inputFileHasHeader = inputFileHasHeader;
    }
}
