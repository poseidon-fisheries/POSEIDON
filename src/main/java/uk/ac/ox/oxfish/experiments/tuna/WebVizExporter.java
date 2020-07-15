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
import org.jetbrains.annotations.NotNull;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.webviz.JsonOutputManagerFactory;
import uk.ac.ox.oxfish.model.data.webviz.JsonOutputPlugin;
import uk.ac.ox.oxfish.model.data.webviz.charts.ChartBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.colours.ColourSeries;
import uk.ac.ox.oxfish.model.data.webviz.events.SinglePeriodEventDefinitionBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.AverageNumberOfActiveFadsHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.BiomassSnapshotHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.FadSetCountingHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.HeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.heatmaps.UnassociatedSetCountingHeatmapBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.regions.SpecificRegionsBuilderFactory;
import uk.ac.ox.oxfish.model.data.webviz.vessels.VesselClassifier;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.MultipleRegulationsFactory;
import uk.ac.ox.oxfish.model.regs.factory.NoFishingFactory;
import uk.ac.ox.oxfish.model.regs.factory.TemporaryRegulationFactory;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.model.regs.fads.SetLimitsFactory;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.awt.Color.CYAN;
import static java.awt.Color.GREEN;
import static java.awt.Color.ORANGE;
import static java.awt.Color.RED;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JULY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.model.data.webviz.charts.ChartBuilderFactory.KG_TO_T_TRANSFORMER;
import static uk.ac.ox.oxfish.model.data.webviz.vessels.VesselClassifier.singleTypeClassifier;
import static uk.ac.ox.oxfish.model.regs.MultipleRegulations.TAG_FOR_ALL;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.zipToMap;

public final class WebVizExporter {

    private static final int NUM_YEARS_TO_SKIP = 1;
    private static final int NUM_YEARS_TO_RUN = 5;
    private static final int POLICY_KICK_IN_YEAR = 3;

    private final Path basePath =
        Paths.get(System.getProperty("user.home"), "workspace");
    private final Path scenarioPath =
        basePath.resolve(Paths.get("tuna", "np", "runs", "webviz_test", "tuna.yaml"));
    private final Path jsonOutputPath =
        basePath.resolve(Paths.get("poseidon-webviz", "public", "testdata"));
    private final Path csvOutputPath =
        basePath.resolve(Paths.get("tuna", "np", "runs", "webviz"));

    private final AlgorithmFactory<? extends ActionSpecificRegulation> currentFadLimits =
        new ActiveFadLimitsFactory();

    private final AlgorithmFactory<? extends ActionSpecificRegulation> smallerFadLimits =
        new ActiveFadLimitsFactory(0, 0, 75, 115);

    private final ImmutableList<Policy<TunaScenario>> policies = ImmutableList.of(
        makePolicy(
            "Tuna - Business as usual",
            "Current IATTC regulations: limits on active FAD (300 FADs for class 6A vessels and 450 for class 6B), El Corralito and seasonal closures.",
            ImmutableList.of(currentFadLimits),
            null
        ),
        makePolicy(
            "Tuna - No El Corralito closure",
            "When removing El Corralito closure, results do not differ significantly from business as usual scenario.",
            ImmutableList.of(currentFadLimits),
            scenario -> new MultipleRegulationsFactory(ImmutableMap.of(
                scenario.galapagosEezReg, TAG_FOR_ALL,
                scenario.closureAReg, "closure A",
                scenario.closureBReg, "closure B"
            ))
        ),
        makePolicy(
            "Tuna - Stricter limits on active FADs",
            "Stricter FAD limits (115 FADs for class 6A vessels and 75 for class 6B) have limited impact.",
            ImmutableList.of(smallerFadLimits),
            null
        ),
        makePolicy(
            "Tuna - Limits on sets",
            "A limit of 50 sets per year per vessel has significant impact on the fishery.",
            ImmutableList.of(currentFadLimits, new SetLimitsFactory(50)),
            null
        ),
        makePolicy(
            "Tuna - Longer seasonal closures",
            "Length of closure periods A and B increased by 40% (100 days instead of 72).",
            ImmutableList.of(currentFadLimits),
            scenario -> new MultipleRegulationsFactory(ImmutableMap.of(
                scenario.galapagosEezReg, TAG_FOR_ALL,
                scenario.elCorralitoReg, TAG_FOR_ALL,
                new TemporaryRegulationFactory(
                    scenario.dayOfYear(JULY, 1),
                    scenario.dayOfYear(OCTOBER, 8),
                    new NoFishingFactory()
                ), "closure A",
                new TemporaryRegulationFactory(
                    scenario.dayOfYear(NOVEMBER, 9),
                    scenario.dayOfYear(FEBRUARY, 16),
                    new NoFishingFactory()
                ), "closure B"
            ))
        )
    );

    public static void main(final String[] args) {
        new WebVizExporter().makeRunner().run(NUM_YEARS_TO_RUN);
    }

