/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ObjectArrays;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision;

import java.io.*;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;

public class EpoScenarioCustomRegionalDivisionTest {

    final RegionalDivision division = EpoScenario.REGIONAL_DIVISION;

    @Test
    public void testLocationsInDivision() {

        final ImmutableMap<Coordinate, String> testPoints =
            new ImmutableMap.Builder<Coordinate, String>()
                .put(new Coordinate(-149.5, 49.5), "West")
                .put(new Coordinate(-140.5, -49.5), "West")
                .put(new Coordinate(-139.5, 50), "North")
                .put(new Coordinate(-90.5, 0.5), "North")
                .put(new Coordinate(-139.5, -0.5), "South")
                .put(new Coordinate(-90.5, -49.5), "South")
                .put(new Coordinate(-89.5, 49.5), "East")
                .put(new Coordinate(-70.5, -49.5), "East")
                .build();

        testPoints.forEach(((coordinate, regionName) ->
            Assertions.assertEquals(regionName, division.getRegion(coordinate).getName(), coordinate.toString())
        ));

    }

    /**
     * The goal of this method is to write the regional boundaries to a file more than it is to test the
     * TicTacToeRegionalDivision per se...
     */
    @Test
    public void testWriteRegionalBoundaries() {
        final FishState fishState = startTestableScenario(EpoGravityAbundanceScenario.class);
        final NauticalMap map = fishState.getMap();

        final ImmutableList<Object[]> rows = map.getAllSeaTilesAsList().stream()
            .collect(groupingBy(
                division::getRegion,
                mapping(
                    map::getCoordinates,
                    collectingAndThen(
                        toList(),
                        coordinates -> {
                            final DoubleSummaryStatistics xs = coordinates.stream()
                                .collect(summarizingDouble(coord -> coord.x));
                            final DoubleSummaryStatistics ys = coordinates.stream()
                                .collect(summarizingDouble(coord -> coord.y));
                            return new Object[]{
                                xs.getMin(), xs.getMax(), ys.getMin(), ys.getMax()
                            };
                        }
                    )
                )
            ))
            .entrySet()
            .stream()
            .map(entry -> ObjectArrays.concat(entry.getKey(), entry.getValue()))
            .sorted(
                Comparator
                    .<Object[]>comparingDouble(a -> (double) (a[1]))
                    .thenComparingDouble(a -> (double) (a[3]))
            )
            .collect(toImmutableList());

        final File outputFile = Paths.get("epo_inputs", "tests", "regions.csv").toFile();
        try (final Writer fileWriter = new FileWriter(outputFile)) {
            final CsvWriter csvWriter =
                new CsvWriter(new BufferedWriter(fileWriter), new CsvWriterSettings());
            csvWriter.writeHeaders("region", "min_lon", "max_lon", "min_lat", "max_lat");
            csvWriter.writeRowsAndClose(rows);
        } catch (final IOException e) {
            throw new IllegalStateException("Writing to " + outputFile + " failed.", e);
        }

    }
}
