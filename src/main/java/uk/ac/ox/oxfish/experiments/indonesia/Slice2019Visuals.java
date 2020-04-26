/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FullSeasonalRetiredDecoratorFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumDaysAYearFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.JsonOutputManagerFactory;
import uk.ac.ox.oxfish.model.data.webviz.JsonOutputPlugin;
import uk.ac.ox.oxfish.model.data.webviz.charts.ChartBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.colours.ColourSeries;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.BiomassSnapshotHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.EffortHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.HeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.vessels.VesselClassifier;
import uk.ac.ox.oxfish.model.plugins.StartingMPAFactory;
import uk.ac.ox.oxfish.model.regs.factory.DepthMPAFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import static java.awt.Color.GREEN;
import static uk.ac.ox.oxfish.model.data.webviz.charts.ChartBuilderFactory.PERCENTILE_TRANSFORMER;

public class Slice2019Visuals {

    private static final Path basePath =
        Paths.get(System.getProperty("user.home"), "workspace");
    private static final Path scenarioPath =
        basePath.resolve(Paths.get("tuna", "np", "runs", "webviz_test", "indonesia", "historical20_baranov_8h.yaml"));
    private static final Path outputPath =
        basePath.resolve(Paths.get("poseidon-webviz", "public", "testdata"));

    private static final int YEARS_TO_RUN = 11;

    public static void main(String[] args) throws IOException {

        runScenario(
            "No Take Zone (Example)",
            1,
            0L,
            scenario -> {
                FlexibleScenario flexible = (FlexibleScenario) scenario;
                StartingMPAFactory mpa = new StartingMPAFactory();
                mpa.getStartingMPAs().add(new StartingMPA(74, 50, 24, 20));
                flexible.getPlugins().add(mpa);
                for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {
                    fisherDefinition.setRegulation(new ProtectedAreasOnlyFactory());
                }
            },
            "No take zone: a sample no take zone is shown near the border of WPP 712 and 713 near Sumenep. " +
                "Fishing is restricted for all boats (5 GT and above) in this area. Boats do pass through this area "
        );

        runScenario(
            "No Management (baseline), No Fishery Exit",
            1,
            0L,
            scenario -> {
                FlexibleScenario flexible = (FlexibleScenario) scenario;
                for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {
                    //large boats already never quit
                    if (fisherDefinition.getDepartingStrategy() instanceof FullSeasonalRetiredDecoratorFactory)
                        ((FullSeasonalRetiredDecoratorFactory) fisherDefinition.getDepartingStrategy()).
                            setInertia(new FixedDoubleParameter(100));
                }
            },
            "Baseline scenario - no management. Vessels do not exit the fishery when " +
                "profitability declines and we see a long term decline in the stock."
        );

        runScenario(
            "No Management (baseline), No Fishery Exit",
            1,
            0L,
            null,
            "Baseline scenario: no management of fishing so we see stocks get depleted. " +
                "Unprofitable boats leave the fishery, so eventually stocks recover."
        );

        //150 days

        runScenario(
            "150 Fishing Days - All Boats",
            1,
            0L,
            Slice2019Sweeps.setupEffortControlConsumer(
                new String[]{"big", "small", "medium", "small10"},
                2,
                150
            ),
            "Season closure for all boats: boats are allowed 150 fishing days each year."
        );

        runScenario(
            "150 Fishing Days - Boats 10GT+",
            1,
            0L,
            Slice2019Sweeps.setupEffortControlConsumer(
                new String[]{"big", "medium", "small10"},
                2,
                150
            ),
            "Season closure for boats 10GT and above: these boats are allowed 150 fishing " +
                "days each year. Small boats (5-9 GT) have no management."
        );

        //100 days
        runScenario(
            "100 Fishing Days - All Boats",
            1,
            0L,
            Slice2019Sweeps.setupEffortControlConsumer(
                new String[]{"big", "small", "medium", "small10"},
                2,
                100
            ),
            "Season closure for all boats: boats are allowed 100 fishing days each year."
        );

        runScenario(
            "100 Fishing Days - Boats 10GT+",
            1,
            0L,
            Slice2019Sweeps.setupEffortControlConsumer(
                new String[]{"big", "medium", "small10"},
                2,
                100
            ),
            "Season closure for boats 10GT and above: these boats are allowed 100 fishing " +
                "days each year. Small boats (5-9 GT) have no management."
        );

        //premium

        runScenario(
            "Price Premium for Mature L. malabaricus",
            1,
            0L,
            Slice2019Sweeps.setupPremiumConsumer(
                10, "Lutjanus malabaricus", 2
            ),
            "Price Premium: Fishers (all boats 5GT and above) receive double the price " +
                "for any mature Lutjanus malabaricus they catch."
        );

        //delays
        runScenario(
            "10 Days Port Delay - All",
            1,
            0L,
            Slice2019Sweeps.setupDelaysConsumer(
                new String[]{"big", "small", "medium", "small10"},
                2,
                10
            ), "Each boat is forced to spend 10 days at port between each trip"
        );

        //fleet reduction
        runScenario(
            "8% Annual Reduction in Fishing Fleet",
            1,
            0L,
            Slice2019Sweeps.setupFleetReductionConsumer(
                2,
                .08
            ),
            "Fleet reduction: The fishing fleet (all boats 5GT and above) is reduced " +
                "by 8% each year. Vessels may also exit the fishery voluntarily due to lack of profitability."
        );

        runScenario(
            "Marine Protected Area - >75m",
            1,
            0L,
            scenario -> {
                FlexibleScenario flexible = (FlexibleScenario) scenario;
                for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {
                    //large boats already never quit
                    DepthMPAFactory regulation = new DepthMPAFactory();
                    regulation.setMaxDepth(new FixedDoubleParameter(10000));
                    regulation.setMinDepth(new FixedDoubleParameter(75));
                    fisherDefinition.setRegulation(regulation);
                }
            },
            "All areas whose depth is above 75m are closed to fishing. This protects somewhat " +
                "Pristipomoides multidens and Lutjanus erythropterus; in the short run boats fish more " +
                "Lutjanus malabaricus, in the long run they quit"

        );

        runScenario(
            "Marine Protected Area - <75m",
            1,
            0L,
            scenario -> {
                FlexibleScenario flexible = (FlexibleScenario) scenario;
                for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {
                    //large boats already never quit
                    DepthMPAFactory regulation = new DepthMPAFactory();
                    regulation.setMaxDepth(new FixedDoubleParameter(75));
                    regulation.setMinDepth(new FixedDoubleParameter(-10000000));
                    fisherDefinition.setRegulation(regulation);
                }
            },
            "All areas whose depth is below 75m are closed to fishing. This makes Lutjanus malabaricus " +
                "impossible to catch which results in the liquidation of the entire small fleet"
        );

    }

