package uk.ac.ox.oxfish.utility;

import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.experiments.NoDataTachiuoSlice1;
import uk.ac.ox.oxfish.experiments.indonesia.limited.NoData718Slice3;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.FullSeasonalRetiredDataCollectorsFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RejectionSampling {
    /**
     * given a baseline scenario and a yaml containing a big list of parameters we can randomize. Run th e model
     * "many" times, collecting all the variables in "listOfColumnsToPrint" in a big summary file.
     * All this takes place in the "main directory"
     * @throws IOException
     */
    public static void runSlice(
            Path baselineScenarioFile,
            Path parameterFile,
            Path listOfColumnsToPrintFile,
            Path mainDirectory, long seed, int maxYearsToRun, List<Predicate<FishState>> modelInterruptors
    ) throws IOException {


        FishYAML yaml = new FishYAML();
        final List<String> columnsToPrint =
                yaml.loadAs(new FileReader(
                                listOfColumnsToPrintFile.toFile()
                        ),
                        LinkedList.class);

        final List<OptimizationParameter> parameters =
                yaml.loadAs(new FileReader(
                                parameterFile.toFile()
                        ),
                        LinkedList.class);


        String computerName = FishStateUtilities.getComputerName();
        MersenneTwisterFast random = new MersenneTwisterFast();
        int directoryIndex = random.nextInt(999999);
        Path scenarioDirectory = mainDirectory.resolve("scenarios").resolve(computerName + "_" + directoryIndex);
        scenarioDirectory.toFile().mkdirs();
        Path summaryDirectory = scenarioDirectory.resolve("summaries");
        summaryDirectory.toFile().mkdir();


        FileWriter summaryStatisticsWriter =
                new FileWriter(summaryDirectory.resolve("summary_statistics_" + seed + ".csv").toFile());
        summaryStatisticsWriter.write("run,year,scenario,variable,value\n");
        summaryStatisticsWriter.flush();


        System.out.println("working in directory: " + scenarioDirectory);

        FileWriter parameterMasterFile = NoData718Slice3.initializeParameterMasterFile(summaryDirectory, parameters);


        for (int i = 0; i < 50000; i++) {
            final Path writtenScenario = NoData718Slice3.writeToFileOneScenario(scenarioDirectory,
                    parameters,
                    baselineScenarioFile,
                    parameterMasterFile,
                    new MersenneTwisterFast(),
                    i
            );
            NoDataTachiuoSlice1.runOneScenario(
                    seed,
                    columnsToPrint,
                    summaryStatisticsWriter,
                    writtenScenario,
                    maxYearsToRun,
                    modelInterruptors
            );
        }

    }


    /**
     * runs one accepted scenario provided
     * @param scenarioFile the scenario file
     * @param shockYear the year at which policy occurs
     * @param yearsToRunAfterShock how many years to simulate AFTER the shock
     * @param seed the random seed
     * @param outputFolder where to store results
     * @param policyMap a list of names--->additional_startable_factory where the factory will be called when shock year occurs
     * @param columnsToPrint columns to print
     * @param printYAMLScenario flag: shall we print the scenario in the new folder
     * @param commonPolicy in optional (nullable) consumer that sets up the scenario and is called whatever policy we are currently simulating
     * @param additionalPlugins additional plugins that are not part of the policy but should be started at some given years anyway!
     * @throws IOException
     */
    public static void runOneAcceptedScenario(
            Path scenarioFile, int shockYear, int yearsToRunAfterShock,
            long seed,
            Path outputFolder,
            LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> policyMap,
            List<String> columnsToPrint,
            boolean printYAMLScenario,
            @Nullable
                    Consumer<Scenario> commonPolicy,
            //the problem with adding plugins through scenario is that they may screw up the seed as the stack has to randomize it
            //the solution then is simply not to start anything until the right year arrives. This will make the seed
            //still inconsistent after the startable... starts, but at least until then it's okay
            @Nullable
                    LinkedList<Pair<Integer,
                            AlgorithmFactory<? extends AdditionalStartable>>> additionalPlugins) throws IOException {

        String filename =      scenarioFile.toAbsolutePath().toString().replace('/','$');

        System.out.println(filename);
        if(outputFolder.resolve(filename + ".csv").toFile().exists())
        {
            System.out.println(filename + " already exists!");
            return;

        }
        if(printYAMLScenario && !outputFolder.resolve(filename).toFile().exists())
            Files.copy(scenarioFile,outputFolder.resolve(filename));


        FileWriter fileWriter = new FileWriter(outputFolder.resolve(filename + ".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for (Map.Entry<String,  AlgorithmFactory<? extends AdditionalStartable>> policyRun : policyMap.entrySet()) {
            String policyName = policyRun.getKey();
            //add some information gathering

            BatchRunner runner = new BatchRunner(
                    scenarioFile,
                    shockYear + yearsToRunAfterShock,
                    columnsToPrint,
                    outputFolder,
                    null,
                    seed,
                    -1
            );

            //if there needs to be a consumer, do it now!
            if(commonPolicy!=null)
                runner.setScenarioSetup(commonPolicy);

            LinkedList<Pair<Integer, AlgorithmFactory<? extends AdditionalStartable>>> plugins = new LinkedList<>();
            plugins.add(new Pair<>(shockYear,policyRun.getValue()));
            if(additionalPlugins!=null)
                plugins.addAll(additionalPlugins);
            runner.setOutsidePlugins(plugins);

            //remember to output the policy tag
            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(policyName).append(",");
                }
            });

            StringBuffer tidy = new StringBuffer();
            runner.run(tidy);
            fileWriter.write(tidy.toString());
            fileWriter.flush();


        }
        fileWriter.close();

    }



}
