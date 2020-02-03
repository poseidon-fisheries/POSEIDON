package uk.ac.ox.oxfish.experiments.tuna;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.model.regs.fads.GeneralSetLimitsFactory;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

@SuppressWarnings("UnstableApiUsage")
public class Slice1SweepsWithAltExoCatches {
    private static final Path exoCatchesPath = TunaScenario.input("exogenous_catches.csv");
    private static final Path basePath = Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");
    private static final Path scenarioPath = basePath.resolve(Paths.get("calibrations", "2019-12-13_2-all_targets"));
    private static final Path outputPath = basePath.resolve(Paths.get("runs", "slice1_2020-01-31_normal_exo"));
    private static final int numberOfRunsPerPolicy = 1;
    private static final int yearsToRun = 11;

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
            "Bigeye tuna catches from FAD sets",
            "Bigeye tuna catches from unassociated sets",
            "Skipjack tuna catches from FAD sets",
            "Skipjack tuna catches from unassociated sets",
            "Yellowfin tuna catches from FAD sets",
            "Yellowfin tuna catches from unassociated sets",
            "Average Trip Duration",
            "Total number of FAD deployments",
            "Total number of FAD sets",
            "Total number of unassociated sets",
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

        final ActiveFadLimitsFactory currentFadLimits =
            new ActiveFadLimitsFactory(ImmutableSortedMap.of(
                0, 70,
                213, 120,
                426, 300,
                1200, 450
            ));

        final ImmutableMap<ActiveFadLimitsFactory, String> fadLimits = ImmutableMap.of(
            currentFadLimits, "Current FAD limits"
        );

        final ImmutableMap<Optional<GeneralSetLimitsFactory>, String> setLimits = ImmutableMap.of(
            Optional.empty(), "No set limit"
        );

        FileWriter fileWriter = new FileWriter(outputPath.resolve("results.csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        fadLimits.forEach((activeFadLimitsFactory, fadLimitsName) ->
            setLimits.forEach((generalSetLimitsFactory, setLimitsName) -> {
                final String policyName = fadLimitsName + " / " + setLimitsName;
                System.out.println(policyName);
                setupRunner(
                    batchRunner,
                    concat(Stream.of(activeFadLimitsFactory), stream(generalSetLimitsFactory)).collect(toList()),
                    policyName
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
        List<AlgorithmFactory<? extends ActionSpecificRegulation>> regulationFactories,
        String policyName
    ) {
        batchRunner.setScenarioSetup(scenario -> {
            final TunaScenario tunaScenario = (TunaScenario) scenario;
            final AlgorithmFactory<? extends Gear> gearFactory = tunaScenario.getFisherDefinition().getGear();
            ((PurseSeineGearFactory) gearFactory).setActionSpecificRegulations(regulationFactories);
            tunaScenario.getExogenousCatchesFactory().setCatchesFile(exoCatchesPath);
        });
        batchRunner.setColumnModifier((writer, model, year) ->
            writer.append(policyName).append(",")
        );
    }
}
