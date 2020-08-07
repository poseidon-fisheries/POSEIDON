package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static uk.ac.ox.oxfish.experiments.indonesia.limited.NoData718Slice3.setupScenario;
import static uk.ac.ox.oxfish.experiments.indonesia.limited.NoData718Slice6.MAIN_DIRECTORY;

/**
 * trying to make a single run version of slice6 that we can calibrate externally
 * with SMC
 */
public class NoData718Slice6Iterative {


    public static final String DIRECTORY = "scenarios_iterative";


    public static void main(String[] args) throws IOException {



        int scenarioID = Integer.parseInt(args[0]);
        System.out.println("scenarioID: " + scenarioID);


        double[] parameters = new double[args.length-1];

        Preconditions.checkArgument(parameters.length>0, "no parameter received!");

        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = Double.parseDouble(args[i+1]);
        }
        System.out.println("parametrers: " + Arrays.toString(parameters));


        final Path outputDirectory = MAIN_DIRECTORY.resolve(DIRECTORY).resolve(FishStateUtilities.getComputerName());
        outputDirectory.toFile().mkdirs();
        runOneTime(
                MAIN_DIRECTORY.resolve("base.yaml"),
                MAIN_DIRECTORY.resolve("parameters.yaml"),
                MAIN_DIRECTORY.resolve("columnsToPrint.yaml"),
                outputDirectory,
                0l,
                NoData718Slice6.MAX_YEARS_TO_RUN,
                parameters,
                scenarioID



        );

    }

//    private static FileWriter getSummaryStatisticsFile(
//            Path outputDirectory
//
//    ) throws IOException {
//        final Path summaryFilePath = outputDirectory.resolve("summary.csv");
//        if(Files.exists(summaryFilePath))
//            return new FileWriter(summaryFilePath.toFile(),true);
//        else {
//            final FileWriter summaryWriter = new FileWriter(summaryFilePath.toFile(), true);
//            summaryWriter.write("run,year,scenario,variable,value\n");
//            summaryWriter.flush();
//            return summaryWriter;
//        }
//
//
//    }



    public static void runOneTime(
            Path baselineScenarioFile,
            Path parameterFile,
            Path listOfColumnsToPrintFile,
            Path outputDirectory,
            long seed,
            int maxYearsToRun,
            double[] parameterValues,
            int scenarioID
    ) throws IOException {


        //read infos
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
        Path scenarioDirectory = outputDirectory.resolve(DIRECTORY).resolve(computerName);
        scenarioDirectory.toFile().mkdirs();

        //create scenario to run from parameters given
        Scenario scenario = yaml.loadAs(new FileReader(baselineScenarioFile.toFile()), Scenario.class);
        final Pair<Scenario, String[]> scenarioPair = setupScenario(scenario, parameterValues, parameters);
        final Path pathToScenario = scenarioDirectory.resolve("scenario_" + scenarioID + ".yaml");
        yaml.dump(scenarioPair.getFirst(),new FileWriter(pathToScenario.toFile()));

        //run scenario
        final StringBuffer tidyOutput = NoData718Slice6.runOneScenario(
                seed,
                columnsToPrint,
                null,
                pathToScenario,
                maxYearsToRun
        );

        FileWriter writer = new FileWriter(
                scenarioDirectory.resolve("scenario_" + scenarioID + ".csv").toFile()
        );
        writer.write("run,year,policy,variable,value\n");
        writer.write(tidyOutput.toString());
        writer.flush();;
        writer.close();



    }


}
