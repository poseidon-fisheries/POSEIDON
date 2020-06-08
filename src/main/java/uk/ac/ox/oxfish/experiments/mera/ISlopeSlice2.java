package uk.ac.ox.oxfish.experiments.mera;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.experiments.indonesia.limited.NoData718Slice3;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ISlopeSlice2 {


    final static public Path MAIN_DIRECTORY =
            Paths.get("docs", "20200604 islope",
                    "slice2");


    final static public int MAX_YEARS_TO_RUN = 40;



    public static void main(String[] args) throws IOException {

        runSlice(
                MAIN_DIRECTORY.resolve("base_islope_slice2.yaml"),
                MAIN_DIRECTORY.resolve("parameters_nomovement.yaml"),
                MAIN_DIRECTORY.resolve("columnsToPrint.yaml"),
                MAIN_DIRECTORY,
                0L,
                MAX_YEARS_TO_RUN, 5000


        );
    }


    public static void runSlice(
            Path baselineScenarioFile,
            Path parameterFile,
            Path listOfColumnsToPrintFile,
            Path mainDirectory, long seed, int maxYearsToRun,
            int totalRunsToMake
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
        int directoryIndex =  random.nextInt(999999);
        Path scenarioDirectory = mainDirectory.resolve("scenarios").resolve(computerName+"_"+directoryIndex);
        scenarioDirectory.toFile().mkdirs();
        Path summaryDirectory = scenarioDirectory.resolve("summaries");
        summaryDirectory.toFile().mkdir();




        FileWriter summaryStatisticsWriter =
                new FileWriter(summaryDirectory.resolve("summary_statistics_" + seed + ".csv").toFile());
        summaryStatisticsWriter.write("run,year,scenario,variable,value\n");
        summaryStatisticsWriter.flush();


        System.out.println("working in directory: " + scenarioDirectory);

        FileWriter parameterMasterFile = initializeParameterMasterFile(summaryDirectory, parameters);


        for(int i = 0; i< totalRunsToMake; i++) {
            final Path writtenScenario = writeToFileOneScenario(scenarioDirectory,
                    parameters,
                    baselineScenarioFile,
                    parameterMasterFile,
                    new MersenneTwisterFast(),
                    i
            );
            runOneScenario(
                    seed,
                    columnsToPrint,
                    summaryStatisticsWriter,
                    writtenScenario,
                    maxYearsToRun
            );
        }

    }






    @NotNull
    public static void runOneScenario(long randomSeed,
                                      List<String> columns,
                                      FileWriter summaryStatisticsWriter,
                                      Path scenarioFile,
                                      int maxYearsToRun
    ) throws IOException {

        System.out.println(scenarioFile.toFile().getAbsolutePath() );

        try {
            long start = System.currentTimeMillis();


            final BatchRunner batchRunner = new BatchRunner(
                    scenarioFile,
                    maxYearsToRun,
                    columns,
                    null,
                    null,
                    randomSeed,
                    -1
            );
            StringBuffer tidy = new StringBuffer();

            batchRunner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(scenarioFile.toString()).append(",");
                }
            });

            batchRunner.run(tidy);
            summaryStatisticsWriter.write(tidy.toString());
            summaryStatisticsWriter.flush();

            long end = System.currentTimeMillis();
            System.out.println( "Run lasted: " + (end-start)/1000 + " seconds");
        }
        catch (OutOfMemoryError e){
        }
        System.out.println(scenarioFile.toFile().getAbsolutePath() );
        System.out.println("--------------------------------------------------------------");
    }

    public static Path writeToFileOneScenario(Path destinationFolder,
                                              List<OptimizationParameter> parameters,
                                              Path pathToBaselineScenario,
                                              FileWriter parameterMasterFileWriter,
                                              MersenneTwisterFast randomizer,
                                              int scenarioID) throws IOException {
        FishYAML yaml = new FishYAML();

        double[] randomValues = new double[parameters.size()];
        for (int h = 0; h < randomValues.length; h++) {
            randomValues[h] = randomizer.nextDouble() * 20 - 10;
        }
        Scenario scenario = yaml.loadAs(new FileReader(pathToBaselineScenario.toFile()), Scenario.class);
        final Pair<Scenario, String[]> scenarioPair = setupScenario(scenario, randomValues, parameters);
        final Path pathToScenario = destinationFolder.resolve("scenario_" + scenarioID + ".yaml");
        yaml.dump(scenarioPair.getFirst(), new FileWriter(pathToScenario.toFile()));

        for (String value : scenarioPair.getSecond()) {
            parameterMasterFileWriter.write(value);
            parameterMasterFileWriter.write(",");
        }
        parameterMasterFileWriter.write(pathToScenario.toString());
        parameterMasterFileWriter.write("\n");

        parameterMasterFileWriter.flush();
        return pathToScenario;
    }
    public static Pair<Scenario,String[]> setupScenario(Scenario scenario,
                                                        double[] randomValues,
                                                        List<OptimizationParameter> parameters) {

        Preconditions.checkState(parameters.size()==randomValues.length);
        String[] values = new String[randomValues.length];
        for (int i = 0; i < randomValues.length; i++) {


            values[i] =
                    parameters.get(i).parametrize(scenario,
                            new double[]{randomValues[i]});


        }


        return new Pair<>(scenario,values);
    }

    public static FileWriter initializeParameterMasterFile(Path folder,
                                                           List<OptimizationParameter> parameters) throws IOException {
        FileWriter masterFile = new FileWriter(folder.resolve("masterfile.csv").toFile());
        for (OptimizationParameter parameter : parameters) {
            masterFile.write(parameter.getName());
            masterFile.write(",");

        }
        masterFile.write("filename");
        masterFile.write("\n");
        masterFile.flush();
        return masterFile;
    }

}
