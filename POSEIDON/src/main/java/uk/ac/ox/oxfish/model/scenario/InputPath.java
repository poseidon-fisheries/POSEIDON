package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class InputPath implements Supplier<Path> {

    private InputPath parent;
    private Path path;

    public InputPath() {
    }

    private InputPath(final InputPath parent, final Path path) {
        this.parent = parent;
        this.path = path;
    }

    public static InputPath of(final Path path) {
        return new InputPath(null, path);
    }

    public static InputPath of(final String first, final String... more) {
        return of(Paths.get(first, more));
    }

    public InputPath getParent() {
        return parent;
    }

    public void setParent(final InputPath parent) {
        this.parent = parent;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(final Path path) {
        this.path = path;
    }

    public InputPath path(final String first, final String... more) {
        return path(Paths.get(first, more));
    }

    public InputPath path(final Path path) {
        return new InputPath(this, path);
    }

    @Override
    public Path get() {
        return parent == null ? path : parent.get().resolve(path);
    }
}
