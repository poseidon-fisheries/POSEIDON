package uk.ac.ox.oxfish.experiments;

import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.experiments.indonesia.limited.NoData718Slice3;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class NoDataTachiuoSlice1 {

    //docs/20200425 abc_example/
    final static public Path MAIN_DIRECTORY =
            Paths.get("docs", "20200425 abc_example");


    final static public int MAX_YEARS_TO_RUN = 45;



    public static void main(String[] args) throws IOException {

        runSlice(
                MAIN_DIRECTORY.resolve("base.yaml"),
                MAIN_DIRECTORY.resolve("parameters.yaml"),
                MAIN_DIRECTORY.resolve("columnsToPrint.yaml"),
                MAIN_DIRECTORY,
                0L,
                MAX_YEARS_TO_RUN
        );
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





}
