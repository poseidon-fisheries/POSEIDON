package uk.ac.ox.poseidon.regulations.core;

import com.google.common.io.MoreFiles;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.csv.CsvParserUtil;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.conditions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.io.MoreFiles.getFileExtension;
import static java.util.stream.Collectors.*;

public class ForbiddenAreasFromShapeFiles implements ComponentFactory<Regulations> {

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
    public Regulations apply(final ModelState modelState) {

        final Map<String, ComponentFactory<Condition>> areaConditionsByName =
            loadAreaConditionsByName();

        final Map<Integer, Map<String, List<String>>> tagsByNameByYear =
            CsvParserUtil.recordStream(getTagsFile().get())
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

        return new ForbiddenIfFactory(
            new AnyOfFactory(
                tagsByNameByYear.entrySet().stream().map(yearAndTagsByName ->
                    new AllOfFactory(
                        new InYearFactory(yearAndTagsByName.getKey()),
                        new AnyOfFactory(
                            yearAndTagsByName.getValue().entrySet().stream().map(nameAndTags ->
                                new AllOfFactory(
                                    new AgentHasAnyOfTagsFactory(nameAndTags.getValue()),
                                    areaConditionsByName.get(nameAndTags.getKey())
                                )
                            )
                        )
                    )
                )
            )
        ).apply(modelState);

    }

    private Map<String, ComponentFactory<Condition>> loadAreaConditionsByName() {
        try (final Stream<Path> files = Files.list(shapeFilesFolder.get())) {
            return files
                .filter(path -> getFileExtension(path).equals("shp"))
                .map(Path::getFileName)
                .collect(toImmutableMap(
                    MoreFiles::getNameWithoutExtension,
                    fileName -> new InVectorFieldFactory(shapeFilesFolder.path(fileName))
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
