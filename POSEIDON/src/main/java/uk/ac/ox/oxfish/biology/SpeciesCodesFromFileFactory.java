package uk.ac.ox.oxfish.biology;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.model.scenario.InputFolder;

import java.nio.file.Path;
import java.util.function.Supplier;

import static com.google.common.cache.CacheLoader.from;
import static com.google.common.collect.ImmutableBiMap.toImmutableBiMap;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

/**
 * Builds a SpeciesCodes map from a CSV file. The CSV file columns must be:
 * <ul>
 *  <li>{@code species_code}</li>
 *  <li>{@code species_name}</li>
 * </ul>
 */
public class SpeciesCodesFromFileFactory implements Supplier<SpeciesCodes> {

    private static final LoadingCache<Path, SpeciesCodes> cache =
        CacheBuilder.newBuilder().build(from(SpeciesCodesFromFileFactory::getSpeciesCodes));
    private InputFolder inputsFolder;
    private Path speciesCodeFile;

    @SuppressWarnings("unused")
    public SpeciesCodesFromFileFactory() {
    }

    public SpeciesCodesFromFileFactory(
        final InputFolder inputsFolder,
        final Path speciesCodeFile
    ) {
        this.inputsFolder = inputsFolder;
        this.speciesCodeFile = speciesCodeFile;
    }

    @NotNull
    private static SpeciesCodes getSpeciesCodes(final Path path) {
        return new SpeciesCodes(
            recordStream(path).collect(toImmutableBiMap(
                r -> r.getString("species_code"),
                r -> r.getString("species_name")
            ))
        );
    }

    @SuppressWarnings("unused")
    public InputFolder getInputsFolder() {
        return inputsFolder;
    }

    @SuppressWarnings("unused")
    public void setInputsFolder(final InputFolder inputsFolder) {
        this.inputsFolder = inputsFolder;
    }

    @SuppressWarnings("unused")
    public Path getSpeciesCodeFile() {
        return speciesCodeFile;
    }

    @SuppressWarnings("unused")
    public void setSpeciesCodeFile(final Path speciesCodeFile) {
        this.speciesCodeFile = speciesCodeFile;
    }

    @Override
    public SpeciesCodes get() {
        return cache.getUnchecked(inputsFolder.resolve(speciesCodeFile));
    }
}
