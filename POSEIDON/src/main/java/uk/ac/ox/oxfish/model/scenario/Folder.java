package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;
import java.util.function.Supplier;

public interface Folder extends Supplier<Path> {

    default Path resolve(final Path other) {
        return get().resolve(other);
    }

    default Path resolve(final String other) {
        return get().resolve(other);
    }
}
