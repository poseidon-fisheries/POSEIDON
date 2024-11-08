package uk.ac.ox.poseidon.common.core.parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathParameter extends FixedParameter<Path> {

    public PathParameter(final String path) {
        this(Paths.get(path));
    }

    public PathParameter(final Path value) {
        super(value);
    }
}
