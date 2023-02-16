package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;

public class RootFolder implements InputFolder {

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
    public Path resolve(final Path other) {
        return path.resolve(other);
    }

    @Override
    public Path resolve(final String other) {
        return path.resolve(other);
    }

}
