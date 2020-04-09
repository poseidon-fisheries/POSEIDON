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
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.regions.TicTacToeRegionalDivision;
import uk.ac.ox.oxfish.model.data.webviz.JsonOutputManagerFactory;
import uk.ac.ox.oxfish.model.data.webviz.charts.ChartBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.events.SinglePeriodEventBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.AverageNumberOfActiveFadsHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.BiomassSnapshotHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.vessels.SingleTypeVesselClassifier;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.zip;
import static java.util.stream.Stream.concat;
import static org.jfree.chart.ChartColor.LIGHT_BLUE;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.writeAdditionalOutputsToFolder;

@SuppressWarnings("UnstableApiUsage")
public final class WebVizTest {

    private static final Path basePath =
        Paths.get(System.getProperty("user.home"), "workspace");
    private static final Path scenarioPath =
        basePath.resolve(Paths.get("tuna", "np", "runs", "webviz_test", "tuna.yaml"));
    private static final Path outputPath =
        basePath.resolve(Paths.get("poseidon-webviz", "public", "testdata"));

    public static void main(final String[] args) throws IOException {

        final TunaScenario scenario = new FishYAML().loadAs(new FileReader(scenarioPath.toFile()), TunaScenario.class);

        final Set<String> speciesNames = TunaScenario.speciesNames.values();

        final JsonOutputManagerFactory jsonOutputManagerFactory = new JsonOutputManagerFactory();
        jsonOutputManagerFactory.setScenarioTitle("Tuna test");
        jsonOutputManagerFactory.setScenarioDescription(
            "This is sample output from the current tuna simulation, " +
                "over a period of three years after one year of 'spin up'.");
        jsonOutputManagerFactory.setStartDate("2017-01-01");
        jsonOutputManagerFactory.setNumYearsToSkip(1);
        jsonOutputManagerFactory.setPrettyPrinting(true);

        jsonOutputManagerFactory.setVesselClassifier(
            new SingleTypeVesselClassifier(1, "Class 6 vessels", LIGHT_BLUE)
        );

        jsonOutputManagerFactory.setEventBuilderFactories(ImmutableList.of(
            new SinglePeriodEventBuilderFactory("Closure period A", 210, 281),
            new SinglePeriodEventBuilderFactory("El Corralito closure", 282, 312),
            new SinglePeriodEventBuilderFactory("Closure period B", 313, 364 + 19),

            new SinglePeriodEventBuilderFactory("Closure period A", (364 * 1) + 210, (364 * 1) + 281),
            new SinglePeriodEventBuilderFactory("El Corralito closure", (364 * 1) + 282, (364 * 1) + 312),
            new SinglePeriodEventBuilderFactory("Closure period B", (364 * 1) + 313, (364 * 1) + 364 + 19),

            new SinglePeriodEventBuilderFactory("Closure period A", (364 * 2) + 210, (364 * 2) + 281),
            new SinglePeriodEventBuilderFactory("El Corralito closure", (364 * 2) + 282, (364 * 2) + 312),
            new SinglePeriodEventBuilderFactory("Closure period B", (364 * 2) + 313, (364 * 2) + 364 + 19)

        ));

        final ImmutableList.Builder<ChartBuilderFactory> chartBuilderFactories = new ImmutableList.Builder<>();
        ImmutableList.of(
            entry("Biomass (kg)", "Biomass %s"),
            entry("Landings (kg)", "%s Landings"),
            entry("Recruitment (kg)", "%s Recruitment"),
            entry("Average catch per set (kg)", "Average %s catches by set"),
            entry("Total catch from FAD sets (kg)", "Sum of %s catches from FAD sets"),
            entry("Total catch from unassociated sets (kg)", "Sum of %s catches from unassociated sets"),
            entry("Total biomass under FADs (kg)", "Sum of %s biomass under FADs")
        ).forEach(entry -> chartBuilderFactories.add(
            ChartBuilderFactory.forPattern(entry.getKey(), entry.getValue(), speciesNames)
        ));
        ImmutableList.of(
            entry("Number of FAD deployments", "Number of FAD deployments (%s)"),
            entry("Number of FAD sets", "Number of FAD sets (%s)"),
            entry("Number of unassociated sets", "Number of unassociated sets (%s)")
        ).forEach(entry -> chartBuilderFactories.add(
            ChartBuilderFactory.forPattern(
                entry.getKey(), entry.getValue(),
                TicTacToeRegionalDivision.REGION_NAMES
            )
        ));

        chartBuilderFactories.add(
            ChartBuilderFactory.forPattern(
                "Number of actions", "Number of %s",
                ImmutableList.of("FAD deployments", "FAD sets", "unassociated sets")
            )
        );

        jsonOutputManagerFactory.setChartBuilderFactories(chartBuilderFactories.build());

        jsonOutputManagerFactory.setHeatmapBuilderFactories(concat(
            zip(
                speciesNames.stream(),
                Stream.generate(() -> "green"),
                BiomassSnapshotHeatmapBuilderFactory::newInstance
            ),
            Stream.of(new AverageNumberOfActiveFadsHeatmapBuilderFactory())
        ).collect(toImmutableList()));

        final FishState model = new FishState();

        model.setScenario(scenario);

        scenario.getPlugins().add(jsonOutputManagerFactory);

        model.start();

        model.getYearlyDataSet().getColumns().forEach(col -> System.out.println(col.getName()));

        do {
            model.schedule.step(model);
            System.out.println(model.getDay());
        } while (model.getYear() < 2);

        writeAdditionalOutputsToFolder(outputPath, model);

    }

}
