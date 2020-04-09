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
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonOutputManagerFactory;
import uk.ac.ox.oxfish.model.data.webviz.JsonOutputPlugin;
import uk.ac.ox.oxfish.model.data.webviz.charts.ChartBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.events.SinglePeriodEventDefinitionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.AverageNumberOfActiveFadsHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.BiomassSnapshotHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.FadDeploymentCountingHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.FadSetCountingHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.HeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.UnassociatedSetCountingHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static java.awt.Color.GREEN;
import static java.awt.Color.WHITE;
import static uk.ac.ox.oxfish.model.data.monitors.regions.TicTacToeRegionalDivision.REGION_NAMES;
import static uk.ac.ox.oxfish.model.data.webviz.vessels.VesselClassifier.singleTypeClassifier;

public final class WebVizTest {

    private static final Path basePath =
        Paths.get(System.getProperty("user.home"), "workspace");
    private static final Path scenarioPath =
        basePath.resolve(Paths.get("tuna", "np", "runs", "webviz_test", "tuna.yaml"));
    private static final Path outputPath =
        basePath.resolve(Paths.get("poseidon-webviz", "public", "testdata"));

    public static void main(final String[] args) throws IOException {
        final TunaScenario scenario = new FishYAML().loadAs(new FileReader(scenarioPath.toFile()), TunaScenario.class);
        final FishState model = new FishState();
        model.setScenario(scenario);
        scenario.getPlugins().add(makeJsonOutputManagerFactory());
        model.start();
        do {
            model.schedule.step(model);
            System.out.println(model.getDay());
        } while (model.getYear() < 2);
        JsonOutputPlugin.writeOutputsToFolder(model, outputPath);
    }

    @NotNull private static JsonOutputManagerFactory makeJsonOutputManagerFactory() {

        final Set<String> speciesNames = TunaScenario.speciesNames.values();

        final JsonOutputManagerFactory jsonOutputManagerFactory = new JsonOutputManagerFactory();
        jsonOutputManagerFactory.setScenarioTitle("Tuna - Baseline Scenario");
        jsonOutputManagerFactory.setScenarioDescription(
            "This is sample output from the current tuna simulation, " +
                "over a period of three years after one year of 'spin up'.");
        jsonOutputManagerFactory.setStartDate("2017-01-01");
        jsonOutputManagerFactory.setNumYearsToSkip(1);
        jsonOutputManagerFactory.setPrettyPrinting(true);

        jsonOutputManagerFactory.getVesselsBuilderFactory().setVesselClassifier(
            singleTypeClassifier("Class 6 vessels", WHITE)
        );

        jsonOutputManagerFactory.setEventBuilderFactories(ImmutableList.of(
            new SinglePeriodEventDefinitionBuilderFactory("Closure period A", 210, 281),
            new SinglePeriodEventDefinitionBuilderFactory("El Corralito closure", 282, 312),
            new SinglePeriodEventDefinitionBuilderFactory("Closure period B", 313, 364 + 19),

            new SinglePeriodEventDefinitionBuilderFactory("Closure period A", (364) + 210, (364) + 281),
            new SinglePeriodEventDefinitionBuilderFactory("El Corralito closure", (364) + 282, (364) + 312),
            new SinglePeriodEventDefinitionBuilderFactory("Closure period B", (364) + 313, (364) + 364 + 19),

            new SinglePeriodEventDefinitionBuilderFactory("Closure period A", (364 * 2) + 210, (364 * 2) + 281),
            new SinglePeriodEventDefinitionBuilderFactory("El Corralito closure", (364 * 2) + 282, (364 * 2) + 312),
            new SinglePeriodEventDefinitionBuilderFactory("Closure period B", (364 * 2) + 313, (364 * 2) + 364 + 19)

        ));

        jsonOutputManagerFactory.setChartBuilderFactories(ImmutableList.of(
            ChartBuilderFactory.fromValues(
                "Biomass per species",
                "Biomass (kg)",
                speciesNames,
                "Biomass %s"
            ),
            ChartBuilderFactory.fromValues(
                "Landings per species",
                "Landings (kg)",
                speciesNames,
                "%s Landings"
            ),
            ChartBuilderFactory.fromValues(
                "Recruitment per species",
                "Recruitment (kg)",
                speciesNames,
                "%s Recruitment"
            ),
            ChartBuilderFactory.fromValues(
                "Average catch per set per species",
                "Average catch (kg)",
                speciesNames,
                "Average %s catches by set"
            ),
            ChartBuilderFactory.fromValues(
                "Catch from FAD sets per species",
                "Catch (kg)",
                speciesNames,
                "Sum of %s catches from FAD sets"
            ),
            ChartBuilderFactory.fromValues(
                "Catch from unassociated sets per species",
                "Catch (kg)",
                speciesNames,
                "Sum of %s catches from unassociated sets"
            ),
            ChartBuilderFactory.fromValues(
                "Catch from unassociated sets (kg)",
                "Catch (kg)",
                speciesNames,
                "Sum of %s catches from unassociated sets"
            ),
            ChartBuilderFactory.fromValues(
                "Biomass under FADs per species",
                "Biomass (kg)",
                speciesNames,
                "Sum of %s biomass under FADs"
            ),
            ChartBuilderFactory.fromValues(
                "FAD deployments per region",
                "Number of FAD deployments",
                REGION_NAMES,
                "Number of FAD deployments (%s)"
            ),
            ChartBuilderFactory.fromValues(
                "FAD sets per region",
                "Number of FAD sets",
                REGION_NAMES,
                "Number of FAD sets (%s)"
            ),
            ChartBuilderFactory.fromValues(
                "Unassociated sets per region",
                "Number of unassociated sets",
                REGION_NAMES,
                "Number of unassociated sets (%s)"
            ),
            ChartBuilderFactory.fromValues(
                "Number of actions per action type",
                "Number of actions",
                ImmutableList.of("FAD deployments", "FAD sets", "unassociated sets"),
                "Number of %s"
            )
        ));

        jsonOutputManagerFactory.setHeatmapBuilderFactories(
            new ImmutableList.Builder<HeatmapBuilderFactory>()
                .addAll(BiomassSnapshotHeatmapBuilderFactory.forSpecies(speciesNames, GREEN, 30))
                .add(new AverageNumberOfActiveFadsHeatmapBuilderFactory())
                .add(new FadDeploymentCountingHeatmapBuilderFactory())
                .add(new FadSetCountingHeatmapBuilderFactory())
                .add(new UnassociatedSetCountingHeatmapBuilderFactory())
                .build()
        );
        return jsonOutputManagerFactory;
    }

}
