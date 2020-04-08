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
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.FishState;
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
import static java.util.stream.Stream.concat;
import static org.jfree.chart.ChartColor.LIGHT_BLUE;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.writeAdditionalOutputsToFolder;

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
            new SinglePeriodEventBuilderFactory("Closure period B", 313, 364 + 19)
        ));

        final ImmutableList.Builder<ChartBuilderFactory> chartBuilderFactories = new ImmutableList.Builder<>();
        ImmutableMap.of(
            "Biomass (kg)", "Biomass %s",
            "Landings (kg)", "%s Landings"
        ).forEach((title, pattern) -> chartBuilderFactories.add(
            ChartBuilderFactory.forPattern(title, pattern, speciesNames)
        ));

        jsonOutputManagerFactory.setChartBuilderFactories(chartBuilderFactories.build());

        jsonOutputManagerFactory.setHeatmapBuilderFactories(concat(
            speciesNames.stream().map(BiomassSnapshotHeatmapBuilderFactory::newInstance),
            Stream.of(new AverageNumberOfActiveFadsHeatmapBuilderFactory())
        ).collect(toImmutableList()));

        scenario.getPlugins().add(jsonOutputManagerFactory);

        final FishState model = new FishState();

        model.setScenario(scenario);
        model.start();
        do {
            model.schedule.step(model);
            System.out.println(model.getDay());
        } while (model.getYear() < 4);

        writeAdditionalOutputsToFolder(outputPath, model);

    }

}
