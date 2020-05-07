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

public class NoData718Slice3PriceIncrease {


    public static final String CANDIDATES_CSV_FILE = "price_shock_candidates_max_highspr.csv";
    private static Path OUTPUT_FOLDER =
            NoData718Slice3.MAIN_DIRECTORY.resolve("price_shock_max_highspr");


    static final private double newCroakerPriceDobo = 80000; //glaudy's report

    static final private double newCroakerPriceProbolinggo = 350000; //glaudy's report

    static private LinkedHashMap<String,
            Function<Integer, Consumer<Scenario>>> priceIncreasePolicies = new LinkedHashMap();

    static {


        priceIncreasePolicies.put(
                "Price Shock",
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        priceIncreaseEvent(shockYear, newCroakerPriceDobo, newCroakerPriceProbolinggo)
                                );
                            }
                        };


                    }
                }

        );

        priceIncreasePolicies.put(
                "Price Shock from 0",
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        priceIncreaseEvent(shockYear, newCroakerPriceDobo, newCroakerPriceProbolinggo)
                                );
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        zeroPriceEvent()
                                );
                            }
                        };


                    }
                }

        );


        priceIncreasePolicies.put(
                "Price Shock minus 2",
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        priceIncreaseEvent(shockYear-2, newCroakerPriceDobo, newCroakerPriceProbolinggo)
                                );
                            }
                        };


                    }
                }

        );
        priceIncreasePolicies.put(
                "Price Shock minus 2 from 0",
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        priceIncreaseEvent(shockYear-2, newCroakerPriceDobo, newCroakerPriceProbolinggo)
                                );
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        zeroPriceEvent()
                                );
                            }
                        };


                    }
                }

        );

        priceIncreasePolicies.put(
                "Price Shock minus 5",
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        priceIncreaseEvent(shockYear-5, newCroakerPriceDobo, newCroakerPriceProbolinggo)
                                );
                            }
                        };


                    }
                }

        );
        priceIncreasePolicies.put(
                "Price Shock minus 5 from 0",
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        priceIncreaseEvent(shockYear-5, newCroakerPriceDobo, newCroakerPriceProbolinggo)
                                );
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        zeroPriceEvent()
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


    @NotNull
    public static AlgorithmFactory<AdditionalStartable> priceIncreaseEvent(Integer shockYear,
                                                                           final double newCroakerPriceDobo, final double newCroakerPriceProbolinggo) {
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
                                        for (Port port : model.getPorts()) {
                                            System.out.println(port.getName());
                                            //assuming here all fishers get the same treatment
                                            if(port.getName() == "Port 0") {
                                                ((FixedPriceMarket) ((MarketProxy) port.getMarketMap(null).getMarket(
                                                        model.getBiology().getSpecie("Atrobucca brevis")
                                                )).getDelegate()).setPrice(newCroakerPriceDobo);
                                            }
                                            else{


                                                //look at this piece of code and weep!
                                                ((FixedPriceMarket) ((MarketProxy) ((MarketProxy) port.getMarketMap(null).getMarket(
                                                        model.getBiology().getSpecie("Atrobucca brevis")
                                                )).getDelegate()).getDelegate()).setPrice(newCroakerPriceProbolinggo);
                                            }
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


    @NotNull
    public static AlgorithmFactory<AdditionalStartable> zeroPriceEvent() {
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
                                        for (Port port : model.getPorts()) {
                                            System.out.println(port.getName());
                                            //assuming here all fishers get the same treatment
                                            if(port.getName() == "Port 0") {
                                                ((FixedPriceMarket) port.getMarketMap(null).getMarket(
                                                        model.getBiology().getSpecie("Atrobucca brevis")
                                                )).setPrice(0);
                                            }
                                            else{
                                                ((FixedPriceMarket) ((MarketProxy) port.getMarketMap(null).getMarket(
                                                        model.getBiology().getSpecie("Atrobucca brevis")
                                                )).getDelegate()).setPrice(0);
                                            }
                                        }
                                    }
                                },
                                StepOrder.DAWN,
                                1
                        );
                    }
                };


            }
        };
    }

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


            BatchRunner runner = NoData718Slice2PriceIncrease.setupRunner(scenarioFile, shockYear+5, null,SEED, null);

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



}
