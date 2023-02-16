package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;

public class Subfolder implements InputFolder {

    InputFolder parentFolder;
    private Path subfolderPath;

    public Subfolder() {
    }

    public Subfolder(final InputFolder parentFolder, final Path subfolderPath) {
        this.parentFolder = parentFolder;
        this.subfolderPath = subfolderPath;
    }

    public Path getSubfolderPath() {
        return subfolderPath;
    }

    public void setSubfolderPath(final Path subfolderPath) {
        this.subfolderPath = subfolderPath;
    }

    public InputFolder getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(final InputFolder parentFolder) {
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
