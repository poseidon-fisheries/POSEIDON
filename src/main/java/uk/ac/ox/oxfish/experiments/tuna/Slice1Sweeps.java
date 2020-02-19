package uk.ac.ox.oxfish.experiments.tuna;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimits;
import uk.ac.ox.oxfish.model.regs.fads.GeneralSetLimitsFactory;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Streams.stream;
import static java.util.stream.Stream.concat;

@SuppressWarnings("UnstableApiUsage")
public class Slice1Sweeps {
    private static final Path basePath = Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");
    private static final Path scenarioPath = basePath.resolve(Paths.get("calibrations", "2019-12-13_2-all_targets"));

    private static final Path outputPath = basePath.resolve(Paths.get("runs", "slice1_2020-02-18_policy_shock"));
    private static final int numberOfRunsPerPolicy = 2;
    private static final int yearsToRun = 5;

    public static void main(String[] args) throws IOException {
        final ArrayList<String> columnsToPrint = newArrayList(
            "Biomass Bigeye tuna",
            "Biomass Skipjack tuna",
            "Biomass Yellowfin tuna",
            "Total Earnings",
            "Total Variable Costs",
            "Bigeye tuna Landings",
            "Skipjack tuna Landings",
            "Yellowfin tuna Landings",
            "Bigeye tuna Catches (kg)",
            "Skipjack tuna Catches (kg)",
            "Yellowfin tuna Catches (kg)",
            "Bigeye tuna catches from FAD sets",
            "Bigeye tuna catches from unassociated sets",
            "Bigeye tuna biomass lost (kg)",
            "Skipjack tuna catches from FAD sets",
            "Skipjack tuna catches from unassociated sets",
            "Skipjack tuna biomass lost (kg)",
            "Yellowfin tuna catches from FAD sets",
            "Yellowfin tuna catches from unassociated sets",
            "Yellowfin tuna biomass lost (kg)",
            "Average Trip Duration",
            "Total number of FAD deployments",
            "Total number of FAD sets",
            "Total number of unassociated sets",
            "Proportion of FAD deployments (Central region)",
            "Proportion of FAD deployments (East region)",
            "Proportion of FAD deployments (North region)",
            "Proportion of FAD deployments (Northeast region)",
            "Proportion of FAD deployments (Northwest region)",
            "Proportion of FAD deployments (South region)",
            "Proportion of FAD deployments (Southeast region)",
            "Proportion of FAD deployments (Southwest region)",
            "Proportion of FAD deployments (West region)",
            "Proportion of FAD sets (Central region)",
            "Proportion of FAD sets (East region)",
            "Proportion of FAD sets (North region)",
            "Proportion of FAD sets (Northeast region)",
            "Proportion of FAD sets (Northwest region)",
            "Proportion of FAD sets (South region)",
            "Proportion of FAD sets (Southeast region)",
            "Proportion of FAD sets (Southwest region)",
            "Proportion of FAD sets (West region)",
            "Proportion of unassociated sets (Central region)",
            "Proportion of unassociated sets (East region)",
            "Proportion of unassociated sets (North region)",
            "Proportion of unassociated sets (Northeast region)",
            "Proportion of unassociated sets (Northwest region)",
            "Proportion of unassociated sets (South region)",
            "Proportion of unassociated sets (Southeast region)",
            "Proportion of unassociated sets (Southwest region)",
            "Proportion of unassociated sets (West region)",
            "Exogenous catches of Bigeye tuna",
            "Exogenous catches of Skipjack tuna",
            "Exogenous catches of Yellowfin tuna",
            "Total Bigeye tuna biomass under FADs",
            "Total Skipjack tuna biomass under FADs",
            "Total Yellowfin tuna biomass under FADs"
        );

        final BatchRunner batchRunner = new BatchRunner(
            scenarioPath.resolve("tuna_calibrated.yaml"),
            yearsToRun,
            columnsToPrint,
            outputPath,
            null,
            System.currentTimeMillis(),
            -1
        );

        final ActiveFadLimits currentFadLimits = new ActiveFadLimits(ImmutableSortedMap.of(
            0, 70,
            213, 120,
            426, 300,
            1200, 450
        ));

        final ActiveFadLimits smallerFadLimits = new ActiveFadLimits(ImmutableSortedMap.of(
            0, 20,
            213, 30,
            426, 75,
            1200, 115
        ));

        final ImmutableMap<AlgorithmFactory<? extends ActionSpecificRegulation>, String> fadLimits = ImmutableMap.of(
            __ -> currentFadLimits, "Current FAD limits",
            __ -> smallerFadLimits, "Strict FAD limits"
        );

        final ImmutableMap<Optional<AlgorithmFactory<? extends ActionSpecificRegulation>>, String> setLimits = concat(
            Stream.of(25, 50, 75).map(Optional::of),
            Stream.of(Optional.<Integer>empty())
        ).collect(toImmutableMap(
            opt -> opt.map(limit -> new GeneralSetLimitsFactory(ImmutableSortedMap.of(0, limit))),
            opt -> opt.map(limit -> limit + " sets limit").orElse("No set limit")
        ));

        FileWriter fileWriter = new FileWriter(outputPath.resolve("results.csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        final ImmutableList<AlgorithmFactory<? extends ActionSpecificRegulation>> businessAsUsual =
            ImmutableList.of(__ -> currentFadLimits);

        fadLimits.forEach((activeFadLimits, fadLimitsName) ->
            setLimits.forEach((generalSetLimits, setLimitsName) -> {
                setupRunner(
                    batchRunner,
                    businessAsUsual,
                    concat(Stream.of(activeFadLimits), stream(generalSetLimits)).collect(toImmutableList()),
                    fadLimitsName + " / " + setLimitsName
                );
                for (int i = 0; i < numberOfRunsPerPolicy; i++) {
                    System.out.println("Run " + i);
                    StringBuffer outputBuffer = new StringBuffer();
                    try {
                        batchRunner.run(outputBuffer);
                        fileWriter.write(outputBuffer.toString());
                        fileWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            })
        );
        fileWriter.close();
    }

    private static void setupRunner(
        BatchRunner batchRunner,
        ImmutableList<AlgorithmFactory<? extends ActionSpecificRegulation>> businessAsUsual,
        ImmutableList<AlgorithmFactory<? extends ActionSpecificRegulation>> policyRegulations,
        String policyName
    ) {
        Steppable setRegulations = simState -> {
            System.out.println("Changing regulations to " + policyName + " for all fishers at day " + simState.schedule.getSteps());
            final FishState fishState = (FishState) simState;
            fishState.getFishers().forEach(fisher ->
                ((PurseSeineGear) fisher.getGear()).getFadManager().setActionSpecificRegulations(
                    policyRegulations.stream().map(factory -> factory.apply(fishState))
                )
            );
        };
        batchRunner.setScenarioSetup(scenario -> {
            final TunaScenario tunaScenario = (TunaScenario) scenario;
            PurseSeineGearFactory purseSeineGearFactory = (PurseSeineGearFactory) tunaScenario.getFisherDefinition().getGear();
            purseSeineGearFactory.setActionSpecificRegulations(businessAsUsual);
            tunaScenario.getPlugins().add(fishState ->
                __ -> fishState.scheduleOnceAtTheBeginningOfYear(setRegulations, StepOrder.DAWN, 1)
            );
        });
        batchRunner.setColumnModifier((writer, model, year) ->
            writer.append(policyName).append(",")
        );
    }
}
