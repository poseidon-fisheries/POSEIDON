package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.base.Preconditions;
import com.opencsv.CSVReader;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.plugins.FullSeasonalRetiredDataCollectorsFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoData718Slice4PriceIncrease {

    private static final String CANDIDATES_CSV_FILE = "price_shock_candidates_max.csv";
    private static Path OUTPUT_FOLDER =
            NoData718Slice4.MAIN_DIRECTORY.resolve("price_shock_max");


    static final public double newCroakerPriceDobo = 80000; //glaudy's report

    static final public double newCroakerPriceProbolinggo = 350000; //glaudy's report

    static private LinkedHashMap<String,
            Function<Integer, Consumer<Scenario>>> priceIncreasePolicies = new LinkedHashMap();


    /**
     * adds some longline boats in the far-off port; useful because they might have quit before the price shock;
     * assuming POPULATION0 is the faroff one
     * @param shockYear
     * @return
     */
    @NotNull
    public static AlgorithmFactory<AdditionalStartable> farOffPortsSeedingEvent(Integer shockYear,
                                                                           final int minPopulation0Boats) {
        return new AlgorithmFactory<AdditionalStartable>() {
            @Override
            public AdditionalStartable apply(FishState fishState) {
                return new AdditionalStartable() {
                    @Override
                    public void start(FishState model) {
                        model.scheduleOnceAtTheBeginningOfYear(
                                new Steppable() {
                                    @Override
                                    public void step(SimState simState) {
                                        int currentNumber = (((FishState) simState).
                                                getLatestYearlyObservation("Number Of Active Fishers of population0")).intValue();
                                        int toAdd = minPopulation0Boats - currentNumber;
                                        System.out.println("adding " + toAdd + " far-off fishers");
                                        for (int i = 0; i < toAdd; i++) {
                                            ((FishState) simState).getFisherFactory("population0").buildFisher(((FishState) simState));

                                        }

                                    }
                                },
                                StepOrder.DAWN,
                                shockYear
                        );
                    }
                };


            }
        };
    }


    static {


        priceIncreasePolicies.put(
                "Price Shock plus seeding",
                priceShockAndSeedingGenerator(0)

        );
//        priceIncreasePolicies.put(
//                "Price Shock lag 2 plus seeding",
//                priceShockAndSeedingGenerator(2)
//
//        );
//        priceIncreasePolicies.put(
//                "Price Shock lag 5 plus seeding",
//                priceShockAndSeedingGenerator(5)
//
//        );


        priceIncreasePolicies.put(
                "BAU",
                shockYear -> scenario -> {
                }

        );

    }

    @NotNull
    public static Function<Integer, Consumer<Scenario>> priceShockAndSeedingGenerator(final int lead) {
        return new Function<Integer, Consumer<Scenario>>() {
            @Override
            public Consumer<Scenario> apply(Integer shockYear) {

                return new Consumer<Scenario>() {
                    @Override
                    public void accept(Scenario scenario) {
                        Preconditions.checkNotNull(scenario);
                        Preconditions.checkNotNull(((FlexibleScenario) scenario).getPlugins());
                        ((FlexibleScenario) scenario).getPlugins().add(
                                NoData718Slice3PriceIncrease.priceIncreaseEvent(shockYear- lead,
                                        newCroakerPriceDobo,
                                        newCroakerPriceProbolinggo)
                        );
                        ((FlexibleScenario) scenario).getPlugins().add(
                                farOffPortsSeedingEvent(shockYear- lead,10)
                        );
                    }
                };


            }
        };
    }


    public static void priceIncreaseOneRun(Path scenarioFile, int shockYear,
                                           Path outputFolder,
                                           LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> policyMap,
                                           List<String> additionalColumnsToPrint,
                                           boolean printYAMLScenario, Consumer<Scenario>... additionalPolicies) throws IOException {

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

        for (Map.Entry<String, Function<Integer, Consumer<Scenario>>> policyRun : policyMap.entrySet()) {
            String policyName = policyRun.getKey();
            //add some information gathering
            Consumer<Scenario> policy = policyRun.getValue().apply(shockYear).andThen(
                    new Consumer<Scenario>() {
                        @Override
                        public void accept(Scenario scenario) {
                            ((FlexibleScenario) scenario).getPlugins().add(
                                    new FullSeasonalRetiredDataCollectorsFactory()
                            );
                        }
                    }
            );

            for (Consumer<Scenario> additionalPolicy : additionalPolicies) {
                policy = policy.andThen(additionalPolicy);
            }


            BatchRunner runner = NoData718Slice2PriceIncrease.setupRunner(scenarioFile, shockYear+5, null,SEED, additionalColumnsToPrint);

            //give it the scenario
            runner.setScenarioSetup(policy);

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


    private static final  long SEED = 0;



    public static void main(String[] args) throws IOException {

        CSVReader reader = new CSVReader(new FileReader(
                OUTPUT_FOLDER.getParent().resolve(CANDIDATES_CSV_FILE).toFile()
        ));

        List<String[]> strings = reader.readAll();
        strings.remove(0);
        Collections.shuffle(strings);

        for (int i = 1; i < strings.size(); i++) {

            String[] row = strings.get(i);
            System.out.println(Arrays.toString(row));

            final Path scenarioPath = Paths.get(row[0]);
            if(Files.exists(scenarioPath))
            {

                priceIncreaseOneRun(
                        scenarioPath,
                        Integer.parseInt(row[1]),
                        OUTPUT_FOLDER,
                        priceIncreasePolicies, null,
                        false);
            }
            else {
                System.err.println("Couldn't find scenario " + scenarioPath);
            }
        }


    }



}
