package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class InputFile implements Supplier<Path> {
    private InputFolder inputFolder;
    private Path path;

    public InputFile() {
    }

    public InputFile(final InputFolder inputFolder, final String path) {
        this(inputFolder, Paths.get(path));
    }

    public InputFile(final InputFolder inputFolder, final Path path) {
        this.inputFolder = inputFolder;
        this.path = path;
    }

    public InputFolder getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(final InputFolder inputFolder) {
        this.inputFolder = inputFolder;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(final Path path) {
        this.path = path;
    }

    @Override
    public Path get() {
        return inputFolder.resolve(path);
    }
}
