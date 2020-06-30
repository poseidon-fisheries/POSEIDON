package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.base.Preconditions;
import com.opencsv.CSVReader;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.Market;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoData718Slice6Policy {

    public static final String CANDIDATES_CSV_FILE = "total_successes.csv";
    public static final int SEED = 0;
    private static Path OUTPUT_FOLDER =
            NoData718Slice6.MAIN_DIRECTORY.resolve("outputs");



    static private LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> policies = new LinkedHashMap();


    private static Function<Integer,Consumer<Scenario>> decreasePricesForAllSpeciesByAPercentage(double taxRate) {

        return new Function<Integer, Consumer<Scenario>>() {
            public Consumer<Scenario> apply(Integer shockYear) {


                return new Consumer<Scenario>() {

                    @Override
                    public void accept(Scenario scenario) {

                        ((FlexibleScenario) scenario).getPlugins().add(
                                new AlgorithmFactory<AdditionalStartable>() {
                                    @Override
                                    public AdditionalStartable apply(FishState state) {

                                        return new AdditionalStartable() {
                                            @Override
                                            public void start(FishState model) {

                                                model.scheduleOnceAtTheBeginningOfYear(
                                                        new Steppable() {
                                                            @Override
                                                            public void step(SimState simState) {

                                                                //shock the prices
                                                                for (Port port : ((FishState) simState).getPorts()) {
                                                                    for (Market market : port.getDefaultMarketMap().getMarkets()) {

                                                                        if(port.getName().equals("Port 0")) {
                                                                            final FixedPriceMarket delegate = (FixedPriceMarket) ((MarketProxy) market).getDelegate();
                                                                            delegate.setPrice(
                                                                                    delegate.getPrice() * (1 - taxRate)
                                                                            );
                                                                        }
                                                                        else {

                                                                            final FixedPriceMarket delegate = ((FixedPriceMarket) ((MarketProxy) ((MarketProxy) market).getDelegate()).getDelegate());
                                                                            delegate.setPrice(
                                                                                    delegate.getPrice() * (1 - taxRate)
                                                                            );
                                                                        }
                                                                    }
                                                                }

                                                            }
                                                        }, StepOrder.DAWN, shockYear);

                                            }
                                        };


                                    }
                                });


                    }
                };
            }

            ;

        };
    }



    static {

        policies.put(
                "BAU",
                shockYear -> scenario -> {
                }

        );


        policies.put(
                "noentry",
                shockYear -> NoDataPolicy.removeEntry(shockYear)

        );


        for(int days = 250; days>100; days-=10) {
            int finalDays = days;
            policies.put(
                    days+"_days_noentry",
                    shockYear -> NoDataPolicy.buildMaxDaysRegulation(shockYear,
                            new String[]{"population0", "population1", "population2"}
                            , finalDays).andThen(
                            NoDataPolicy.removeEntry(shockYear)
                    )

            );
        }

        policies.put(
                "tax_20",
                shockYear -> NoDataPolicy.removeEntry(shockYear).andThen(
                        decreasePricesForAllSpeciesByAPercentage(.2d).apply(shockYear)
                )

        );


    }




    public static void main(String[] args) throws IOException {

        CSVReader reader = new CSVReader(new FileReader(
                OUTPUT_FOLDER.getParent().resolve(CANDIDATES_CSV_FILE).toFile()
        ));

        List<String[]> strings = reader.readAll();
        for (int i = 1; i < strings.size(); i++) {

            String[] row = strings.get(i);
            runOnePolicySimulation(
                    Paths.get(row[0]),
                    Integer.parseInt(row[1]),
                    Integer.parseInt(row[2])
            );
        }


    }


    private static void runOnePolicySimulation(Path scenarioFile,
                                               int yearOfPriceShock,
                                               int yearOfPolicyShock) throws IOException {


        Preconditions.checkArgument(yearOfPolicyShock>yearOfPriceShock);

        String filename =      scenarioFile.toAbsolutePath().toString().replace('/','$');

        System.out.println(filename);
        if(OUTPUT_FOLDER.resolve(filename + ".csv").toFile().exists())
        {
            System.out.println(filename + " already exists!");
            return;

        }


        FileWriter fileWriter = new FileWriter(OUTPUT_FOLDER.resolve(filename + ".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for (Map.Entry<String, Function<Integer, Consumer<Scenario>>> policyRun : policies.entrySet()) {
            String policyName = policyRun.getKey();

            //add the price shock
            final Consumer<Scenario> priceShockConsumer =
                    NoData718Slice4PriceIncrease.priceShockAndSeedingGenerator(0).apply(yearOfPriceShock);

            //add policy!
            final Consumer<Scenario> totalConsumer = priceShockConsumer.andThen(
                    policyRun.getValue().apply(yearOfPolicyShock)
            ).andThen(
                    //collect full-time vs part-time stuff
                    scenario -> ((FlexibleScenario) scenario).getPlugins().add(
                            new FullSeasonalRetiredDataCollectorsFactory()
                    )
            );;

            BatchRunner runner =  NoData718Slice2PriceIncrease.setupRunner(scenarioFile,
                    yearOfPolicyShock+15, null, SEED, null);

            //give it the scenario
            runner.setScenarioSetup(totalConsumer);

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

}
