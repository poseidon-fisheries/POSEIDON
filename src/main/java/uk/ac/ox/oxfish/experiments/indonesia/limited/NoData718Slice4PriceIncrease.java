package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.opencsv.CSVReader;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketProxy;
import uk.ac.ox.oxfish.model.plugins.FullSeasonalRetiredDataCollectorsFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoData718Slice4PriceIncrease {

    public static final String CANDIDATES_CSV_FILE = "price_shock_candidates_max.csv";
    private static Path OUTPUT_FOLDER =
            NoData718Slice4.MAIN_DIRECTORY.resolve("price_shock_max");


    static final private double newCroakerPriceDobo = 80000; //glaudy's report

    static final private double newCroakerPriceProbolinggo = 350000; //glaudy's report

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
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        NoData718Slice3PriceIncrease.priceIncreaseEvent(shockYear,
                                                newCroakerPriceDobo,
                                                newCroakerPriceProbolinggo)
                                );
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        farOffPortsSeedingEvent(shockYear,10)
                                );
                            }
                        };


                    }
                }

        );
        priceIncreasePolicies.put(
                "Price Shock lag 2 plus seeding",
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        NoData718Slice3PriceIncrease.priceIncreaseEvent(shockYear-2,
                                                newCroakerPriceDobo,
                                                newCroakerPriceProbolinggo)
                                );
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        farOffPortsSeedingEvent(shockYear-2,10)
                                );
                            }
                        };


                    }
                }

        );
        priceIncreasePolicies.put(
                "Price Shock lag 5 plus seeding",
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        NoData718Slice3PriceIncrease.priceIncreaseEvent(shockYear-5,
                                                newCroakerPriceDobo,
                                                newCroakerPriceProbolinggo)
                                );
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        farOffPortsSeedingEvent(shockYear-5,10)
                                );
                            }
                        };


                    }
                }

        );


        priceIncreasePolicies.put(
                "BAU",
                shockYear -> scenario -> {
                }

        );

    }



    public static void sensitivity(Path scenarioFile, int shockYear,
                                   Path outputFolder,
                                   LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> policyMap) throws IOException {

        String filename =      scenarioFile.toAbsolutePath().toString().replace('/','$');

        System.out.println(filename);
        if(outputFolder.resolve(filename + ".csv").toFile().exists())
        {
            System.out.println(filename + " already exists!");
            return;

        }


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


            BatchRunner runner = NoData718Slice2PriceIncrease.setupRunner(scenarioFile, shockYear+5, null,SEED);

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
            sensitivity(
                    Paths.get(row[0]),
                    Integer.parseInt(row[1]),
                    OUTPUT_FOLDER,
                    priceIncreasePolicies
            );
        }


    }



}
