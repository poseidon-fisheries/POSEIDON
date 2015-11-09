package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.selfanalysis.GearImitationAnalysis;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * Just a bunch of runs that will be knitted together into a dashboard of plots to visually study the health of the model
 * Created by carrknight on 10/30/15.
 */
public class Dashboard
{


    private final static Path DASHBOARD_OUTPUT_DIRECTORY = Paths.get("runs","dashboards");

    public final static Path DASHBOARD_INPUT_DIRECTORY = Paths.get("inputs","dashboard");


    private final static int RUNS_PER_SCENARIO = 10  ;


    public static void main(String[] args) throws IOException {

        //get the directory to write in: probably with today's date
        String subDirectory = args[0];
        //turn it into a path
        Path containerPath = DASHBOARD_OUTPUT_DIRECTORY.resolve(subDirectory);
        containerPath.toFile().mkdirs();

        //read in the base scenario
        String baseScenario = String.join("\n", Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("base.yaml")));
        //get ready to initialize stuff
        FishYAML yamler = new FishYAML();

        /***
         *       ___                 ___       _   _       _         _   _
         *      / __|___ __ _ _ _   / _ \ _ __| |_(_)_ __ (_)_____ _| |_(_)___ _ _
         *     | (_ / -_) _` | '_| | (_) | '_ \  _| | '  \| |_ / _` |  _| / _ \ ' \
         *      \___\___\__,_|_|    \___/| .__/\__|_|_|_|_|_/__\__,_|\__|_\___/_||_|
         *                               |_|
         */