    @SuppressWarnings("SameParameterValue")
    private static void runScenario(
        String simulationTitle,
        int yearsToSkip,
        long seed,
        @Nullable Consumer<Scenario> modifier,
        final String modelDescription
    ) throws IOException {

        System.out.println("===\nRunning: " + simulationTitle + "\n---");

        FishYAML yaml = new FishYAML();
        FlexibleScenario scenario = yaml.loadAs(new FileReader(scenarioPath.toFile()), FlexibleScenario.class);

        scenario.getPlugins().add(newJsonOutputManagerFactory(simulationTitle, yearsToSkip, modelDescription));

        if (modifier != null) modifier.accept(scenario);
        scenario.getFisherDefinitions().forEach(fisherDefinition ->
            fisherDefinition.setFishingStrategy(
                new MaximumDaysAYearFactory(240, fisherDefinition.getFishingStrategy())
            )
        );
        final FishState model = new FishState(seed);
        model.setScenario(scenario);
        model.start();
        while (model.getYear() < YEARS_TO_RUN)
            model.schedule.step(model);
        model.schedule.step(model);
        JsonOutputPlugin.writeOutputsToFolder(model, outputPath);
    }

    @NotNull private static JsonOutputManagerFactory newJsonOutputManagerFactory(
        String simulationTitle,
        int yearsToSkip,
        String modelDescription
    ) {
        final VesselClassifier<String> vesselClassifier = new VesselClassifier<>(
            ImmutableMap.of(
                "population0", "5-9 GT",
                "population3", "10-14 GT",
                "population1", "15-30 GT",
                "population2", ">30 GT"
            ),
            fisher -> fisher.getTags().stream() // extract the vessel type from the Fisher's tags
                .filter(tags -> tags.contains("population"))
                .findFirst().orElseThrow(() -> new RuntimeException("Fisher population tag not set!")),
            new ColourSeries("#008dc4", "#d48500", "#b973a0", "#d32a37")
        );

        final JsonOutputManagerFactory jsonOutputManagerFactory = new JsonOutputManagerFactory();
        jsonOutputManagerFactory.setPrettyPrinting(true);
        jsonOutputManagerFactory.setNumYearsToSkip(yearsToSkip);
        jsonOutputManagerFactory.setStartDate("2018-01-01");
        jsonOutputManagerFactory.setScenarioTitle("Indonesia - " + simulationTitle);
        jsonOutputManagerFactory.setScenarioDescription(modelDescription);
        jsonOutputManagerFactory.getVesselsBuilderFactory().setVesselClassifier(vesselClassifier);

        List<String> speciesNames = ImmutableList.of(
            "Lutjanus malabaricus",
            "Epinephelus areolatus",
            "Lutjanus erythropterus",
            "Pristipomoides multidens"
        );

        jsonOutputManagerFactory.setHeatmapBuilderFactories(new ImmutableList.Builder<HeatmapBuilderFactory>()
            .addAll(BiomassSnapshotHeatmapBuilderFactory.forSpecies(speciesNames, GREEN, 10))
            .add(new EffortHeatmapBuilderFactory(10, "pink"))
            .build()
        );

        ImmutableList.Builder<ChartBuilderFactory> chartBuilderFactories = new ImmutableList.Builder<>();

        chartBuilderFactories.add(ChartBuilderFactory.fromColumnNamePattern(
            "Landings",
            "Landings (kg)",
            speciesNames,
            "%s Landings"
        ));

        chartBuilderFactories.add(ChartBuilderFactory.fromColumnNamePattern(
            "Biomass",
            "Biomass (kg)",
            speciesNames,
            "Biomass %s"
        ));

        final ChartBuilderFactory sprChartBuilderFactory = ChartBuilderFactory.fromColumnNamePattern(
            "SPR",
            "SPR (%)",
            speciesNames,
            "SPR Oracle - %s"
        );
        sprChartBuilderFactory.setYLines(ImmutableList.of(0.4));
        sprChartBuilderFactory.setValueTransformer(PERCENTILE_TRANSFORMER);
        chartBuilderFactories.add(sprChartBuilderFactory);

        for (String speciesName : speciesNames) {
            chartBuilderFactories.add(ChartBuilderFactory.fromVesselClassifier(
                speciesName + " landings per population",
                "Landings (kg)",
                vesselClassifier,
                population -> speciesName + " Landings of " + population
            ));
        }

        chartBuilderFactories.add(ChartBuilderFactory.fromVesselClassifier(
            "Active fishers",
            "Number of fishers",
            vesselClassifier,
            population -> "Number Of Active Fishers of " + population
        ));

        chartBuilderFactories.add(ChartBuilderFactory.fromVesselClassifier(
            "Average profits",
            "IDR",
            vesselClassifier,
            population -> "Average Cash-Flow of " + population
        ));

        final ChartBuilderFactory pctMatureCatchesChartBuilderFactory = ChartBuilderFactory.fromSeriesIdentifiers(
            "Percentage of mature catches",
            "Mature catches (%)",
            speciesNames,
            speciesName -> "Percentage Mature Catches " + speciesName + " 100_" + speciesName.split(" ")[1],
            speciesName -> speciesName
        );
        pctMatureCatchesChartBuilderFactory.setValueTransformer(PERCENTILE_TRANSFORMER);
        chartBuilderFactories.add(pctMatureCatchesChartBuilderFactory);

        jsonOutputManagerFactory.setChartBuilderFactories(chartBuilderFactories.build());

        return jsonOutputManagerFactory;
    }

}
