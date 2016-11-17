package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        //don't log during the dashboarding
        Log.set(Log.LEVEL_NONE);
        //get the directory to write in: probably with today's date
        String subDirectory = args[0];
        //turn it into a osmoseWFSPath
        Path containerPath = DASHBOARD_OUTPUT_DIRECTORY.resolve(subDirectory);
        containerPath.toFile().mkdirs();

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

        for(int i=0; i<RUNS_PER_SCENARIO; i++)
        {
            gearEvolutionDashboard(yamler, expensiveGas, i, "expensive", output, System.currentTimeMillis());
        }

        System.out.println("    - Free Gas");

        String freeGas = String.join("\n", Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("free_gas.yaml")));

        for(int i=0; i<RUNS_PER_SCENARIO; i++)
        {
            gearEvolutionDashboard(yamler, freeGas, i, "free", output, System.currentTimeMillis());
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

        for(int i=0; i<Math.floorDiv(RUNS_PER_SCENARIO, 3); i++)
        {
            oneSpeciesITQRun(yamler, oneSpeciesYAML, i, "rare", output,10);
        }

        System.out.println("    - Common Quota");

        oneSpeciesYAML = String.join("\n", Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("itq_1_common.yaml")));
        for(int i=0; i<Math.floorDiv(RUNS_PER_SCENARIO, 3); i++)
        {
            oneSpeciesITQRun(yamler, oneSpeciesYAML, i, "common", output,10);
        }


        System.out.println("    - Hypothetical Quota");

        oneSpeciesYAML = String.join("\n", Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("itq_1_hypothetical.yaml")));
        for(int i=0; i<Math.floorDiv(RUNS_PER_SCENARIO, 3); i++)
        {
            hypotheticalOneSpeciesITQRun(yamler, oneSpeciesYAML, i, "hypothetical", output,10);
        }


        /***
         *      ___  _     __              _   _               _   ___    _             _
         *     |   \(_)___/ _|_  _ _ _  __| |_(_)___ _ _  __ _| | | __| _(_)___ _ _  __| |___
         *     | |) | (_-<  _| || | ' \/ _|  _| / _ \ ' \/ _` | | | _| '_| / -_) ' \/ _` (_-<
         *     |___/|_/__/_|  \_,_|_||_\__|\__|_\___/_||_\__,_|_| |_||_| |_\___|_||_\__,_/__/
         *
         */
        System.out.println("===============================================================");
        System.out.println("Disfunctional Friends");
        String disfunctionalYaml = String.join("\n", Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("disfunctional.yaml")));
        String toOutput = "friends,steps\n";
        //check how much it takes in days to consume 95% of all the biomass
        for(int friends =0; friends<30;friends++)
        {
            int steps = disfunctionalFriendsRun(friends,
                                                yamler.loadAs(disfunctionalYaml,PrototypeScenario.class),
                                                15);
            toOutput = toOutput + friends + "," + steps + "\n";
        }
        Path outputPath = containerPath.resolve("disfunctional");
        outputPath.toFile().mkdir();
        Files.write(outputPath.resolve("disfunctional.csv"), toOutput.getBytes());
        /***
         *      ___ _____ ___     ___     _    _
         *     |_ _|_   _/ _ \   / __|_ _(_)__| |
         *      | |  | || (_) | | (_ | '_| / _` |
         *     |___| |_| \__\_\  \___|_| |_\__,_|
         *
         */
        System.out.println("===============================================================");
        System.out.println("ITQ Grid");
        String umuffledITQ = String.join("\n",
                                               Files.readAllLines(DASHBOARD_INPUT_DIRECTORY.resolve("itq_geography.yaml")));
        outputPath = containerPath.resolve("grid");
        outputPath.toFile().mkdir();
        for(int i=0; i<5; i++)
        {
            System.out.println("    Run:" + i);
            unmuffledITQGrid(yamler.loadAs(umuffledITQ, PrototypeScenario.class),
                             5,
                             "grid_"+i+".csv",
                             outputPath);

        }


    }


    private static void unmuffledITQGrid(PrototypeScenario scenario,
                                         final int yearsToRun,
                                         final String outputName, final Path outputPath) throws IOException {
        scenario.forcePortPosition(new int[]{40,25});
        assert  scenario.isUsePredictors();
        final FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();
        //run first year "free"
        while(state.getYear()<1)
            state.schedule.step(state);


        double[][] theGrid = new double[state.getMap().getWidth()][state.getMap().getHeight()];
        double[][] theBlue = new double[state.getMap().getWidth()][state.getMap().getHeight()];

        while(state.getYear()<yearsToRun)
        {
            state.schedule.step(state);
            IntGrid2D trawls = state.getMap().getDailyTrawlsMap();
            for(int x =0; x<state.getMap().getWidth(); x++)
            {
                for (int y = 0; y < state.getMap().getHeight(); y++)
                {
                    theGrid[x][state.getMap().getHeight()-y-1] += trawls.get(x, y);
                    theBlue[x][state.getMap().getHeight()-y-1] += state.getMap().getSeaTile(x,y).
                            getBiomass(state.getSpecies().get(1));
                }
            }
        }

        String grid = FishStateUtilities.gridToCSV(theGrid);
        Files.write(outputPath.resolve(outputName),grid.getBytes());
        grid = FishStateUtilities.gridToCSV(theBlue);
        Files.write(outputPath.resolve("blue_"+outputName),grid.getBytes());


    }

    /**
     * run a disfunctional friends simulation: very low p, very high i
     * @param friends how many friends each person has
     * @param scenario the original scenario, as read from the yaml
     * @param maxYearsToRun maximum years after which to cut the simulation
     * @return
     */
    public static int disfunctionalFriendsRun(int friends, PrototypeScenario scenario,
                                               final int maxYearsToRun)
    {
        System.out.println("    Friends: " + friends);
        if(friends > 0) {
            EquidegreeBuilder networkBuilder = new EquidegreeBuilder();
            networkBuilder.setDegree(friends);
            scenario.setNetworkBuilder(networkBuilder);
        }
        else
            scenario.setNetworkBuilder(new EmptyNetworkBuilder());
        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();

        Species onlySpecies = state.getBiology().getSpecie(0);
        //how much does it take to eat 95% of all the fish?
        final double minimumBiomass = state.getTotalBiomass(onlySpecies) * .05;


        int steps = 0;
        while(state.getYear()<=maxYearsToRun)
        {
            state.schedule.step(state);
            if (state.getTotalBiomass(onlySpecies) <= minimumBiomass)
                break;
            steps++;
        }
        return steps;
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

    public static void hypotheticalOneSpeciesITQRun( FishYAML yaml, String scenarioYAML, int run,
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
                                                         new Gatherer<FishState>() {
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

    public static void gearEvolutionDashboard(
            FishYAML yamler, String expensiveGas, int i, final String outputName,
            final Path outputPath, final long randomSeed) throws IOException {
        System.out.println("    run " + i);
        //create the model
        FishState state = new FishState(randomSeed);
        //read in the scenario
        Scenario scenario = yamler.loadAs(expensiveGas,Scenario.class);
        state.setScenario(scenario);
        //just add a daily average
        DataColumn mileage =
                state.getDailyDataSet().registerGatherer("Average Gas Consumption",
                                                         new Gatherer<FishState>() {
                                                             @Override
                                                             public Double apply(FishState state) {
                                                                 double consumption = 0;
                                                                 for (Fisher f : state.getFishers())
                                                                     consumption += ((RandomCatchabilityTrawl) f.getGear()).getGasPerHourFished();
                                                                 return consumption / state.getFishers().size();
                                                             }
                                                         }, Double.NaN);

        //run it for 10 years
        state.start();
        //make agents optimize their gear

        while(state.getYear()<10)
            state.schedule.step(state);

        //write to file
        File outputFile = outputPath.resolve(outputName + "_" + i + ".csv").toFile();
        FishStateUtilities.printCSVColumnToFile(outputFile, mileage
        );
    }


}
