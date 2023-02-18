package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Subfolder implements Folder {

    Folder parentFolder;
    private Path subfolderPath;

    @SuppressWarnings("unused")
    public Subfolder() {
    }

    public Subfolder(final Folder parentFolder, final String subfolderPath) {
        this(parentFolder, Paths.get(subfolderPath));
    }

    public Subfolder(final Folder parentFolder, final Path subfolderPath) {
        this.parentFolder = parentFolder;
        this.subfolderPath = subfolderPath;
    }

    @SuppressWarnings("unused")
    public Path getSubfolderPath() {
        return subfolderPath;
    }

    @SuppressWarnings("unused")
    public void setSubfolderPath(final Path subfolderPath) {
        this.subfolderPath = subfolderPath;
    }

    @SuppressWarnings("unused")
    public Folder getParentFolder() {
        return parentFolder;
    }

    @SuppressWarnings("unused")
    public void setParentFolder(final Folder parentFolder) {
        this.parentFolder = parentFolder;
    }

    @Override
    public Path get() {
        return parentFolder.resolve(subfolderPath);
    }
}
