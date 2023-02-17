package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class InputFile implements Supplier<Path> {
    private Folder folder;
    private Path path;

    public InputFile() {
    }

    public InputFile(final Folder folder, final String path) {
        this(folder, Paths.get(path));
    }

    public InputFile(final Folder folder, final Path path) {
        this.folder = folder;
        this.path = path;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(final Folder folder) {
        this.folder = folder;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(final Path path) {
        this.path = path;
    }

    @Override
    public Path get() {
        return folder.resolve(path);
    }
}
