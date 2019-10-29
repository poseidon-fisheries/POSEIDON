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

package uk.ac.ox.oxfish.experiments.allocatorexperiment;

import com.google.common.collect.Lists;
import joptsimple.internal.Strings;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.MaxHoldSizeRandomAllocationPolicyFactory;
import uk.ac.ox.oxfish.model.regs.factory.OnOffSwitchAllocatorFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Consumer;

public class AllocatorSlice0 {


    private static final int YEARS_TO_RUN = 15;
    private static final String SCENARIO_NAME = "base";
    private static final Path MAIN_DIRECTORY = Paths.get("docs","20191004 allocator");
    public static final int RUNS_PER_POLICY = 10;
    private static final String OUTPUT_FOLDER = "slice0";



    public static void main(String[] args) throws IOException {


        maxHoldSizeExperiment("all",
                              new String[]{"population0","population1"},
                              SCENARIO_NAME,
                              15000,
                              1000, MAIN_DIRECTORY, MAIN_DIRECTORY.resolve(OUTPUT_FOLDER), "Species 0", RUNS_PER_POLICY
        );

        maxHoldSizeExperiment("small",
                              new String[]{"population0"},
                              SCENARIO_NAME,
                              15000,
                              1000, MAIN_DIRECTORY, MAIN_DIRECTORY.resolve(OUTPUT_FOLDER), "Species 0", RUNS_PER_POLICY
        );

        maxHoldSizeExperiment("large",
                              new String[]{"population1"},
                              SCENARIO_NAME,
                              15000,
                              1000, MAIN_DIRECTORY, MAIN_DIRECTORY.resolve(OUTPUT_FOLDER), "Species 0", RUNS_PER_POLICY
        );

    }





    public static void maxHoldSizeExperiment(
            String name,
            String[] modifiedTags,
            final String scenarioFileName,
            final double maxHoldSize, // 15000
            final double stepSize,
            final Path inputDirectory,
            final Path outputDirectory, final String speciesName, final int runsPerPolicy) throws IOException {

        FileWriter fileWriter = new FileWriter(outputDirectory.resolve(
                                                         scenarioFileName + "_"+name+".csv").toFile());

        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        double currentHoldSize = maxHoldSize;
        while (currentHoldSize>0)
        {

            BatchRunner runner = setupRunner(scenarioFileName,
                                             YEARS_TO_RUN, 2,
                                             inputDirectory,
                                             outputDirectory, speciesName);

            int finalHoldSize = (int) currentHoldSize;

            runner.setScenarioSetup(
                    new Consumer<Scenario>() {
                        @Override
                        public void accept(Scenario scenario) {
                            FlexibleScenario flexible = (FlexibleScenario) scenario;
                            OnOffSwitchAllocatorFactory regulator = new OnOffSwitchAllocatorFactory();

                            regulator.setTagsOfParticipants(Strings.join(modifiedTags,","));


                            MaxHoldSizeRandomAllocationPolicyFactory policy =
                                    new MaxHoldSizeRandomAllocationPolicyFactory();
                            regulator.setPermitPolicy(policy);
                            policy.setYearlyHoldSizeLimit(new FixedDoubleParameter(finalHoldSize));
                            flexible.getPlugins().add(
                                    regulator
                            );
                        }
                    }
            );

            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalHoldSize).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< runsPerPolicy; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }


            currentHoldSize-=stepSize;
            currentHoldSize = Math.round(currentHoldSize);
        }





    }


    @NotNull
    private static BatchRunner setupRunner(
            String filename, final int yearsToRun,
            final int populations, final Path inputDirectory, final Path outputDirectory, final String speciesName) {
        ArrayList<String> columnsToPrint = Lists.newArrayList(

                "Actual Average Cash-Flow",
                speciesName + " Landings",
                speciesName + " Earnings",
                "Average Distance From Port",
                "Actual Average Hours Out",
                "Number Of Active Fishers",
                "Biomass " + speciesName);
        for (int i = 0; i < populations; i++) {
            columnsToPrint.add("Total Landings of population"+i);
            columnsToPrint.add("Actual Average Cash-Flow of population"+i);
            columnsToPrint.add("Number Of Active Fishers of population"+i);

        }

        return new BatchRunner(
                inputDirectory.resolve(
                          filename + ".yaml"),
                yearsToRun,
                columnsToPrint,
                outputDirectory.resolve(filename),
                null,
                System.currentTimeMillis(),
                -1
        );
    }

}
