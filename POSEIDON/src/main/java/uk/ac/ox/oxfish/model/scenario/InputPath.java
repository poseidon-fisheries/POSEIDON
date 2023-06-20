package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.utility.parameters.PathParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class InputPath implements Supplier<Path> {

    private InputPath parent;
    private PathParameter path;

    public InputPath() {
    }

    private InputPath(final InputPath parent, final PathParameter path) {
        this.parent = parent;
        this.path = path;
    }

    public static InputPath of(final String first, final String... more) {
        return of(Paths.get(first, more));
    }

    public static InputPath of(final Path path) {
        return new InputPath(null, new PathParameter(path));
    }

    public InputPath getParent() {
        return parent;
    }

    public void setParent(final InputPath parent) {
        this.parent = parent;
    }

    public PathParameter getPath() {
        return path;
    }

    public void setPath(final PathParameter path) {
        this.path = path;
    }

    public InputPath path(final String first, final String... more) {
        return path(Paths.get(first, more));
    }

    public InputPath path(final Path path) {
        return new InputPath(this, new PathParameter(path));
    }

    @Override
    public Path get() {
        return parent == null ? path.getValue() : parent.get().resolve(path.getValue());
    }

    @Override
    public String toString() {
        return get().toString();
    }
}
