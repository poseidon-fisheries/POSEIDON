package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RootFolder implements Folder {

    private Path path;

    public RootFolder() {
    }

    public RootFolder(final Path path) {
        this.path = path;
    }

    public RootFolder(String first, String... more) {
        this(Paths.get(first, more));
    }

    public Path getPath() {
        return path;
    }

    public void setPath(final Path path) {
        this.path = path;
    }

    @Override
    public Path get() {
        return path;
    }
}
