package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.collect.ImmutableMap;
import com.univocity.parsers.common.record.Record;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.scenario.Folder;
import uk.ac.ox.oxfish.model.scenario.InputFile;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.io.MoreFiles.getFileExtension;
import static com.google.common.io.MoreFiles.getNameWithoutExtension;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;


public class ProtectedAreasFromFolderFactory implements AlgorithmFactory<MultipleRegulations> {

    final CacheByFile<Map<String, AlgorithmFactory<? extends Regulation>>> factoriesCache =
        new CacheByFile<>(this::loadShapeFiles);
    private Folder shapefilesFolder;
    private InputFile tagsFile;

    @SuppressWarnings("unused")
    public ProtectedAreasFromFolderFactory() {
    }

    public ProtectedAreasFromFolderFactory(final Folder shapefilesFolder, final String tagsFile) {
        this(shapefilesFolder, new InputFile(shapefilesFolder, tagsFile));
    }

    public ProtectedAreasFromFolderFactory(final Folder shapefilesFolder, final InputFile tagsFile) {
        this.shapefilesFolder = shapefilesFolder;
        this.tagsFile = tagsFile;
    }

    private ImmutableMap<String, AlgorithmFactory<? extends Regulation>> loadShapeFiles(
        final Path shapefilesFolder
    ) {
        try {
            //noinspection UnstableApiUsage
            return Files.list(shapefilesFolder)
                .filter(path -> getFileExtension(path).equals("shp"))
                .collect(toImmutableMap(
                    path -> getNameWithoutExtension(path.getFileName()),
                    SpecificProtectedAreaFromShapeFileFactory::new
                ));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Folder getShapefilesFolder() {
        return shapefilesFolder;
    }

    @SuppressWarnings("unused")
    public void setShapefilesFolder(final Folder shapefilesFolder) {
        this.shapefilesFolder = shapefilesFolder;
    }

    public InputFile getTagsFile() {
        return tagsFile;
    }

    @SuppressWarnings("unused")
    public void setTagsFile(final InputFile tagsFile) {
        this.tagsFile = tagsFile;
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
}
