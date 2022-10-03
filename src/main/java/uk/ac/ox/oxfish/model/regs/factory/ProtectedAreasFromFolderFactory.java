package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.cache.CacheLoader.from;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.io.MoreFiles.getFileExtension;
import static com.google.common.io.MoreFiles.getNameWithoutExtension;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;


public class ProtectedAreasFromFolderFactory implements AlgorithmFactory<MultipleRegulations> {

    final LoadingCache<Path, Map<String, AlgorithmFactory<? extends Regulation>>> factoriesCache =
        CacheBuilder.newBuilder().build(from(this::loadShapeFiles));
    private Path shapefilesFolder;
    private Path tagsFile;

    @SuppressWarnings("unused")
    public ProtectedAreasFromFolderFactory() {
    }

    public ProtectedAreasFromFolderFactory(Path shapefilesFolder, Path tagsFile) {
        this.shapefilesFolder = shapefilesFolder;
        this.tagsFile = tagsFile;
    }

    private ImmutableMap<String, AlgorithmFactory<? extends Regulation>> loadShapeFiles(Path shapefilesFolder) {
        try {
            //noinspection UnstableApiUsage
            return Files.list(shapefilesFolder)
                .filter(path -> getFileExtension(path).equals("shp"))
                .collect(toImmutableMap(
                    path -> getNameWithoutExtension(path.getFileName()),
                    SpecificProtectedAreaFromShapeFileFactory::new
                ));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Path getShapefilesFolder() {
        return shapefilesFolder;
    }

    @SuppressWarnings("unused")
    public void setShapefilesFolder(Path shapefilesFolder) {
        this.shapefilesFolder = shapefilesFolder;
    }

    public Path getTagsFile() {
        return tagsFile;
    }

    @SuppressWarnings("unused")
    public void setTagsFile(Path tagsFile) {
        this.tagsFile = tagsFile;
    }

    @Override
    public MultipleRegulations apply(FishState fishState) {

        final Map<String, AlgorithmFactory<? extends Regulation>> factoriesByName =
            factoriesCache.getUnchecked(getShapefilesFolder());

        final Map<String, List<AlgorithmFactory<? extends Regulation>>> factoriesByTag =
            recordStream(getShapefilesFolder().resolve(getTagsFile()))
                .collect(groupingBy(
                    record -> record.getString("tag"),
                    mapping(
                        record -> checkNotNull(factoriesByName.get(record.getString("name"))),
                        toList()
                    )
                ));

        return new MultipleRegulations(factoriesByTag);

    }
}
