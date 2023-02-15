package uk.ac.ox.oxfish.geography.currents;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.scenario.InputFolder;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class CurrentPatternMapSupplier implements Supplier<Map<CurrentPattern, Path>> {

    public static final CurrentPatternMapSupplier EMPTY =
        new CurrentPatternMapSupplier(null, ImmutableMap.of());

    private InputFolder inputFolder;
    private Map<CurrentPattern, Path> currentFiles;

    @SuppressWarnings("unused")
    public CurrentPatternMapSupplier() {
    }

    public CurrentPatternMapSupplier(final InputFolder inputFolder, final Map<CurrentPattern, Path> currentFiles) {
        this.inputFolder = inputFolder;
        this.currentFiles = currentFiles;
    }

    @SuppressWarnings("unused")
    public InputFolder getInputFolder() {
        return inputFolder;
    }

    @SuppressWarnings("unused")
    public void setInputFolder(final InputFolder inputFolder) {
        this.inputFolder = inputFolder;
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
            entry -> inputFolder.resolve(entry.getValue())
        ));
    }
}
