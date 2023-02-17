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

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.BiomassPurseSeineGearFactory;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.model.regs.fads.SetLimitsFactory;
import uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario;
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

    private static final Path exoCatchesPath =
        Paths.get("inputs", "epo_inputs", "biomass", "exogenous_catches.csv");
    private static final Path basePath =
        Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");
    private static final Path scenarioPath =
        basePath.resolve(Paths.get("calibrations", "2019-12-13_2-all_targets"));
    private static final Path outputPath =
        basePath.resolve(Paths.get("runs", "slice1_2020-01-31_normal_exo"));
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

        final ActiveFadLimitsFactory currentFadLimits = new ActiveFadLimitsFactory();

        final ImmutableMap<ActiveFadLimitsFactory, String> fadLimits = ImmutableMap.of(
            currentFadLimits, "Current FAD limits"
        );

        final ImmutableMap<Optional<SetLimitsFactory>, String> setLimits = ImmutableMap.of(
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
                    concat(
                        Stream.of(activeFadLimitsFactory),
                        stream(generalSetLimitsFactory)
                    ).collect(toList()),
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
            final EpoBiomassScenario epoBiomassScenario = (EpoBiomassScenario) scenario;
            final AlgorithmFactory<? extends Gear> gearFactory =
                epoBiomassScenario.getPurseSeineGearFactory();
            ((BiomassPurseSeineGearFactory) gearFactory).setActionSpecificRegulations(
                regulationFactories);
            epoBiomassScenario.getExogenousCatchesFactory().setCatchesFile(exoCatchesPath);
        });
        batchRunner.setColumnModifier((writer, model, year) ->
            writer.append(policyName).append(",")
        );
    }
}
