package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.biology.LogisticLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.OsmoseBiologyFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFishStateTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFisherTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.ExplorationPenaltyProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A series of runs and outputs that will turn hopefully into the paper
 * Created by carrknight on 1/21/16.
 */
public class FirstPaper
{

    public static final Path INPUT_FOLDER = Paths.get("inputs", "first_paper");
    public static final Path OUTPUT_FOLDER = Paths.get("runs", "first_paper_temp");

    public static final long RANDOM_SEED = 0l;


    public static void main(String[] args) throws IOException {

        OUTPUT_FOLDER.toFile().mkdirs();

        /*
        Log.info("Moving Front Image Starting");
        fronts();
        frontsSensitivity(Paths.get("fronts.yaml"));
        frontsSensitivity(Paths.get("sensitivity","fronts","fronts_worst.yaml"));
        frontsSensitivity(Paths.get("sensitivity","fronts","fronts_flat.yaml"));
        Log.info("Hyperstability");
        hyperStability();
        Log.info("Oil Price Changes");
        oilCheck();
        oils(1);
        oils(2);
        Log.info("Fishing the Line");
        mpa();
        Log.info("OSMOSE");
        osmoseDemersal2(50);
        Log.info("Best Heuristic");
        optimalHeuristic();
        Log.info("Hard Switch");
        hardSwitch(INPUT_FOLDER.resolve("hardswitch.yaml"), OUTPUT_FOLDER.resolve("hardswitch").resolve("hardswitch.csv"));

       hardSwitch(INPUT_FOLDER.resolve("sensitivity").resolve("hardswitch"). //sensitivity
                resolve("large_difference.yaml"), INPUT_FOLDER.resolve("sensitivity").resolve("hardswitch").resolve("large_difference.csv"));

        Log.info("Directed Technological Change");


        directedTechnologicalChange();

        Log.info("TAC vs ITQ 1 Species");
        catchesPerPolicyCatchability();
        catchesPerPolicyMileage();
        Log.info("Race to Fish");
        raceToFish();
        Log.info("Location Choice");
        policyAndLocation("itq");
        policyAndLocation("tac");
        Log.info("Gear Choice");
        policyAndGear("itq");
        policyAndGear("tac");

*/
        policyAndLocation("itq_sensitivity");
        //policyAndGear("itq_sensitivity");

    }

