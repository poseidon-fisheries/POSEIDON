/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.experiments.tuna;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class SampleTunaRun {

    private static final int NUM_YEARS_TO_RUN = 1;

    private static final Path basePath =
        Paths.get(System.getProperty("user.home"), "workspace");
    private static final Path scenarioPath =
        basePath.resolve(Paths.get("tuna", "np", "runs", "webviz_test", "tuna.yaml"));
    private static final Path outputPath =
        basePath.resolve(Paths.get("tuna", "np", "runs", "gatherers_test"));
    private static final File outputFile =
        outputPath.resolve("results.csv").toFile();

    public static void main(final String[] args) {
        final TunaRunner tunaRunner = new TunaRunner(scenarioPath);
        tunaRunner.runUntilYear(NUM_YEARS_TO_RUN, model ->
            System.out.printf("%5d (year %d, day %3d)\n", model.getStep(), model.getYear(), model.getDayOfTheYear())
        );
        CsvWriter csvWriter = new CsvWriter(outputFile, new CsvWriterSettings());
        csvWriter.writeHeaders("boat_id", "year", "variable", "value");
        final Stream<List<?>> rows = tunaRunner.getFisherYearlyData(fisher -> fisher.getTags().get(0));
        csvWriter.writeRowsAndClose(rows::iterator);
    }

}