        System.out.println("===============================================================");
        System.out.println("Gear Optimization");
        System.out.println("    - Expensive Gas");
        //read and concatenate the YAML
        String expensiveGas = String.join("\n", Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("expensive_gas.yaml")));
        Path output = containerPath.resolve("gearopt");
        output.toFile().mkdirs();
        //putting initial scenario back means that the new yaml will override the old one
        expensiveGas = expensiveGas + "\n" + baseScenario;

        for(int i=0; i<RUNS_PER_SCENARIO; i++)
        {
            gearEvolutionDashboard(yamler, expensiveGas, i, "expensive", output);
        }

        System.out.println("    - Free Gas");

        String freeGas = String.join("\n", Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("free_gas.yaml")));

        freeGas = freeGas + "\n" + baseScenario;

        for(int i=0; i<RUNS_PER_SCENARIO; i++)
        {
            gearEvolutionDashboard(yamler, freeGas, i, "free", output);
        }

        /***
         *       ___             ___              _          ___ _____ ___
         *      / _ \ _ _  ___  / __|_ __  ___ __(_)___ ___ |_ _|_   _/ _ \
         *     | (_) | ' \/ -_) \__ \ '_ \/ -_) _| / -_|_-<  | |  | || (_) |
         *      \___/|_||_\___| |___/ .__/\___\__|_\___/__/ |___| |_| \__\_\
         *                          |_|
         */

        System.out.println("===============================================================");
        System.out.println("One Species ITQ Prices");
        System.out.println("    - Rare Quota");
        output = containerPath.resolve("1itq");
        output.toFile().mkdirs();
        String oneSpeciesYAML = String.join("\n", Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("itq_1_rare.yaml")));
        oneSpeciesYAML = oneSpeciesYAML + "\n" + baseScenario;

        for(int i=0; i<Math.floorDiv(RUNS_PER_SCENARIO, 3); i++)
        {
            oneSpeciesITQRun(yamler, oneSpeciesYAML, i, "rare", output,10);
        }

        System.out.println("    - Common Quota");

        oneSpeciesYAML = String.join("\n", Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("itq_1_common.yaml")));
        oneSpeciesYAML = oneSpeciesYAML + "\n" + baseScenario;
        for(int i=0; i<Math.floorDiv(RUNS_PER_SCENARIO, 3); i++)
        {
            oneSpeciesITQRun(yamler, oneSpeciesYAML, i, "common", output,10);
        }


        System.out.println("    - Hypothetical Quota");

        oneSpeciesYAML = String.join("\n", Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("itq_1_hypothetical.yaml")));
        oneSpeciesYAML = oneSpeciesYAML + "\n" + baseScenario;
        for(int i=0; i<Math.floorDiv(RUNS_PER_SCENARIO, 3); i++)
        {
            hypotheticalOneSpeciesITQRun(yamler, oneSpeciesYAML, i, "hypothetical", output,10);
        }

    }

    private static void oneSpeciesITQRun(
            FishYAML yaml, String scenarioYAML, int run,
            final String outputName, final Path outputPath, final int yearsToRun
    )
    {

        System.out.println("    run " + run);
        //create the model
        FishState state = new FishState(System.currentTimeMillis());
        //read in the scenario
        Scenario scenario = yaml.loadAs(scenarioYAML,Scenario.class);
        state.setScenario(scenario);
        //run it for 10 years
        state.start();
        while(state.getYear()< yearsToRun)
            state.schedule.step(state);

        //write to file
        File outputFile = outputPath.resolve(outputName + "_" + run + ".csv").toFile();
        FishStateUtilities.printCSVColumnToFile(outputFile,
                                                state.getDailyDataSet().getColumn("ITQ Last Closing Price Of Species 0")
        );


    }

    private static void hypotheticalOneSpeciesITQRun( FishYAML yaml, String scenarioYAML, int run,
                                                      final String outputName, final Path outputPath, final int yearsToRun)
    {
        System.out.println("    run " + run);
        //create the model
        FishState state = new FishState(System.currentTimeMillis());
        //read in the scenario
        Scenario scenario = yaml.loadAs(scenarioYAML,Scenario.class);
        state.setScenario(scenario);

        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                for(Fisher fisher : model.getFishers())
                    //create a lambda gatherer
                    fisher.getDailyData().registerGatherer("Reservation Lambda Owning 1000 quotas",
                                                           fisher1 -> {
                                                               if (state.getDayOfTheYear() == 365)
                                                                   return Double.NaN;
                                                               double probability = 1 - fisher1.probabilityDailyCatchesBelowLevel(
                                                                       0,
                                                                       1000 / (365 - state.getDayOfTheYear()));
                                                               return (probability * fisher1.predictUnitProfit(0));
                                                           }, Double.NaN);

                model.getDailyDataSet().registerGatherer("Average Hypothetical Quota",
                                                         new Function<FishState, Double>() {
                                                             @Override
                                                             public Double apply(FishState state) {
                                                                 return state.getFishers().stream().mapToDouble(
                                                                         value -> value.getDailyData().getLatestObservation("Reservation Lambda Owning 1000 quotas")).sum() / 100d;
                                                             }
                                                         },Double.NaN);
            }

            @Override
            public void turnOff() {

            }
        });

        //run it for 10 years
        state.start();
        while(state.getYear()< yearsToRun)
            state.schedule.step(state);

        //write to file
        File outputFile = outputPath.resolve(outputName + "_" + run + ".csv").toFile();
        FishStateUtilities.printCSVColumnToFile(outputFile,
                                                state.getDailyDataSet().getColumn("Average Hypothetical Quota")
        );




    }

    private static void gearEvolutionDashboard(
            FishYAML yamler, String expensiveGas, int i, final String outputName,
            final Path outputPath) throws IOException {
        System.out.println("    run " + i);
        //create the model
        FishState state = new FishState(System.currentTimeMillis());
        //read in the scenario
        Scenario scenario = yamler.loadAs(expensiveGas,Scenario.class);
        state.setScenario(scenario);
        //just add a daily average
        DataColumn mileage =
                state.getDailyDataSet().registerGatherer("Average Gas Consumption",
                                                         new Function<FishState, Double>() {
                                                             @Override
                                                             public Double apply(FishState state) {
                                                                 double consumption = 0;
                                                                 for (Fisher f : state.getFishers())
                                                                     consumption += ((RandomCatchabilityTrawl) f.getGear()).getTrawlSpeed();
                                                                 return consumption / state.getFishers().size();
                                                             }
                                                         }, Double.NaN);

        //run it for 10 years
        state.start();
        //make agents optimize their gear
        GearImitationAnalysis.attachGearAnalysisToEachFisher(state.getFishers(), state, new ArrayList<>());
        while(state.getYear()<10)
            state.schedule.step(state);

        //write to file
        File outputFile = outputPath.resolve(outputName + "_" + i + ".csv").toFile();
        FishStateUtilities.printCSVColumnToFile(outputFile, mileage
        );
    }


}
