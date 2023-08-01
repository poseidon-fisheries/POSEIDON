package uk.ac.ox.oxfish.regulation;

import com.google.common.io.MoreFiles;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.regulation.conditions.*;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.io.MoreFiles.getFileExtension;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class ForbiddenAreasFromShapeFiles implements AlgorithmFactory<Regulation> {

    private InputPath shapeFilesFolder;
    private InputPath tagsFile;

    public ForbiddenAreasFromShapeFiles() {
    }

    public ForbiddenAreasFromShapeFiles(
        final InputPath shapeFilesFolder,
        final InputPath tagsFile
    ) {
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

        final Map<String, AlgorithmFactory<Condition>> areaConditionsByName =
            loadAreaConditionsByName();

        final Map<Integer, Map<String, List<String>>> tagsByNameByYear =
            recordStream(getTagsFile().get())
                .collect(
                    groupingBy(
                        record -> record.getInt("year"),
                        groupingBy(
                            record -> record.getString("name"),
                            mapping(
                                record -> record.getString("tag"),
                                toList()
                            )
                        )
                    )
                );

        return new ForbiddenIf(
            new AnyOf(
                tagsByNameByYear.entrySet().stream().map(yearAndTagsByName ->
                    new AllOf(
                        new InYear(yearAndTagsByName.getKey()),
                        new AnyOf(
                            yearAndTagsByName.getValue().entrySet().stream().map(nameAndTags ->
                                new AllOf(
                                    new AgentHasAnyOfTags(nameAndTags.getValue()),
                                    areaConditionsByName.get(nameAndTags.getKey())
                                )
                            )
                        )
                    )
                )
            )
        ).apply(fishState);

    }

    private Map<String, AlgorithmFactory<Condition>> loadAreaConditionsByName() {
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
