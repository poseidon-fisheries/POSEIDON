package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Booleans;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.SpecificProtectedArea;
import uk.ac.ox.oxfish.model.scenario.EpoGravityAbundanceScenario;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Iterables.getLast;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class SpecificProtectedAreaFactoryTest {

    final FishState fishState = startTestableScenario(EpoGravityAbundanceScenario.class);

    @Test
    public void testEveryEEZHasTilesInArea() {
        final Set<SpecificProtectedArea> areas =
            fishState.getFishers().stream().flatMap(fisher ->
                ((MultipleRegulations) fisher.getRegulation()).getRegulations()
                    .stream()
                    .filter(SpecificProtectedArea.class::isInstance)
                    .map(SpecificProtectedArea.class::cast)
            ).collect(toSet());

        final ImmutableList<SpecificProtectedArea> areasWithNoTiles =
            areas.stream()
                .filter(reg ->
                    Arrays.stream(reg.getInAreaArrayClone())
                        .noneMatch(booleans ->
                            Booleans.asList(booleans).stream().anyMatch(Boolean::booleanValue)
                        )
                )
                .collect(toImmutableList());

        Assertions.assertEquals(ImmutableList.of(), areasWithNoTiles);
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