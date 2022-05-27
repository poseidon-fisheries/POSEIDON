/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.data.monitors.regions;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.summarizingDouble;
import static java.util.stream.Collectors.toList;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.TESTS_INPUT_PATH;
import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Stream;
import junit.framework.TestCase;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoAbundanceScenario;

public class TicTacToeRegionalDivisionTest extends TestCase {

    /**
     * The goal of this method is to write the regional boundaries to a file more than it is to test
     * the TicTacToeRegionalDivision per se...
     */
    public void testWriteRegionalBoundaries() {
        final FishState fishState = startTestableScenario(EpoAbundanceScenario.class);
        final NauticalMap map = fishState.getMap();
        final TicTacToeRegionalDivision regionalDivision = new TicTacToeRegionalDivision(map);

        final ImmutableList<Object[]> rows = map.getAllSeaTilesAsList().stream()
            .collect(groupingBy(
                regionalDivision::getRegion,
                mapping(
                    map::getCoordinates,
                    collectingAndThen(
                        toList(),
                        coordinates -> {
                            final DoubleSummaryStatistics xs = coordinates.stream()
                                .collect(summarizingDouble(coord -> coord.x));
                            final DoubleSummaryStatistics ys = coordinates.stream()
                                .collect(summarizingDouble(coord -> coord.y));
                            return new Object[] {
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

        final File outputFile = TESTS_INPUT_PATH.resolve("regions.csv").toFile();
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