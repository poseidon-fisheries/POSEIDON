package uk.ac.ox.oxfish.experiments.indonesia.limited;

import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * my thoughts evolved a bit on this. I used to use predicates to filter runs straight in java but now I seem
 * to be just outputting to file and then using R to filter runs. If that's the case we don't really need predicates
 * anymore, just a list of variables to output to file.
 */
public class NoData718Slice6 {

    final static public Path MAIN_DIRECTORY =
            Paths.get("docs", "indonesia_hub",
                    "runs", "718",
                    "slice6limited");


    final static public int MAX_YEARS_TO_RUN = 40;



    public static void main(String[] args) throws IOException {

        runSlice(
                MAIN_DIRECTORY.resolve("base.yaml"),
                MAIN_DIRECTORY.resolve("parameters.yaml"),
                MAIN_DIRECTORY.resolve("columnsToPrint.yaml"),
                MAIN_DIRECTORY,
                0L,
                MAX_YEARS_TO_RUN


        );
//        runSliceIntegrated(
//                MAIN_DIRECTORY.resolve("base.yaml"),
//                MAIN_DIRECTORY.resolve("parameters.yaml"),
//                MAIN_DIRECTORY.resolve("columnsToPrint.yaml"),
//                MAIN_DIRECTORY,
//                0L,
//                MAX_YEARS_TO_RUN
//
//
//        );
    }



    public static void runSliceIntegrated(
            Path baselineScenarioFile,
            Path parameterFile,
            Path listOfColumnsToPrintFile,
            Path mainDirectory, long seed, int maxYearsToRun
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
        Path scenarioDirectory = mainDirectory.resolve("scenarios_integrated").
                resolve(computerName+"_"+directoryIndex);
        scenarioDirectory.toFile().mkdirs();
        Path summaryDirectory = scenarioDirectory.resolve("summaries");
        summaryDirectory.toFile().mkdir();




        FileWriter summaryStatisticsWriter =
                new FileWriter(summaryDirectory.resolve("summary_statistics_" + seed + ".csv").toFile());
        summaryStatisticsWriter.write("run,year,scenario,price_shock_year,variable,value\n");
        summaryStatisticsWriter.flush();


        System.out.println("working in directory: " + scenarioDirectory);

        FileWriter parameterMasterFile = NoData718Slice3.initializeParameterMasterFile(summaryDirectory, parameters);


        for(int i=0; i<50000; i++) {
            final Path writtenScenario = NoData718Slice3.writeToFileOneScenario(scenarioDirectory,
                    parameters,
                    baselineScenarioFile,
                    parameterMasterFile,
                    new MersenneTwisterFast(),
                    i
            );
            runOneScenarioIntegrated(
                    seed,
                    columnsToPrint,
                    summaryStatisticsWriter,
                    writtenScenario,
                    maxYearsToRun,
                    random.nextInt(10)+25

            );
        }

    }



    public static void runSlice(
            Path baselineScenarioFile,
            Path parameterFile,
            Path listOfColumnsToPrintFile,
            Path mainDirectory, long seed, int maxYearsToRun
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

        FileWriter parameterMasterFile = NoData718Slice3.initializeParameterMasterFile(summaryDirectory, parameters);


        for(int i=0; i<50000; i++) {
            final Path writtenScenario = NoData718Slice3.writeToFileOneScenario(scenarioDirectory,
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
                    maxYearsToRun,
                    mainDirectory
            );
        }

    }






    @NotNull
    public static void runOneScenario(long randomSeed,
                                                   List<String> columns,
                                                   FileWriter summaryStatisticsWriter,
                                                   Path scenarioFile,
                                                   int maxYearsToRun,
                                      Path mainDirectory
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






    @NotNull
    public static void runOneScenarioIntegrated(
            long randomSeed,
                                      List<String> columns,
                                      FileWriter summaryStatisticsWriter,
                                      Path scenarioFile,
                                      int maxYearsToRun,
            int shockYear
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
            //add price shock and seeding to this
            final Consumer<Scenario> priceShockPolicy =
                    NoData718Slice6PriceIncrease.slice6PriceJump.get("Price Shock plus seeding").apply(shockYear);
            batchRunner.setScenarioSetup(priceShockPolicy);


            StringBuffer tidy = new StringBuffer();
            batchRunner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(scenarioFile.toString()).append(",");
                    writer.append(shockYear).append(",");
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

}