    private Runner<TunaScenario> makeRunner() {
        return new Runner<>(TunaScenario.class, scenarioPath, csvOutputPath)
            .setPolicies(policies)
            .requestYearlyData()
            .setAfterStartConsumer(runnerState ->
                runnerState.getModel().registerStartable(
                    makeJsonOutputManagerFactory(runnerState).apply(runnerState.getModel())
                )
            )
            .setAfterRunConsumer(runnerState ->
                JsonOutputPlugin.writeOutputsToFolder(runnerState.getModel(), jsonOutputPath)
            );
    }

    @NotNull
    private JsonOutputManagerFactory makeJsonOutputManagerFactory(Runner<TunaScenario>.State runnerState) {

        final Set<String> speciesNames = TunaScenario.speciesNames.values();

        final JsonOutputManagerFactory jsonOutputManagerFactory = new JsonOutputManagerFactory();
        jsonOutputManagerFactory.setScenarioTitle(runnerState.getPolicy().getName());
        jsonOutputManagerFactory.setScenarioDescription(runnerState.getPolicy().getDescription());
        jsonOutputManagerFactory.setStartDate("2017-01-01");
        jsonOutputManagerFactory.setNumYearsToSkip(NUM_YEARS_TO_SKIP);
        jsonOutputManagerFactory.setPrettyPrinting(true);
        jsonOutputManagerFactory.setRegionsBuilderFactory(new SpecificRegionsBuilderFactory());
        final VesselClassifier<Integer> vesselClassifier = singleTypeClassifier("Class 6 vessels", CYAN);
        jsonOutputManagerFactory.getVesselsBuilderFactory().setVesselClassifier(vesselClassifier);

        // This block of code makes me feel dirty...
        switch (runnerState.getPolicy().getName()) {
            case "Tuna - No El Corralito closure":
                jsonOutputManagerFactory.setEventBuilderFactories(
                    range(0, NUM_YEARS_TO_RUN).boxed().flatMap(i -> Stream.of(
                        new SinglePeriodEventDefinitionBuilderFactory(
                            "Closure period A",
                            (365 * i) + 209,
                            (365 * i) + 280
                        ),
                        new SinglePeriodEventDefinitionBuilderFactory(
                            "Closure period B",
                            (365 * i) + 312,
                            (365 * (i + 1)) + 18
                        )
                    )).collect(toList())
                );
                break;
            case "Tuna - Longer seasonal closures":
                jsonOutputManagerFactory.setEventBuilderFactories(
                    range(0, NUM_YEARS_TO_RUN).boxed().flatMap(i -> Stream.of(
                        new SinglePeriodEventDefinitionBuilderFactory(
                            "Closure period A",
                            (365 * i) + (209 - 28),
                            (365 * i) + 280
                        ),
                        new SinglePeriodEventDefinitionBuilderFactory(
                            "El Corralito closure",
                            (365 * i) + 281,
                            (365 * i) + 311
                        ),
                        new SinglePeriodEventDefinitionBuilderFactory(
                            "Closure period B",
                            (365 * i) + 312,
                            (365 * (i + 1)) + 18 + 28
                        )
                    )).collect(toList())
                );
                break;
            case "Tuna - Limits on sets":
                jsonOutputManagerFactory.setEventBuilderFactories(
                    Stream.concat(
                        range(0, NUM_YEARS_TO_RUN).boxed().flatMap(i -> Stream.of(
                            new SinglePeriodEventDefinitionBuilderFactory(
                                "Closure period A",
                                (365 * i) + 209,
                                (365 * i) + 280
                            ),
                            new SinglePeriodEventDefinitionBuilderFactory(
                                "El Corralito closure",
                                (365 * i) + 281,
                                (365 * i) + 311
                            ),
                            new SinglePeriodEventDefinitionBuilderFactory(
                                "Closure period B",
                                (365 * i) + 312,
                                (365 * (i + 1)) + 18
                            )
                            )
                        ),
                        // I don't think this works because of overlapping events, but I'm leaving it there for now
                        Stream.of(new SinglePeriodEventDefinitionBuilderFactory(
                                "Set limit activated",
                                (365 * POLICY_KICK_IN_YEAR),
                                (365 * POLICY_KICK_IN_YEAR) + 1
                            )
                        )
                    ).collect(toList())
                );
                break;
            default:
                jsonOutputManagerFactory.setEventBuilderFactories(
                    range(0, NUM_YEARS_TO_RUN).boxed().flatMap(i -> Stream.of(
                        new SinglePeriodEventDefinitionBuilderFactory(
                            "Closure period A",
                            (365 * i) + 209,
                            (365 * i) + 280
                        ),
                        new SinglePeriodEventDefinitionBuilderFactory(
                            "El Corralito closure",
                            (365 * i) + 281,
                            (365 * i) + 311
                        ),
                        new SinglePeriodEventDefinitionBuilderFactory(
                            "Closure period B",
                            (365 * i) + 312,
                            (365 * (i + 1)) + 18
                        )
                    )).collect(toList())
                );
                break;
        }

        final ColourSeries speciesColours = new ColourSeries(GREEN.darker(), RED, ORANGE.darker());

        jsonOutputManagerFactory.setChartBuilderFactories(ImmutableList.of(
            ChartBuilderFactory.fromColumnNamePattern(
                "Biomass per species",
                "Biomass (t)",
                speciesNames,
                "Biomass %s"
            ).setValueTransformer(KG_TO_T_TRANSFORMER).setSeriesColours(speciesColours),
            ChartBuilderFactory.fromColumnNamePattern(
                "Landings per species",
                "Landings (t)",
                speciesNames,
                "%s Landings"
            ).setValueTransformer(KG_TO_T_TRANSFORMER).setSeriesColours(speciesColours),
// https://github.com/poseidon-fisheries/tuna/issues/129#issuecomment-641950109
//            ChartBuilderFactory.fromColumnNamePattern(
//                "Recruitment per species",
//                "Recruitment (t)",
//                speciesNames,
//                "%s Recruitment"
//            ).setValueTransformer(KG_TO_T_TRANSFORMER).setSeriesColours(speciesColours),
            ChartBuilderFactory.fromColumnNamePattern(
                "Catch from FAD sets per species",
                "Catch (t)",
                speciesNames,
                "Sum of %s catches from FAD sets"
            ).setValueTransformer(KG_TO_T_TRANSFORMER).setSeriesColours(speciesColours),
            ChartBuilderFactory.fromColumnNamePattern(
                "Catch from unassociated sets per species",
                "Catch (t)",
                speciesNames,
                "Sum of %s catches from unassociated sets"
            ).setValueTransformer(KG_TO_T_TRANSFORMER).setSeriesColours(speciesColours),
            ChartBuilderFactory.fromColumnNamePattern(
                "Biomass under FADs per species",
                "Biomass (t)",
                speciesNames,
                "Sum of %s biomass under FADs"
            ).setValueTransformer(KG_TO_T_TRANSFORMER).setSeriesColours(speciesColours),
// https://github.com/poseidon-fisheries/tuna/issues/129#issuecomment-641950109
//            ChartBuilderFactory.fromColumnNamePattern(
//                "FAD deployments per region",
//                "Number of FAD deployments",
//                REGION_NAMES,
//                "Number of FAD deployments (%s)"
//            ),
//            ChartBuilderFactory.fromColumnNamePattern(
//                "FAD sets per region",
//                "Number of FAD sets",
//                REGION_NAMES,
//                "Number of FAD sets (%s)"
//            ),
//            ChartBuilderFactory.fromColumnNamePattern(
//                "Unassociated sets per region",
//                "Number of unassociated sets",
//                REGION_NAMES,
//                "Number of unassociated sets (%s)"
//            ),
            ChartBuilderFactory.fromColumnNamePattern(
                "Number of actions per action type",
                "Number of actions",
                ImmutableList.of("FAD deployments", "FAD sets", "unassociated sets"),
                "Number of %s"
            ),
            ChartBuilderFactory.fromColumnName(
                "Total profits",
                "USD",
                "Total profits"
            )
        ));

        jsonOutputManagerFactory.setHeatmapBuilderFactories(
            new ImmutableList.Builder<HeatmapBuilderFactory>()
                .addAll(BiomassSnapshotHeatmapBuilderFactory.forSpecies(
                    zipToMap(speciesNames, speciesColours.getJavaColors()),
                    30
                ))
                .add(new AverageNumberOfActiveFadsHeatmapBuilderFactory())
// https://github.com/poseidon-fisheries/tuna/issues/129#issuecomment-641950109
//                .add(new FadDeploymentCountingHeatmapBuilderFactory())
                .add(new FadSetCountingHeatmapBuilderFactory())
                .add(new UnassociatedSetCountingHeatmapBuilderFactory())
                .build()
        );
        return jsonOutputManagerFactory;
    }

