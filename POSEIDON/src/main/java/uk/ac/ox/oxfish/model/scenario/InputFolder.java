package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;

public class InputFolder {

    private Path path;

    public InputFolder() {
    }

    public InputFolder(final Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(final Path path) {
        this.path = path;
    }

    public Path resolve(final Path other) {
        return path.resolve(other);
    }

    public Path resolve(final String other) {
        return path.resolve(other);
    }

}
