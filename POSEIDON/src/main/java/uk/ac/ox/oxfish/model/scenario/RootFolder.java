package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;

public class RootFolder implements Folder {

    private Path path;

    public RootFolder() {
    }

    public RootFolder(final Path path) {
        this.path = path;
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
