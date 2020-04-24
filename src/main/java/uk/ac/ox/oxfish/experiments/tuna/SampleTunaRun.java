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

import com.google.common.collect.ImmutableList;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import uk.ac.ox.oxfish.model.data.heatmaps.BiomassHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.CatchFromFadSetsHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.CatchFromUnassociatedSetsHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.FadDensityHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.FadDeploymentHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.FadSetHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.HeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.UnassociatedSetHeatmapGatherer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class SampleTunaRun {

    private static final int NUM_YEARS_TO_RUN = 3;

    private static final Path basePath =
        Paths.get(System.getProperty("user.home"), "workspace");
    private static final Path scenarioPath =
        basePath.resolve(Paths.get("tuna", "np", "runs", "webviz_test", "tuna.yaml"));
    private static final Path outputPath =
        basePath.resolve(Paths.get("tuna", "np", "runs", "gatherers_test"));
    private static final File fisherDataOutputFile =
        outputPath.resolve("fisher_data.csv").toFile();
    private static final File heatmapDataOutputFile =
        outputPath.resolve("heatmap_data.csv").toFile();

    public static void main(final String[] args) {

        final TunaRunner tunaRunner = new TunaRunner(scenarioPath);
        final int interval = 30;

        tunaRunner.registerHeatmapGatherers(fishState -> {
            ImmutableList.Builder<HeatmapGatherer> gatherers = new ImmutableList.Builder<>();
            gatherers.add(
                new FadDeploymentHeatmapGatherer(interval),
                new FadSetHeatmapGatherer(interval),
                new UnassociatedSetHeatmapGatherer(interval),
                new FadDensityHeatmapGatherer(interval)
            );
            fishState.getSpecies().forEach(species -> {
                gatherers.add(new BiomassHeatmapGatherer(interval, species));
                gatherers.add(new CatchFromFadSetsHeatmapGatherer(interval, species));
                gatherers.add(new CatchFromUnassociatedSetsHeatmapGatherer(interval, species));
            });
            return gatherers.build();
        });

        tunaRunner.runUntilYear(NUM_YEARS_TO_RUN, model ->
            System.out.printf("%5d (year %d, day %3d)\n", model.getStep(), model.getYear(), model.getDayOfTheYear())
        );

        final CsvWriterSettings csvWriterSettings = new CsvWriterSettings();

        CsvWriter fisherDataWriter = new CsvWriter(fisherDataOutputFile, csvWriterSettings);
        fisherDataWriter.writeHeaders("boat_id", "year", "variable", "value");
        final Stream<List<?>> rows = tunaRunner.getFisherYearlyData(fisher -> fisher.getTags().get(0));
        fisherDataWriter.writeRowsAndClose(rows::iterator);

        tunaRunner.writeHeatmapData(new CsvWriter(heatmapDataOutputFile, csvWriterSettings));

    }

}
