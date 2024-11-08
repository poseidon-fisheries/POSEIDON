package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.collect.ImmutableMap;
import com.univocity.parsers.common.record.Record;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.io.MoreFiles.getFileExtension;
import static com.google.common.io.MoreFiles.getNameWithoutExtension;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class ProtectedAreasFromFolderFactory implements AlgorithmFactory<MultipleRegulations> {

    private static final CacheByFile<Map<String, AlgorithmFactory<? extends Regulation>>> factoriesCache =
        new CacheByFile<>(ProtectedAreasFromFolderFactory::loadShapeFiles);
    private InputPath shapefilesInputPath;
    private InputPath tagsFile;

    @SuppressWarnings("unused")
    public ProtectedAreasFromFolderFactory() {
    }

    public ProtectedAreasFromFolderFactory(
        final InputPath shapefilesInputPath,
        final String tagsFile
    ) {
        this(shapefilesInputPath, shapefilesInputPath.path(tagsFile));
    }

    public ProtectedAreasFromFolderFactory(
        final InputPath shapefilesInputPath,
        final InputPath tagsFile
    ) {
        this.shapefilesInputPath = shapefilesInputPath;
        this.tagsFile = tagsFile;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static ImmutableMap<String, AlgorithmFactory<? extends Regulation>> loadShapeFiles(
        final Path shapefilesFolder
    ) {
        try (final Stream<Path> files = Files.list(shapefilesFolder)) {
            return files
                .filter(path -> getFileExtension(path).equals("shp"))
                .collect(toImmutableMap(
                    path -> getNameWithoutExtension(path.getFileName()),
                    SpecificProtectedAreaFromShapeFileFactory::new
                ));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public MultipleRegulations apply(final FishState fishState) {

        final Map<String, AlgorithmFactory<? extends Regulation>> factoriesByName =
            factoriesCache.apply(getShapefilesFolder().get());

        final Map<String, List<AlgorithmFactory<? extends Regulation>>> factoriesByTag =
            recordStream(getTagsFile().get())
                .collect(groupingBy(
                    record -> record.getString("tag"),
                    mapping(
                        (Record record) -> checkNotNull(factoriesByName.get(record.getString("name"))),
                        toList()
                    )
                ));

        return new MultipleRegulations(factoriesByTag);

    }

    public InputPath getShapefilesFolder() {
        return shapefilesInputPath;
    }

    @SuppressWarnings("unused")
    public void setShapefilesFolder(final InputPath shapefilesInputPath) {
        this.shapefilesInputPath = shapefilesInputPath;
    }

    public InputPath getTagsFile() {
        return tagsFile;
    }

    @SuppressWarnings("unused")
    public void setTagsFile(final InputPath tagsFile) {
        this.tagsFile = tagsFile;
    }
}
