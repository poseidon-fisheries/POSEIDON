package uk.ac.ox.oxfish.geography.currents;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.scenario.Folder;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class CurrentPatternMapSupplier implements Supplier<Map<CurrentPattern, Path>> {

    public static final CurrentPatternMapSupplier EMPTY =
        new CurrentPatternMapSupplier(null, ImmutableMap.of());

    private Folder folder;
    private Map<CurrentPattern, Path> currentFiles;

    @SuppressWarnings("unused")
    public CurrentPatternMapSupplier() {
    }

    public CurrentPatternMapSupplier(final Folder folder, final Map<CurrentPattern, Path> currentFiles) {
        this.folder = folder;
        this.currentFiles = currentFiles;
    }

    @SuppressWarnings("unused")
    public Folder getInputFolder() {
        return folder;
    }

    @SuppressWarnings("unused")
    public void setInputFolder(final Folder folder) {
        this.folder = folder;
    }

    public Map<CurrentPattern, Path> getCurrentFiles() {
        return currentFiles;
    }

    public void setCurrentFiles(final Map<CurrentPattern, Path> currentFiles) {
        this.currentFiles = currentFiles;
    }

    @Override
    public Map<CurrentPattern, Path> get() {
        return currentFiles.entrySet().stream().collect(toImmutableMap(
            Entry::getKey,
            entry -> folder.resolve(entry.getValue())
        ));
    }
}
