package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;

public class Subfolder implements Folder {

    Folder parentFolder;
    private Path subfolderPath;

    public Subfolder() {
    }

    public Subfolder(final Folder parentFolder, final Path subfolderPath) {
        this.parentFolder = parentFolder;
        this.subfolderPath = subfolderPath;
    }

    public Path getSubfolderPath() {
        return subfolderPath;
    }

    public void setSubfolderPath(final Path subfolderPath) {
        this.subfolderPath = subfolderPath;
    }

    public Folder getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(final Folder parentFolder) {
        this.parentFolder = parentFolder;
    }

    @Override
    public Path resolve(final Path other) {
        return parentFolder.resolve(subfolderPath).resolve(other);
    }

    @Override
    public Path resolve(final String other) {
        return parentFolder.resolve(subfolderPath).resolve(other);
    }
}
