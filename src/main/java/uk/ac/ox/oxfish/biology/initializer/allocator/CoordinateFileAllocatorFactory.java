package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CoordinateFileAllocatorFactory implements AlgorithmFactory<CoordinateFileBiomassAllocator> {


    private Path biomassPath = Paths.get("inputs","tests","fake_indo_abundance.csv");

    private boolean inputFileHasHeader = true;


    @Override
    public CoordinateFileBiomassAllocator apply(FishState fishState) {
        return new CoordinateFileBiomassAllocator(biomassPath,
                inputFileHasHeader);
    }

    public Path getBiomassPath() {
        return biomassPath;
    }

    public void setBiomassPath(Path biomassPath) {
        this.biomassPath = biomassPath;
    }

    public boolean isInputFileHasHeader() {
        return inputFileHasHeader;
    }

    public void setInputFileHasHeader(boolean inputFileHasHeader) {
        this.inputFileHasHeader = inputFileHasHeader;
    }
}
