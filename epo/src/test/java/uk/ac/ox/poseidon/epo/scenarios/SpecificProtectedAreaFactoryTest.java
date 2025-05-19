/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.epo.scenarios;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.core.BasicAction;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.regulations.api.ConditionalRegulations;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.ForbiddenAreasFromShapeFiles;
import uk.ac.ox.poseidon.regulations.core.conditions.AllOf;
import uk.ac.ox.poseidon.regulations.core.conditions.InVectorField;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.getLast;
import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

class SpecificProtectedAreaFactoryTest {

    private final FishState fishState = startTestableScenario(EpoGravityAbundanceScenario.class);

    private final InputPath regionsFolder =
        ((EpoScenario<?>) fishState.getScenario())
            .getInputFolder()
            .path("regions");
    private final Regulations regulations =
        new ForbiddenAreasFromShapeFiles(
            regionsFolder,
            regionsFolder.path("region_tags.csv")
        ).apply(fishState);
    private final List<InVectorField> vectorFields =
        ((ConditionalRegulations) regulations).getCondition()
            .getSubConditions()
            .stream()
            .map(AllOf.class::cast)
            .map(AllOf::getSubConditions)
            .flatMap(Collection::stream)
            .filter(InVectorField.class::isInstance)
            .map(InVectorField.class::cast)
            .collect(toList());
    private final NauticalMap map = fishState.getMap();
    private final List<Coordinate> coordinates =
        map.getAllSeaTilesExcludingLandAsList()
            .stream()
            .map(map::getCoordinates)
            .collect(toList());

    @Test
    void testEveryEEZHasTilesInArea() {
        vectorFields
            .forEach(inVectorField ->
                Assertions.assertTrue(coordinates.stream().anyMatch(inVectorField::test))
            );
    }

    @Test
    void testEEZPoints() {

        final Path testPointsFile =
            ((EpoScenario<?>) fishState.getScenario())
                .testFolder()
                .get()
                .resolve("regions_test_points.csv");
        final Map<String, List<TestPoint>> testPoints =
            recordStream(testPointsFile)
                .filter(r -> r.getInt("year") == fishState.getCalendarYear())
                .collect(
                    groupingBy(
                        record -> record.getString("flag"),
                        mapping(
                            record -> new TestPoint(
                                record.getString("name"),
                                record.getInt("year"),
                                record.getString("flag"),
                                record.getBoolean("allowed"),
                                new Coordinate(record.getDouble("lon"), record.getDouble("lat"))
                            ),
                            toList()
                        )
                    )
                );

        fishState.getFishers().forEach(fisher ->
            testPoints
                .get(getLast(fisher.getTagsList()))
                .forEach(testPoint -> {
                    final Action action = new BasicAction(
                        "FAD",
                        fisher,
                        LocalDate.of(testPoint.year, 1, 1).atStartOfDay(),
                        testPoint.coordinate
                    );
                    assertEquals(
                        testPoint.shouldBeAllowed,
                        regulations.isPermitted(action),
                        () -> action + ", " + testPoint
                    );
                })
        );

    }

    private static class TestPoint {
        final String eezName;
        final int year;
        final String flag;
        final boolean shouldBeAllowed;
        final Coordinate coordinate;

        private TestPoint(
            final String eezName,
            final int year,
            final String flag,
            final boolean shouldBeAllowed,
            final Coordinate coordinate
        ) {
            this.eezName = eezName;
            this.year = year;
            this.flag = flag;
            this.shouldBeAllowed = shouldBeAllowed;
            this.coordinate = coordinate;
        }

        @Override
        public String toString() {
            return "TestPoint{" +
                "eezName='" + eezName + '\'' +
                ", year=" + year +
                ", flag='" + flag + '\'' +
                ", shouldBeAllowed=" + shouldBeAllowed +
                ", coordinate=" + coordinate +
                '}';
        }
    }
}
