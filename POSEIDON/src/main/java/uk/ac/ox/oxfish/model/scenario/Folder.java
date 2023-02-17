package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;

public interface Folder {
    Path resolve(Path other);

    Path resolve(String other);
}
