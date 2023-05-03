package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
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

public class NoData718Slice2PriceIncrease {


    private static final int POPULATIONS = 3;

    public static Map<String,String> speciesToSprAgent =
            new HashMap<>(3);
    static {
        speciesToSprAgent.put("Atrobucca brevis","spr_agent3");
        speciesToSprAgent.put("Lutjanus malabaricus","spr_agent2");
        speciesToSprAgent.put("Lethrinus laticaudis","spr_agent1");
        speciesToSprAgent.put("Pristipomoides multidens","spr_agent4");
    }




    private static final  long SEED = 0;

    @NotNull
    public static BatchRunner setupRunner(
            Path scenarioFile,
            final int yearsToRun,
            Path outputFolder, long seed,
            @Nullable
            List<String> additionalColumnsToPrint) {
        ArrayList<String> columnsToPrint = Lists.newArrayList(
                "Actual Average Cash-Flow",
                "Actual Average Hours Out",
                "Full-time fishers",
                "Seasonal fishers",
                "Retired fishers"


        );


        for (String species : NoData718Slice1.validSpecies) {
            final String agent = speciesToSprAgent.get(species);
            Preconditions.checkNotNull(agent, "species has no agent!");
            columnsToPrint.add("SPR " + species + " " + agent);
            columnsToPrint.add("Biomass " + species);
            columnsToPrint.add("Bt/K " + species);
            columnsToPrint.add("Percentage Mature Catches " + species + " "+ agent);
            columnsToPrint.add("Percentage Lopt Catches " + species + " "+ agent);
            columnsToPrint.add(species + " Earnings");
            columnsToPrint.add(species + " Landings");

        }

        for(int i = 0; i< POPULATIONS; i++){
            columnsToPrint.add("Total Hours Out of population"+i);
            columnsToPrint.add("Seasonal fishers of population"+i);
            columnsToPrint.add("Retired fishers of population"+i);
            columnsToPrint.add("Full-time fishers of population"+i);
            columnsToPrint.add("Total Landings of population"+i);
            columnsToPrint.add("Actual Average Cash-Flow of population"+i);
            columnsToPrint.add("Average Number of Trips of population"+i);
            columnsToPrint.add("Number Of Active Fishers of population"+i);
            columnsToPrint.add("Average Distance From Port of population"+i);
            columnsToPrint.add("Average Trip Duration of population"+i);
            for (String species : NoData718Slice1.validSpecies) {
                columnsToPrint.add(species+ " Landings of population" + i);
            }
            columnsToPrint.add("Others Landings of population" + i);

            columnsToPrint.add("Actual Average Distance From Port of population"+i);
            columnsToPrint.add("Actual Average Variable Costs of population"+i);
            columnsToPrint.add("Total Variable Costs of population"+i);
            columnsToPrint.add("Total Hours Out of population"+i);
        }


        if(additionalColumnsToPrint != null)
            columnsToPrint.addAll(additionalColumnsToPrint);

        return new BatchRunner(
                scenarioFile,
                yearsToRun,
                columnsToPrint,
                outputFolder,
                null,
                seed,
                -1
        );
    }


    private static Path OUTPUT_FOLDER =
                NoData718Slice2.MAIN_DIRECTORY.resolve("price_test_success");


    static final private double newCroakerPrice = 350000; //glaudy's report

    /**
     * give me a year and I will give you a policy
     */
    static private LinkedHashMap<String,
            Function<Integer, Consumer<Scenario>>> policies = new LinkedHashMap();

    static {

        policies.put(
                "BAU",
                shockYear -> scenario -> {
                }

        );


        policies.put(
                "Price Shock",
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        priceIncreaseEvent(shockYear)
                                );
                            }
                        };


                    }
                }

        );

        policies.put(
                "Price Shock minus 2",
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        priceIncreaseEvent(shockYear-2)
                                );
                            }
                        };


                    }
                }

        );

        policies.put(
                "Price Shock minus 5",
                new Function<Integer, Consumer<Scenario>>() {
                    @Override
                    public Consumer<Scenario> apply(Integer shockYear) {

                        return new Consumer<Scenario>() {
                            @Override
                            public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        priceIncreaseEvent(shockYear-5)
                                );
                            }
                        };


                    }
                }

        );
    }

    @NotNull
    public static AlgorithmFactory<AdditionalStartable> priceIncreaseEvent(Integer shockYear) {
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
                                            //assuming here all fishers get the same treatment
                                            ((FixedPriceMarket) port.getMarketMap(null).getMarket(
                                                    model.getBiology().getSpecie("Atrobucca brevis")
                                            )).setPrice(newCroakerPrice);
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


        public static void main(String[] args) throws IOException {

        CSVReader reader = new CSVReader(new FileReader(
                OUTPUT_FOLDER.getParent().resolve("success.csv").toFile()
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
                    policies
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


            BatchRunner runner = setupRunner(scenarioFile, shockYear+5, outputFolder, SEED, null);

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


}
