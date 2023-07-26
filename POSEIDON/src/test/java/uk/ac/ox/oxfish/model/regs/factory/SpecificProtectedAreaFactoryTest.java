package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoGravityAbundanceScenario;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.regulation.ForbiddenAreasFromShapeFiles;
import uk.ac.ox.poseidon.regulations.api.Regulation;
import uk.ac.ox.poseidon.regulations.core.ConditionalRegulation;
import uk.ac.ox.poseidon.regulations.core.conditions.AllOf;
import uk.ac.ox.poseidon.regulations.core.conditions.AnyOf;
import uk.ac.ox.poseidon.regulations.core.conditions.InVectorField;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Iterables.getLast;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class SpecificProtectedAreaFactoryTest {

    final FishState fishState = startTestableScenario(EpoGravityAbundanceScenario.class);

    final InputPath regionsFolder = InputPath.of("inputs", "epo_inputs", "regions");
    final Regulation regulation =
        new ForbiddenAreasFromShapeFiles(
            regionsFolder,
            regionsFolder.path("region_tags.csv")
        ).apply(fishState);
    final List<InVectorField> vectorFields =
        ((AnyOf) ((ConditionalRegulation) regulation).getCondition())
            .getConditions()
            .stream()
            .map(AllOf.class::cast)
            .map(AllOf::getConditions)
            .flatMap(Collection::stream)
            .filter(InVectorField.class::isInstance)
            .map(InVectorField.class::cast)
            .collect(toList());
    final NauticalMap map = fishState.getMap();
    final List<Coordinate> coordinates =
        map.getAllSeaTilesExcludingLandAsList()
            .stream()
            .map(map::getCoordinates)
            .collect(toList());

    @Test
    public void testEveryEEZHasTilesInArea() {
        vectorFields
            .forEach(inVectorField ->
                Assertions.assertTrue(coordinates.stream().anyMatch(inVectorField::test))
            );
    }

    @Test
    public void testEEZPoints() {

        final Map<String, List<TestPoint>> testPoints =
            recordStream(Paths.get("inputs", "epo_inputs", "tests")
                .resolve("regions_test_points.csv"))
                .collect(
                    groupingBy(
                        record -> record.getString("flag"),
                        mapping(
                            record -> new TestPoint(
                                record.getString("eez_name"),
                                record.getString("flag"),
                                record.getBoolean("allowed"),
                                new Coordinate(record.getDouble("lon"), record.getDouble("lat"))
                            ),
                            toList()
                        )
                    )
                );

        final ImmutableList<TestPoint> failures =
            fishState.getFishers()
                .stream()
                .flatMap(fisher ->
                    testPoints
                        .get(getLast(fisher.getTagsList()))
                        .stream()
                        .filter(testPoint ->
                            testPoint.shouldBeAllowed !=
                                fisher.getRegulation().canFishHere(
                                    fisher,
                                    fishState.getMap().getSeaTile(testPoint.coordinate),
                                    fishState,
                                    100 // make sure we're outside of IATTC January closure
                                )
                        )
                )
                .collect(toImmutableList());

        Assertions.assertEquals(ImmutableList.of(), failures);

    }

    private static class TestPoint {
        final String eezName;
        final String flag;
        final boolean shouldBeAllowed;
        final Coordinate coordinate;

        private TestPoint(
            final String eezName,
            final String flag,
            final boolean shouldBeAllowed,
            final Coordinate coordinate
        ) {
            this.eezName = eezName;
            this.flag = flag;
            this.shouldBeAllowed = shouldBeAllowed;
            this.coordinate = coordinate;
        }

        @Override
        public String toString() {
            return "TestPoint{" +
                "eezName='" + eezName + '\'' +
                ", flag='" + flag + '\'' +
                ", shouldBeAllowed=" + shouldBeAllowed +
                ", coordinate=" + coordinate +
                '}';
        }
    }
}