    private Policy<TunaScenario> makePolicy(
        String policyName,
        String policyDescription,
        Collection<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulationFactories,
        Function<TunaScenario, AlgorithmFactory<? extends Regulation>> makeGeneralRegulationFactory
    ) {
        Consumer<TunaScenario> scenarioConsumer = scenario -> {
            final Optional<AlgorithmFactory<? extends Regulation>> generalRegulationFactory =
                Optional.ofNullable(makeGeneralRegulationFactory).map(factory -> factory.apply(scenario));
            Steppable setRegulations = simState -> {
                final FishState fishState = (FishState) simState;
                System.out.println("Setting regulations to " + policyName + " for all fishers at day " + simState.schedule
                    .getSteps());
                fishState.getFishers().forEach(fisher -> {
                    generalRegulationFactory.map(factory -> factory.apply(fishState)).ifPresent(fisher::setRegulation);
                    ((PurseSeineGear) fisher.getGear()).getFadManager().setActionSpecificRegulations(
                        actionSpecificRegulationFactories.stream().map(factory -> factory.apply(fishState))
                    );
                });
            };
            scenario.getPlugins().add(__ -> fishState ->
                fishState.scheduleOnceAtTheBeginningOfYear(setRegulations, StepOrder.AFTER_DATA, POLICY_KICK_IN_YEAR)
            );
        };
        return new Policy<>(policyName, policyDescription, scenarioConsumer);
    }

}
