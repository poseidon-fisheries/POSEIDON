package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.selfanalysis.GearImitationAnalysis;
import uk.ac.ox.oxfish.model.FishState;
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
         *      _____                  ____        __   _         _            __   _
         *     / ___/___  ___ _ ____  / __ \ ___  / /_ (_)__ _   (_)___ ___ _ / /_ (_)___   ___
         *    / (_ // -_)/ _ `// __/ / /_/ // _ \/ __// //  ' \ / //_ // _ `// __// // _ \ / _ \
         *    \___/ \__/ \_,_//_/    \____// .__/\__//_//_/_/_//_/ /__/\_,_/ \__//_/ \___//_//_/
         *                                /_/
         */

        System.out.println("===============================================================");
        System.out.println("Gear Optimization");
        System.out.println("    - Expensive Gas");
        //read and concatenate the YAML
        String expensiveGas = String.join("\n", Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("expensive_gas.yaml")));
        Path output = containerPath.resolve("gearopt");
        output.toFile().mkdirs();

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



    }

    private static void gearEvolutionDashboard(
            FishYAML yamler, String expensiveGas, int i, final String outputName,
            final Path outputPath) throws IOException {
        System.out.println("run " + i);
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
        FishStateUtilities.printCSVColumnToFile(mileage,
                                                outputFile);
    }


}