    private static void policyAndLocation(final String policy) throws IOException {

        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("location_" + policy + ".yaml")));
        Path outputFolder = OUTPUT_FOLDER.resolve("location");
        outputFolder.toFile().mkdirs();
        Scenario scenario =  yaml.loadAs(scenarioYaml,Scenario.class);
        FishState state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);


        state.start();
        Species specie0 = state.getSpecies().get(0);
        //here we store the fishing pressure
        double[][] fishing = new double[state.getMap().getWidth()][state.getMap().getHeight()];
        //here we store the blue biomass (for diagnostics)
        double[][] blue = new double[state.getMap().getWidth()][state.getMap().getHeight()];


        while (state.getYear() < 1)
            state.schedule.step(state); //ignore




        while (state.getYear() < 5)
        {
            state.schedule.step(state);
            IntGrid2D trawls = state.getMap().getDailyTrawlsMap();
            for (int x = 0; x < state.getMap().getWidth(); x++)
                for (int y = 0; y < state.getMap().getHeight(); y++)
                    fishing[x][state.getMap().getHeight() - y - 1] += trawls.get(x, y);


        }
        for (int x = 0; x < state.getMap().getWidth(); x++)
            for (int y = 0; y < state.getMap().getHeight(); y++) {
                SeaTile seaTile = state.getMap().getSeaTile(x, y);
                if(seaTile.getAltitude()<0)
                    blue[x][state.getMap().getHeight() - y - 1] = ((LogisticLocalBiology) seaTile.
                            getBiology()).getCarryingCapacity(state.getSpecies().get(1));
            }

        String grid = FishStateUtilities.gridToCSV(fishing);
        Files.write(outputFolder.resolve(policy + "_tows.csv"), grid.getBytes());
        grid = FishStateUtilities.gridToCSV(blue);
        Files.write(outputFolder.resolve(policy + "_blue.csv"), grid.getBytes());


    }







    private static void raceToFish() throws IOException {
        Path outputFolder = OUTPUT_FOLDER.resolve("race");
        outputFolder.toFile().mkdirs();
        RaceToFish.policySweepRaceToFish("race2",
                                         INPUT_FOLDER,
                                         50,
                                         outputFolder,
                                         RaceToFish.EFFORT_COLUMN_NAME,
                                         5000
        );
    }

    public static void frontsSensitivity(Path pathToYaml) throws IOException {
        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve(pathToYaml)));

        Path outputFolder = OUTPUT_FOLDER.resolve(Paths.get("sensitivity","fronts"));
        outputFolder.toFile().mkdirs();
        Scenario scenario = yaml.loadAs(scenarioYaml, Scenario.class);
        FishState state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);
        state.start();
        state.attachAdditionalGatherers();
        while(state.getYear()<3)
        {
            state.schedule.step(state);
        }
        FishStateUtilities.printCSVColumnToFile(outputFolder.resolve(pathToYaml.getFileName() + ".csv").toFile(),
                                                state.getDailyDataSet().getColumn("Average Distance From Port"));
    }

    public static void fronts() throws IOException {
        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("fronts.yaml")));
        Path outputFolder = OUTPUT_FOLDER.resolve("fronts");
        outputFolder.toFile().mkdirs();
        Scenario scenario =  yaml.loadAs(scenarioYaml,Scenario.class);
        FishState state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);


        state.start();
        Species specie0 = state.getSpecies().get(0);
        //here we store the fishing pressure
        double[][] fishing = new double[state.getMap().getWidth()][state.getMap().getHeight()];
        //here we store the biomass
        double[][] biomass = new double[state.getMap().getWidth()][state.getMap().getHeight()];


        for(int snapshot=1;snapshot<5;snapshot++)
        {
            while (state.getYear() < snapshot)
            {
                state.schedule.step(state);
                IntGrid2D trawls = state.getMap().getDailyTrawlsMap();
                for (int x = 0; x < state.getMap().getWidth(); x++)
                    for (int y = 0; y < state.getMap().getHeight(); y++)
                        fishing[x][state.getMap().getHeight() - y - 1] += trawls.get(x, y);


            }
            for (int x = 0; x < state.getMap().getWidth(); x++)
                for (int y = 0; y < state.getMap().getHeight(); y++)

                    biomass[x][state.getMap().getHeight() - y - 1] = state.getMap().getSeaTile(x,y).
                            getBiomass(specie0);

            String grid = FishStateUtilities.gridToCSV(fishing);
            Files.write(outputFolder.resolve("fishing"+snapshot+".csv"),grid.getBytes());
            grid = FishStateUtilities.gridToCSV(biomass);
            Files.write(outputFolder.resolve("biomass"+snapshot+".csv"),grid.getBytes());

            //reset fishing grid for next year
            for (int x = 0; x < state.getMap().getWidth(); x++)
                for (int y = 0; y < state.getMap().getHeight(); y++)
                    fishing[x][state.getMap().getHeight() - y - 1]=0d;

        }





    }


    public static void hyperStability() throws IOException {
        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("hyperstability.yaml")));

        Path outputFolder = OUTPUT_FOLDER.resolve(Paths.get("hyperstability"));
        outputFolder.toFile().mkdirs();
        Scenario scenario = yaml.loadAs(scenarioYaml, Scenario.class);
        FishState state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);
        state.start();
        while(state.getYear()<40)
        {
            state.schedule.step(state);
        }
        FishStateUtilities.printCSVColumnsToFile(outputFolder.resolve("hyperstability.csv").toFile(),
                                                 state.getYearlyDataSet().getColumn("Total Effort"),
                                                 state.getYearlyDataSet().getColumn("Species 0 Landings"),
                                                 state.getYearlyDataSet().getColumn("Biomass Species 0"));


        scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("fronts.yaml")));


        scenario = yaml.loadAs(scenarioYaml, Scenario.class);
        state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);
        state.start();
        while(state.getYear()<40)
        {
            state.schedule.step(state);
        }
        FishStateUtilities.printCSVColumnsToFile(outputFolder.resolve("baseline.csv").toFile(),
                                                 state.getYearlyDataSet().getColumn("Total Effort"),
                                                 state.getYearlyDataSet().getColumn("Species 0 Landings"),
                                                 state.getYearlyDataSet().getColumn("Biomass Species 0"));
    }


    private final static double MIN_GAS_PRICE = 0;
    private final static double MAX_GAS_PRICE = 5;
    private final static double GAS_PRICE_INCREMENT = 0.01;



    public static void oilCheck() throws IOException {
        //tracks the changes in distance to port given price changes
        List<String> lines = new LinkedList<>();
        lines.add("price,type,distance,landings,consumed");
        for(double gasPrice = MIN_GAS_PRICE; gasPrice<= MAX_GAS_PRICE; gasPrice+=GAS_PRICE_INCREMENT)
        {
            gasPrice = FishStateUtilities.round5(gasPrice);
            double[] result = oil(gasPrice, "oil_travel.yaml");
            String line = gasPrice + "," + "oil_travel" + "," + result[0] + "," + result[1]
                    + "," + result[2];
            Log.info(line);
            lines.add(line);
        }
        for(double gasPrice = MIN_GAS_PRICE; gasPrice<= MAX_GAS_PRICE; gasPrice+=GAS_PRICE_INCREMENT)
        {
            gasPrice = FishStateUtilities.round5(gasPrice);
            double[] result = oil(gasPrice, "oil_trawl.yaml");
            String line = gasPrice + "," + "oil_trawl" + "," + result[0] + "," + result[1]
                    + "," + result[2];
            Log.info(line);
            lines.add(line);
        }
        Path outputFolder = OUTPUT_FOLDER.resolve("oil");
        outputFolder.toFile().mkdirs();
        Files.write(outputFolder.resolve("oil_grid.csv"),lines);


    }

    public static double[] oil(double gasPrice,String yamlFile) throws IOException {
        /**
         * first oil
         */
        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve(yamlFile)));
        PrototypeScenario scenario =  yaml.loadAs(scenarioYaml,PrototypeScenario.class);
        scenario.setGasPricePerLiter(new FixedDoubleParameter(gasPrice));
        FishState state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);
        state.attachAdditionalGatherers();
        state.start();

        double[] results = new double[3];
        while(state.getYear()<2)
            state.schedule.step(state);
        state.schedule.step(state);
        //count the landings
        results[1] = state.getYearlyDataSet().getLatestObservation("Species 0 Landings");
        results[2] = state.getYearlyDataSet().getLatestObservation(YearlyFisherTimeSeries.FUEL_CONSUMPTION);
        //and the average distance to port on the last 30 days!
        DataColumn distances = state.getDailyDataSet().getColumn("Average Distance From Port");
        results[0]=distances.stream().skip(distances.size()-30).
                filter(distance -> Double.isFinite(distance)).
                mapToDouble(value -> value).average().getAsDouble();

        return results;
    }

    public static void oils(int i) throws IOException {

        /**
         * first oil
         */
        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("oil"+i+".yaml")));
        Path outputFolder = OUTPUT_FOLDER.resolve("oil");
        outputFolder.toFile().mkdirs();
        Scenario scenario =  yaml.loadAs(scenarioYaml,Scenario.class);
        FishState state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);


        state.start();
        //here we store the fishing pressure
        double[][] fishing = new double[state.getMap().getWidth()][state.getMap().getHeight()];

        for(int snapshot=1;snapshot<=3;snapshot++)
        {
            while (state.getYear() < snapshot)
            {
                state.schedule.step(state);
                IntGrid2D trawls = state.getMap().getDailyTrawlsMap();
                for (int x = 0; x < state.getMap().getWidth(); x++)
                    for (int y = 0; y < state.getMap().getHeight(); y++)
                        fishing[x][state.getMap().getHeight() - y - 1] += trawls.get(x, y);


            }
            String grid = FishStateUtilities.gridToCSV(fishing);
            Files.write(outputFolder.resolve("oil_"+i+"_"+snapshot+".csv"),grid.getBytes());

            //reset fishing grid for next year
            for (int x = 0; x < state.getMap().getWidth(); x++)
                for (int y = 0; y < state.getMap().getHeight(); y++)
                    fishing[x][state.getMap().getHeight() - y - 1]=0d;

            //raise the price of oil!
            Port port = state.getPorts().iterator().next();
            port.setGasPricePerLiter(port.getGasPricePerLiter()+ 5.0);
        }





    }


    public static void mpa() throws IOException {

        Log.info("Fishing the Line - MPA");

        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("mpa.yaml")));
        Path outputFolder = OUTPUT_FOLDER.resolve("line");
        outputFolder.toFile().mkdirs();
        Scenario scenario =  yaml.loadAs(scenarioYaml,Scenario.class);
        FishState state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);

        state.start();
        double[][] theGrid = new double[state.getMap().getWidth()][state.getMap().getHeight()];

        while(state.getYear()<20)
        {
            state.schedule.step(state);
            IntGrid2D trawls = state.getMap().getDailyTrawlsMap();
            for(int x =0; x<state.getMap().getWidth(); x++)
            {
                for (int y = 0; y < state.getMap().getHeight(); y++)
                {
                    theGrid[x][state.getMap().getHeight()-y-1] += trawls.get(x, y);
                }
            }
        }
        String gridToCSV = FishStateUtilities.gridToCSV(theGrid);
        Files.write(outputFolder.resolve("mpa.csv"),gridToCSV.getBytes());


        Log.info("Fishing the Line - Rocky");

        yaml = new FishYAML();
        scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("rocky.yaml")));
        scenario =  yaml.loadAs(scenarioYaml,Scenario.class);
        state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);

        state.start();
        theGrid = new double[state.getMap().getWidth()][state.getMap().getHeight()];

        while(state.getYear()<20)
        {
            state.schedule.step(state);
            IntGrid2D trawls = state.getMap().getDailyTrawlsMap();
            for(int x =0; x<state.getMap().getWidth(); x++)
            {
                for (int y = 0; y < state.getMap().getHeight(); y++)
                {
                    theGrid[x][state.getMap().getHeight()-y-1] += trawls.get(x, y);
                }
            }
        }
        gridToCSV = FishStateUtilities.gridToCSV(theGrid);
        Files.write(outputFolder.resolve("rocky.csv"),gridToCSV.getBytes());

        theGrid = new double[state.getMap().getWidth()][state.getMap().getHeight()];
        for(int x =0; x<state.getMap().getWidth(); x++)
        {
            for (int y = 0; y < state.getMap().getHeight(); y++)
            {
                theGrid[x][state.getMap().getHeight()-y-1] += state.getMap().getSeaTile(x,y).getRockyPercentage();
            }
        }

        gridToCSV = FishStateUtilities.gridToCSV(theGrid);
        Files.write(outputFolder.resolve("rocky_rock.csv"),gridToCSV.getBytes());

    }




    public static void optimalHeuristic() throws IOException{


        String toOutput = "biology,agent,catches\n";


        HashMap<String,AlgorithmFactory<? extends BiologyInitializer>> biologies = new HashMap<>();
        biologies.put("Fixed", new FromLeftToRightFactory());
        biologies.put("Logistic", new DiffusingLogisticFactory());
        biologies.put("OSMOSE", new OsmoseBiologyFactory());

        HashMap<String,ExplorationPenaltyProbabilityFactory> explorations = new HashMap<>();
        explorations.put("Explorer", new ExplorationPenaltyProbabilityFactory(.8,1,0d,0d));
        explorations.put("Exploiter", new ExplorationPenaltyProbabilityFactory(.2,1,0d,0d));
        explorations.put("Adaptive", new ExplorationPenaltyProbabilityFactory(.8,1,0.02d,0.01d));

        for(Map.Entry<String,AlgorithmFactory<? extends BiologyInitializer>> biology : biologies.entrySet())
        {
            for(Map.Entry<String,ExplorationPenaltyProbabilityFactory> exploration : explorations.entrySet())
            {
                Log.info("---- Starting Heuristic Check on " + biology.getKey() + " , " + exploration.getKey());
                double totalCatches = 0;
                for(int run =0; run<20;run++)
                {
                    FishYAML yaml = new FishYAML();
                    String scenarioYaml = String.join("\n", Files.readAllLines(
                            INPUT_FOLDER.resolve("optimal_heuristic.yaml")));
                    PrototypeScenario scenario = yaml.loadAs(scenarioYaml,PrototypeScenario.class);
                    ((PerTripImitativeDestinationFactory) scenario.getDestinationStrategy()).setProbability(exploration.getValue());
                    scenario.setBiologyInitializer(biology.getValue());
                    FishState state = new FishState(RANDOM_SEED+run);
                    state.setScenario(scenario);
                    state.start();
                    while(state.getYear()<5)
                    {
                        state.schedule.step(state);
                        totalCatches+=state.getLatestDailyObservation(state.getSpecies().get(0) + " Landings").doubleValue();
                    }
                }
                toOutput = toOutput + biology.getKey() + "," + exploration.getKey() + ", " + (totalCatches/10.0) + "\n";

            }
        }
        Path outputFolder = OUTPUT_FOLDER.resolve("optimal_heuristic");
        outputFolder.toFile().mkdirs();

        Files.write(outputFolder.resolve("heuristic.csv"), toOutput.getBytes());



    }



    public static void hardSwitch(final Path inputPath, final Path outputFile) throws IOException {


        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                inputPath));
        FishState state = new FishState(RANDOM_SEED);


        int firstSpecies = 0;
        int secondSpecies = 1;
        PrototypeScenario scenario = yaml.loadAs(scenarioYaml,PrototypeScenario.class);


        //add special data
        state.registerStartable(new Startable() {
                                    @Override
                                    public void start(FishState model) {
                                        model.getYearlyDataSet().registerGatherer(model.getSpecies().get(firstSpecies)+ " Catchers", state1 -> {
                                            double size = state1.getFishers().size();
                                            if (size == 0)
                                                return Double.NaN;
                                            else {
                                                double total = 0;
                                                for (Fisher fisher1 : state1.getFishers()) {
                                                    if(((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[firstSpecies]>0)
                                                        total ++;

                                                }
                                                return total;
                                            }
                                        }, Double.NaN);


                                        model.getYearlyDataSet().registerGatherer(model.getSpecies().get(secondSpecies) + " Catchers", state1 -> {
                                            double size = state1.getFishers().size();
                                            if (size == 0)
                                                return Double.NaN;
                                            else {
                                                double total = 0;
                                                for (Fisher fisher1 : state1.getFishers()) {
                                                    if(((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[secondSpecies]>0)
                                                        total ++;

                                                }
                                                return total;
                                            }
                                        }, Double.NaN);


                                    }

                                    /**
                                     * tell the startable to turnoff,
                                     */
                                    @Override
                                    public void turnOff() {

                                    }
                                });



        //now work!
        state.setScenario(scenario);
        state.start();
        while(state.getYear() < 45)
            state.schedule.step(state);


        outputFile.toFile().getParentFile().mkdirs();
        FishStateUtilities.printCSVColumnsToFile(outputFile.toFile(),
                                                 state.getYearlyDataSet().getColumn(state.getSpecies().get(firstSpecies)+ " Catchers"),
                                                 state.getYearlyDataSet().getColumn(state.getSpecies().get(secondSpecies)+ " Catchers"),
                                                 state.getYearlyDataSet().getColumn( "Biomass " + state.getSpecies().get(firstSpecies).getName()),
                                                 state.getYearlyDataSet().getColumn( "Biomass " + state.getSpecies().get(secondSpecies).getName()));



    }



    public static void directedTechnologicalChange() throws IOException {
        Log.info("    - Expensive Gas");
        //read and concatenate the YAML
        String expensiveGas = String.join("\n", Files.readAllLines(INPUT_FOLDER.resolve("expensive_gas.yaml")));
        Path output = OUTPUT_FOLDER.resolve("gearopt");
        output.toFile().mkdirs();
        //putting initial scenario back means that the new yaml will override the old one


        for(int i=0; i<10; i++)
        {
            Dashboard.gearEvolutionDashboard(new FishYAML(), expensiveGas, i, "expensive", output,
                                             RANDOM_SEED+i);
        }

        Log.info("    - Free Gas");
        String freeGas = String.join("\n", Files.readAllLines(INPUT_FOLDER.resolve("free_gas.yaml")));

        for(int i=0; i<10; i++)
        {
            Dashboard.gearEvolutionDashboard(new FishYAML(), freeGas, i, "free", output, RANDOM_SEED+i);
        }

        Log.info("    - Cheap Gas");
        String cheapGas = String.join("\n", Files.readAllLines(INPUT_FOLDER.resolve("cheap_gas.yaml")));

        for(int i=0; i<10; i++)
        {
            Dashboard.gearEvolutionDashboard(new FishYAML(), cheapGas, i, "cheap", output, RANDOM_SEED+i);
        }

        Log.info("    - Sensitivity Results");
        String random_gas = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("sensitivity").resolve("gearopt").resolve("random_walk.yaml")));

        output.resolve("sensitivity").toFile().mkdir();
        for(int i=0; i<10; i++)
        {
            Dashboard.gearEvolutionDashboard(new FishYAML(), random_gas, i, "ant",
                                             output.resolve("sensitivity"),
                                             RANDOM_SEED+i);
        }
    }


    public static void catchesPerPolicyMileage() throws IOException {

        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("tac_mileage.yaml")));
        Path outputFolder = OUTPUT_FOLDER.resolve("one_species");
        outputFolder.toFile().mkdirs();
        String mileageOutput = "policy,mileage,catches" + "\n";


        Scenario scenario =  yaml.loadAs(scenarioYaml,Scenario.class);
        FishState state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);
        //skip the first year and do the second one instead (first year ITQ markets are off)
        state.start();
        while(state.getYear()<5)
            state.schedule.step(state);
        state.schedule.step(state);

        for(Fisher fisher : state.getFishers())
        {
            mileageOutput = mileageOutput +
                    "tac," + ((RandomCatchabilityTrawl) fisher.getGear()).getGasPerHourFished() + ","
                    + fisher.getLatestYearlyObservation("Species 0 Landings") + "\n";
        }

        //now do it again for itq!
        yaml = new FishYAML();
        scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("itq_mileage.yaml")));

        scenario =  yaml.loadAs(scenarioYaml,Scenario.class);
        state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);
        //skip the first year and do the second one instead (first year ITQ markets are off)
        state.start();
        while(state.getYear()<5)
            state.schedule.step(state);
        state.schedule.step(state);

        for(Fisher fisher : state.getFishers())
        {
            mileageOutput = mileageOutput +
                    "itq," + ((RandomCatchabilityTrawl) fisher.getGear()).getGasPerHourFished() + ","
                    + fisher.getLatestYearlyObservation("Species 0 Landings") + "\n";
        }



        Files.write(outputFolder.resolve("mileage.csv"), mileageOutput.getBytes());


    }

    public static void catchesPerPolicyCatchability() throws IOException {

        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("tac_catchability.yaml")));
        Path outputFolder = OUTPUT_FOLDER.resolve("one_species");
        outputFolder.toFile().mkdirs();
        String mileageOutput = "policy,catchability,catches" + "\n";


        Scenario scenario =  yaml.loadAs(scenarioYaml,Scenario.class);
        FishState state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);
        //skip the first year and do the second one instead (first year ITQ markets are off)
        state.start();
        while(state.getYear()<5)
            state.schedule.step(state);
        state.schedule.step(state);

        for(Fisher fisher : state.getFishers())
        {
            mileageOutput = mileageOutput +
                    "tac," + ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0] + ","
                    + fisher.getLatestYearlyObservation("Species 0 Landings") + "\n";
        }

        //now do it again for itq!
        yaml = new FishYAML();
        scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("itq_catchability.yaml")));

        scenario =  yaml.loadAs(scenarioYaml,Scenario.class);
        state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);
        //skip the first year and do the second one instead (first year ITQ markets are off)
        state.start();
        while(state.getYear()<5)
            state.schedule.step(state);
        state.schedule.step(state);

        for(Fisher fisher : state.getFishers())
        {
            mileageOutput = mileageOutput +
                    "itq," + ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0]  + ","
                    + fisher.getLatestYearlyObservation("Species 0 Landings") + "\n";
        }



        Files.write(outputFolder.resolve("catchability.csv"), mileageOutput.getBytes());


    }



    private static void policyAndGear(final String policy) throws IOException {

        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                INPUT_FOLDER.resolve("gear_" + policy + ".yaml")));
        Path outputFolder = OUTPUT_FOLDER.resolve("gear");
        outputFolder.toFile().mkdirs();
        PrototypeScenario scenario =  yaml.loadAs(scenarioYaml,PrototypeScenario.class);
        FishState state = new FishState(RANDOM_SEED);
        state.setScenario(scenario);

        //set up the gear adaptation:
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {

                //start collecting red catchability and blue catchability
                model.getYearlyDataSet().registerGatherer("Red Catchability", state1 -> {
                    double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (Fisher fisher1 : state1.getFishers())
                            total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[0]
                                    ;
                        return total / size;
                    }
                }, Double.NaN);


                model.getYearlyDataSet().registerGatherer("Blue Catchability", state1 -> {
                    double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (Fisher fisher1 : state1.getFishers())
                            total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[1]
                                    ;
                        return total / size;
                    }
                }, Double.NaN);


            }

            @Override
            public void turnOff() {

            }
        });


        state.start();

        //output initial distribution of catchability
        FishStateUtilities.pollHistogramToFile(
                state.getFishers(), outputFolder.resolve(policy+"_start_red.csv").toFile(), fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0]
        );

        FishStateUtilities.pollHistogramToFile(
                state.getFishers(), outputFolder.resolve(policy+"_start_blue.csv").toFile(), fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[1]
        );

        //lspiRun for 50 years
        while (state.getYear() < 50) {
            state.schedule.step(state);
            if(state.getYear()>0 && state.getDayOfTheYear()==1)
                FishStateUtilities.pollFishersToFile(state.getFishers(),
                                                     outputFolder.resolve(policy+"_"+state.getYear()+"_obs.csv").toFile(),
                                                     fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0],
                                                     fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[1],
                                                     fisher -> fisher.getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_FLOW_COLUMN)
                );
        }
        state.schedule.step(state);
        FishStateUtilities.pollFishersToFile(state.getFishers(),
                                             outputFolder.resolve(policy+"_"+state.getYear()+"_obs.csv").toFile(),
                                             fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0],
                                             fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[1],
                                             fisher -> fisher.getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_FLOW_COLUMN)
        );
        //final distributions
        FishStateUtilities.pollHistogramToFile(
                state.getFishers(), outputFolder.resolve(policy+"_final_red.csv").toFile(), fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0]
        );

        FishStateUtilities.pollHistogramToFile(
                state.getFishers(), outputFolder.resolve(policy+"_final_blue.csv").toFile(), fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[1]
        );


        //show the effect on catches
        FishStateUtilities.printCSVColumnToFile(outputFolder.resolve(policy+"_red_landings.csv").toFile(),
                                                state.getYearlyDataSet().getColumn(state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
        );

        FishStateUtilities.printCSVColumnToFile(outputFolder.resolve(policy+"_blue_landings.csv").toFile(),
                                                state.getYearlyDataSet().getColumn(state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
        );

        FishStateUtilities.printCSVColumnToFile(outputFolder.resolve("blue_catchability.csv").toFile(),
                                                state.getYearlyDataSet().getColumn("Blue Catchability")
        );
        FishStateUtilities.printCSVColumnToFile(outputFolder.resolve("red_catchability.csv").toFile(),
                                                state.getYearlyDataSet().getColumn("Red Catchability")
        );



        if(policy=="itq")
        {
            FishStateUtilities.printCSVColumnToFile(outputFolder.resolve(policy + "_red_quotas.csv").toFile(),
                                                    state.getDailyDataSet().getColumn(
                                                            "ITQ Last Closing Price Of " + state.getSpecies().get(
                                                                    0).getName())
            );
            FishStateUtilities.printCSVColumnToFile(outputFolder.resolve(policy+"_blue_quotas.csv").toFile(),
                                                    state.getDailyDataSet().getColumn("ITQ Last Closing Price Of " + state.getSpecies().get(1).getName()));

            FishStateUtilities.printCSVColumnToFile(outputFolder.resolve(policy + "_red_quotas.csv").toFile(),
                                                    state.getDailyDataSet().getColumn(
                                                            "ITQ Last Closing Price Of " + state.getSpecies().get(
                                                                    0).getName())
            );
            FishStateUtilities.printCSVColumnToFile(outputFolder.resolve(policy+"_blue_quotas.csv").toFile(),
                                                    state.getDailyDataSet().getColumn("ITQ Last Closing Price Of " + state.getSpecies().get(1).getName()));
        }

    }

    private static void osmoseDemersal2(int numberOfRuns) throws IOException {

        for(int run = 0; run<numberOfRuns; run++)
        {
            Path outputPath = OUTPUT_FOLDER.resolve("osmose");
            outputPath.toFile().mkdirs();

            File runFile = outputPath.resolve("dem2_"+run+".csv").toFile();
            //scenario.setNetworkBuilder(new EmptyNetworkBuilder());

            //create and lspiRun
            FishYAML yaml = new FishYAML();
            String scenarioYaml = String.join("\n", Files.readAllLines(
                    INPUT_FOLDER.resolve("osmose.yaml")));
            PrototypeScenario scenario =  yaml.loadAs(scenarioYaml,PrototypeScenario.class);

            FishState fishState = new FishState(RANDOM_SEED + run);
            fishState.setScenario(scenario);
            fishState.start();


            while(fishState.getYear()< 30 + 1)
                fishState.schedule.step(fishState);

            //print out all biomasses
            YearlyFishStateTimeSeries yearlyData = fishState.getYearlyDataSet();
            DataColumn[] data = new DataColumn[fishState.getSpecies().size()];
            for(int i=0; i<data.length; i++)
            {
                data[i] = yearlyData.getColumn( "Biomass " + fishState.getSpecies().get(i).getName());
            }

            FishStateUtilities.printCSVColumnsToFile(runFile,
                                                     data);
        }

    }




}
