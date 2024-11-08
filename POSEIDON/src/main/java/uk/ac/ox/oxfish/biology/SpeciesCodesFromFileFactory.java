package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.nio.file.Path;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableBiMap.toImmutableBiMap;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

/**
 * Builds a SpeciesCodes map from a CSV file. The CSV file columns must be:
 * <ul>
 *  <li>{@code species_code}</li>
 *  <li>{@code species_name}</li>
 * </ul>
 */
public class SpeciesCodesFromFileFactory implements AlgorithmFactory<SpeciesCodes>, Supplier<SpeciesCodes> {

    private static final CacheByFile<SpeciesCodes> cache =
        new CacheByFile<>(SpeciesCodesFromFileFactory::getSpeciesCodes);
    private InputPath speciesCodeFile;

    @SuppressWarnings("unused")
    public SpeciesCodesFromFileFactory() {
    }

    public SpeciesCodesFromFileFactory(
        final InputPath speciesCodeFile
    ) {
        this.speciesCodeFile = speciesCodeFile;
    }

    private static SpeciesCodes getSpeciesCodes(final Path path) {
        return new SpeciesCodes(
            recordStream(path).collect(toImmutableBiMap(
                r -> r.getString("species_code"),
                r -> r.getString("species_name")
            ))
        );
    }

    @SuppressWarnings("unused")
    public InputPath getSpeciesCodeFile() {
        return speciesCodeFile;
    }

    @SuppressWarnings("unused")
    public void setSpeciesCodeFile(final InputPath speciesCodeFile) {
        this.speciesCodeFile = speciesCodeFile;
    }

    @Override
    public SpeciesCodes apply(final FishState fishState) {
        return get();
    }

    @Override
    public SpeciesCodes get() {
        return cache.apply(speciesCodeFile.get());
    }
}
