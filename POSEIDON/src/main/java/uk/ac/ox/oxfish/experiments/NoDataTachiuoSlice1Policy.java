package uk.ac.ox.oxfish.experiments;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import ec.util.MersenneTwisterFast;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.experiments.indonesia.limited.*;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HeterogeneousGearFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.SimpleLogisticGearFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.MonthlyDepartingDecorator;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.plugins.FullSeasonalRetiredDataCollectorsFactory;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.regs.TemporaryRegulation;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Season;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class NoDataTachiuoSlice1Policy {

    public static final String CANDIDATES_CSV_FILE = "slice3_spr_opt.csv";
    public static final int SEED = 0;
    public static final int YEARS_FROM_POLICY_TO_RUN = 15;
    private static Path OUTPUT_FOLDER =
            Paths.get("docs", "20200425 abc_example","slice4").resolve("outputs_opt");



    static private LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> policies = new LinkedHashMap();



    static {





        policies.put(
                "BAU",
                integer -> scenario -> {
                }


        );
//
//        policies.put(
//                "noentry",
//                shockYear -> NoDataPolicy.removeEntry(shockYear)
//
//
//        );
//
//
//        policies.put(
//                "hard_seasonal_closure",
//                shockYear -> NoDataPolicy.removeEntry(shockYear).andThen(
//                        seasonalClosure(shockYear, 200)
//                )
//
//        );
//
//        policies.put(
//                "spawning_protection",
//                shockYear -> NoDataPolicy.removeEntry(shockYear).andThen(
//                        new Consumer<Scenario>() {
//                            @Override
//                            public void accept(Scenario scenario) {
//
//                                TemporaryRegulation spawningClosure =
//                                        new TemporaryRegulation(
//                                                new Predicate<Integer>() {
//                                                    @Override
//                                                    public boolean test(Integer dayOfYear) {
//                                                        return (Season.getMonth(dayOfYear) == 5 ||
//                                                                Season.getMonth(dayOfYear) == 10
//                                                        );
//                                                    }
//                                                },
//                                                new FishingSeason(true,0)
//                                        );
//
//                                ((FlexibleScenario) scenario).getPlugins().add(
//                                        new AlgorithmFactory<AdditionalStartable>() {
//                                            @Override
//                                            public AdditionalStartable apply(FishState fishState) {
//
//                                                return new AdditionalStartable() {
//                                                    @Override
//                                                    public void start(FishState model) {
//                                                        model.scheduleOnceAtTheBeginningOfYear(
//                                                                new Steppable() {
//                                                                    @Override
//                                                                    public void step(SimState simState) {
//
//                                                                        for (Fisher fisher : ((FishState) simState).getFishers()) {
//
//                                                                            fisher.setRegulation(spawningClosure);
//                                                                        }
//
//
//                                                                    }
//                                                                },
//                                                                StepOrder.DAWN,
//                                                                shockYear
//                                                        );
//
//
//                                                    }
//                                                };
//                                            }
//                                        }
//                                );
//                            }
//                        }
//                )
//
//        );
//
//        policies.put(
//                "seasonal_closure",
//                shockYear -> NoDataPolicy.removeEntry(shockYear).andThen(
//                        seasonalClosure(shockYear, 300)
//                )
//
//        );
//
//
//
//
//        policies.put(
//                "selectivity_one",
//                shockYear -> NoDataPolicy.removeEntry(shockYear).andThen(
//                        gearGeneration(shockYear, 1, 0.9)
//                )
//
//        );
//
//        policies.put(
//                "selectivity_two",
//                shockYear -> NoDataPolicy.removeEntry(shockYear).andThen(
//                        gearGeneration(shockYear, 1.1, 0.9)
//                )
//
//        );
//        policies.put(
//                "selectivity_three",
//                shockYear -> NoDataPolicy.removeEntry(shockYear).andThen(
//                        gearGeneration(shockYear, 1.2, 0.8)
//                )
//        );

    }

    @NotNull
    private static Consumer<Scenario> gearGeneration(Integer shockYear, final double selex1Multiplier,
                                                     final double selex2Multiplier) {
        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario scenario) {
                final HeterogeneousGearFactory gear =
                        (HeterogeneousGearFactory) ((FlexibleScenario) scenario).getFisherDefinitions().get(0).getGear();
                //make a copy through YAML
                final FishYAML yaml = new FishYAML();
                final HeterogeneousGearFactory futureGear = yaml.loadAs(yaml.dump(gear), HeterogeneousGearFactory.class);
                final SimpleLogisticGearFactory タチウオ = (SimpleLogisticGearFactory) futureGear.getGears().get("タチウオ");
                タチウオ.setSelexParameter2(
                        new FixedDoubleParameter(
                                タチウオ.getSelexParameter2().applyAsDouble(new MersenneTwisterFast()) * selex2Multiplier));
                タチウオ.setSelexParameter1(
                        new FixedDoubleParameter(
                                タチウオ.getSelexParameter1().applyAsDouble(new MersenneTwisterFast()) * selex1Multiplier));



                ((FlexibleScenario) scenario).getPlugins().add(
                        new AlgorithmFactory<AdditionalStartable>() {
                            @Override
                            public AdditionalStartable apply(FishState fishState) {

                                return new AdditionalStartable() {
                                    @Override
                                    public void start(FishState model) {
                                        model.scheduleOnceAtTheBeginningOfYear(
                                                new Steppable() {
                                                    @Override
                                                    public void step(SimState simState) {

                                                        for (Fisher fisher : ((FishState) simState).getFishers()) {

                                                            fisher.setGear(
                                                                    futureGear.apply(((FishState) simState))
                                                            );

                                                        }


                                                    }
                                                },
                                                StepOrder.DAWN,
                                                shockYear
                                        );


                                    }
                                };
                            }
                        }
                );
            }
        };
    }

    @NotNull
    private static Consumer<Scenario> seasonalClosure(Integer shockYear, final int daysOpened) {
        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario scenario) {
                ((FlexibleScenario) scenario).getPlugins().add(
                        new AlgorithmFactory<AdditionalStartable>() {
                            @Override
                            public AdditionalStartable apply(FishState fishState) {

                                return new AdditionalStartable() {
                                    @Override
                                    public void start(FishState model) {
                                        model.scheduleOnceAtTheBeginningOfYear(
                                                new Steppable() {
                                                    @Override
                                                    public void step(SimState simState) {

                                                        for (Fisher fisher : ((FishState) simState).getFishers()) {
                                                            fisher.setRegulation(
                                                                    new FishingSeason(true, daysOpened)
                                                            );
                                                        }


                                                    }
                                                },
                                                StepOrder.DAWN,
                                                shockYear
                                        );


                                    }
                                };
                            }
                        }
                );
            }
        };
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
                    Integer.parseInt(row[1])
            );
        }


    }


    private static void runOnePolicySimulation(Path scenarioFile,
                                               int yearOfPolicyShock) throws IOException {

        if(!Files.exists(scenarioFile))
        {
            System.err.println(scenarioFile+ " does not exist!");
            return;
        }

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

            //add policy!
            final Consumer<Scenario> totalConsumer =
                    policyRun.getValue().apply(yearOfPolicyShock)
                            .andThen(
                                    //collect full-time vs part-time stuff
                                    scenario -> ((FlexibleScenario) scenario).getPlugins().add(
                                            new FullSeasonalRetiredDataCollectorsFactory()
                                    )
                            );;

            BatchRunner runner =  setupRunner(scenarioFile,
                    yearOfPolicyShock+ YEARS_FROM_POLICY_TO_RUN,
                    null, SEED,
                    null);

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






    @NotNull
    public static BatchRunner setupRunner(
            Path scenarioFile,
            final int yearsToRun,
            Path outputFolder, long seed,
            @Nullable
                    List<String> additionalColumnsToPrint) {
        ArrayList<String> columnsToPrint = Lists.newArrayList(
                "SPR タチウオ spr_agent"
                ,"タチウオ Landings"
                ,"Total Hours Out of population0"
                ,"Number Of Active Fishers"
                ,"Actual Average Cash-Flow"
                ,"Exogenous catches of タチウオ"
                ,"Biomass タチウオ",
                "Actual Average Cash Balance",
                "タチウオ" + " " + AbstractMarket.EARNINGS_COLUMN_NAME,
                "Average Trip Earnings",
                "Average Trip Variable Costs",
                //"Average Hours Out",
                "Actual Average Hours Out",
                "Total Effort",
                "Actual Median Trip Profits",
                "Total Number of Trips"



                );


        columnsToPrint.add("Bt/K " + "タチウオ");
        columnsToPrint.add("タチウオ" + " Recruits");
        columnsToPrint.add("Percentage Mature Catches " + "タチウオ" + " "+ "spr_agent");
        columnsToPrint.add("Percentage Lopt Catches "  + "タチウオ" + " "+ "spr_agent");



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
}


