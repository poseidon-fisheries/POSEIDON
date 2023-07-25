package uk.ac.ox.oxfish.regulation;

import com.google.common.io.MoreFiles;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.regulation.conditions.AgentHasAnyOfTags;
import uk.ac.ox.oxfish.regulation.conditions.AllOf;
import uk.ac.ox.oxfish.regulation.conditions.AnyOf;
import uk.ac.ox.oxfish.regulation.conditions.InVectorField;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.io.MoreFiles.getFileExtension;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class ForbiddenAreasFromShapeFiles implements AlgorithmFactory<Regulation> {

    private InputPath shapeFilesFolder;
    private InputPath tagsFile;

    public ForbiddenAreasFromShapeFiles() {
    }

    public ForbiddenAreasFromShapeFiles(final InputPath shapeFilesFolder, final InputPath tagsFile) {
        this.shapeFilesFolder = shapeFilesFolder;
        this.tagsFile = tagsFile;
    }

    public InputPath getShapeFilesFolder() {
        return shapeFilesFolder;
    }

    public void setShapeFilesFolder(final InputPath shapeFilesFolder) {
        this.shapeFilesFolder = shapeFilesFolder;
    }

    @Override
    public Regulation apply(final FishState fishState) {

        final Map<String, AlgorithmFactory<Condition>> factoriesByName =
            makeFactoriesFromShapeFiles();

        final Map<String, List<String>> tagsByName =
            recordStream(getTagsFile().get())
                .collect(groupingBy(
                    record -> record.getString("name"),
                    mapping(
                        record -> record.getString("tag"),
                        toList()
                    )
                ));

        return new ForbiddenIf(
            new AnyOf(
                factoriesByName.entrySet().stream()
                    .map(entry -> new AllOf(
                        new AgentHasAnyOfTags(tagsByName.get(entry.getKey())),
                        entry.getValue()
                    ))
                    .collect(toImmutableList())
            )
        ).apply(fishState);

    }

    private Map<String, AlgorithmFactory<Condition>> makeFactoriesFromShapeFiles() {
        try (final Stream<Path> files = Files.list(shapeFilesFolder.get())) {
            return files
                .filter(path -> getFileExtension(path).equals("shp"))
                .map(Path::getFileName)
                .collect(toImmutableMap(
                    MoreFiles::getNameWithoutExtension,
                    fileName -> new InVectorField(shapeFilesFolder.path(fileName))
                ));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public InputPath getTagsFile() {
        return tagsFile;
    }

    public void setTagsFile(final InputPath tagsFile) {
        this.tagsFile = tagsFile;
    }
}
