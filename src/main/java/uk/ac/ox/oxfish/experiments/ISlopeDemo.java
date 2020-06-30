package uk.ac.ox.oxfish.experiments;

import com.google.common.collect.Lists;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.*;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.HerfindalndexCollectorFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.IndexTargetController;
import uk.ac.ox.oxfish.model.regs.policymakers.PIDControllerIndicatorTarget;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.*;
import uk.ac.ox.oxfish.model.regs.policymakers.TargetToTACController;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoublePredicate;
import java.util.function.ToDoubleFunction;

public class ISlopeDemo {


    public static final Path DIRECTORY = Paths.get("docs",
            "20200604 islope");
    public static final int RUNS_TO_RUN = 100;
    public static final int FIRST_CONTROL_YEAR = 10;
    private static String[] indicatorsToUse = new String[]{
            "Average Income per Hour Out",
           // "Effort Herfindal" ,
            "Species 0 CPHO",
            "Species 0 CPUE",
            "Average Trip Income",
            "Number Of Active Fishers",
            "Biomass Species 0",
          //  "Average Trip Duration"
    };

    private static LinkedHashMap<String,Double> pidMultipliers =
            new LinkedHashMap<>();
    static  {
        pidMultipliers.put("Average Income per Hour Out",1d);
     //   pidMultipliers.put("Effort Herfindal",1d);
        pidMultipliers.put("Species 0 CPHO",1d);
        pidMultipliers.put("Species 0 CPUE",1d);
        pidMultipliers.put("Biomass Species 0",1d);
        pidMultipliers.put("Average Trip Income",1d);
   //     pidMultipliers.put("Average Trip Duration",0.9d);
        pidMultipliers.put("Number Of Active Fishers",1d);
    }

    private static List<String> columnsToPrint = Lists.newArrayList(
            "Species 0 CPHO",
          //  "Effort Herfindal",
            "Species 0 CPUE",
            "Average Trip Income",
            "Average Trip Duration",
            "Species 0 Landings",
            "Biomass Species 0",
            "TAC from TARGET-TAC Controller",
            "Average Income per Hour Out",
            "Number Of Active Fishers",
            "Average Cash-Flow");



    public static void main(String[] args) throws IOException {
      //  mainIT1();

    //      mainPID(false);
        //  mainPID(true);
        mainStockAssessmentFormula(false);
        //    mainStockAssessmentFormula(true);

        //mainSampling();
   //     effortTest();
    //    fishingFrontExample();
    }

    public static void  mainITarget(String[] args) throws IOException {

        FileWriter fileWriter = new FileWriter(
                DIRECTORY.resolve("indicators_itarget.csv").toFile());
        fileWriter.write("run,year,indicator,variable,value\n");
        fileWriter.flush();



        for (String indicator : indicatorsToUse) {


            BatchRunner runner = new BatchRunner(
                    DIRECTORY.resolve(
                            "base_islope_2.yaml"),
                    40,
                    columnsToPrint,
                    null,
                    null,
                    0,
                    -1
            );
            runner.setScenarioSetup(new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {
                    final PrototypeScenario prototype = (PrototypeScenario) scenario;
                    prototype.setFishers(200);
                    prototype.getPlugins().add(
                            new AlgorithmFactory<AdditionalStartable>() {
                                @Override
                                public AdditionalStartable apply(FishState fishState) {
                                    return new AdditionalStartable() {
                                        @Override
                                        public void start(FishState model) {
                                            fishState.scheduleOnceInXDays(
                                                    new Steppable() {
                                                        @Override
                                                        public void step(SimState simState) {
                                                            TargetToTACController controller = new TargetToTACController(
                                                                    new ITarget(
                                                                            "Species 0 Landings",
                                                                            indicator,
                                                                            0,
                                                                            1.5,
                                                                            5, 5 * 2
                                                                    )
                                                            );
                                                            controller.start(model);
                                                            controller.step(model);
                                                        }
                                                    },
                                                    StepOrder.DAWN,
                                                    1+365 * 10
                                            );
                                        }
                                    };

                                }
                            }


                    );

                }
            });


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.
                            append(indicator).append(",");
                }
            });

            for (int run = 0; run < RUNS_TO_RUN; run++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();


            }


        }

    }





    public static void  mainIslope(String[] args) throws IOException {

        FileWriter fileWriter = new FileWriter(
                DIRECTORY.resolve("indicators.csv").toFile());
        fileWriter.write("run,year,indicator,variable,value\n");
        fileWriter.flush();



        for (String indicator : indicatorsToUse) {


            BatchRunner runner = new BatchRunner(
                    DIRECTORY.resolve(
                            "base_islope_2.yaml"),
                    30,
                    columnsToPrint,
                    null,
                    null,
                    0,
                    -1
            );
            runner.setScenarioSetup(new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {
                    final PrototypeScenario prototype = (PrototypeScenario) scenario;
                    prototype.setFishers(200);
                    prototype.getPlugins().add(
                            new HerfindalndexCollectorFactory()
                    );
                    prototype.getPlugins().add(
                            new AlgorithmFactory<AdditionalStartable>() {
                                @Override
                                public AdditionalStartable apply(FishState fishState) {
                                    return new AdditionalStartable() {
                                        @Override
                                        public void start(FishState model) {
                                            fishState.scheduleOnceInXDays(
                                                    new Steppable() {
                                                        @Override
                                                        public void step(SimState simState) {
                                                            TargetToTACController controller = new TargetToTACController(
                                                                    new ISlope(
                                                                            "Species 0 Landings",
                                                                            indicator,
                                                                            0.4,
                                                                            0.8,
                                                                            2
                                                                    )
                                                            );
                                                            controller.start(model);
                                                            controller.step(model);
                                                        }
                                                    },
                                                    StepOrder.DAWN,
                                                    365 * FIRST_CONTROL_YEAR
                                            );
                                        }
                                    };

                                }
                            }


                    );

                }
            });


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.
                            append(indicator).append(",");
                }
            });

            for (int run = 0; run < RUNS_TO_RUN; run++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();


            }


        }

    }



    public static void  mainIT1() throws IOException {

        FileWriter fileWriter = new FileWriter(
                DIRECTORY.resolve("indicators_it1.csv").toFile());
        fileWriter.write("run,year,indicator,variable,value\n");
        fileWriter.flush();




        for (Map.Entry<String, Double> indicator : pidMultipliers.entrySet()) {


            BatchRunner runner = new BatchRunner(
                    DIRECTORY.resolve(
                            "base_islope_2.yaml"),
                    30,
                    columnsToPrint,
                    null,
                    null,
                    0,
                    -1
            );
            runner.setScenarioSetup(new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {
                    final PrototypeScenario prototype = (PrototypeScenario) scenario;
                    prototype.setFishers(200);
                    prototype.getPlugins().add(
                            new HerfindalndexCollectorFactory()
                    );

                    prototype.getPlugins().add(
                            new AlgorithmFactory<AdditionalStartable>() {
                                @Override
                                public AdditionalStartable apply(FishState fishState) {
                                    return new AdditionalStartable() {
                                        @Override
                                        public void start(FishState model) {
                                            fishState.scheduleOnceInXDays(
                                                    new Steppable() {
                                                        @Override
                                                        public void step(SimState simState) {
                                                            IndexTargetController controller =
                                                                    new IndexTargetController(
                                                                            new PastAverageSensor(
                                                                                    indicator.getKey(),
                                                                                    1
                                                                            ),
                                                                            new FixedTargetAsMultipleOfOriginalObservation(
                                                                                    indicator.getKey(),
                                                                                    indicator.getValue(),
                                                                                    5
                                                                            ),
                                                                            IndexTargetController.RATIO_TO_TAC(
                                                                                    new PastAverageSensor(
                                                                                            "Species 0 Landings",
                                                                                            1
                                                                                    ),10000,
                                                                                    9999999d
                                                                            ),
                                                                            365,
                                                                            .1,
                                                                            indicator.getValue()<1,


                                                                            true);

                                                            controller.start(model);
                                                            controller.step(model);
                                                            model.getYearlyDataSet().registerGatherer("TAC from TARGET-TAC Controller",
                                                                    new Gatherer<FishState>() {
                                                                        @Override
                                                                        public Double apply(FishState fishState) {
                                                                            return controller.getLastPolicy();
                                                                        }
                                                                    },
                                                                    Double.NaN);
                                                        }
                                                    },
                                                    StepOrder.DAWN,
                                                    365 * FIRST_CONTROL_YEAR
                                            );
                                        }
                                    };

                                }
                            }


                    );

                }
            });


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.
                            append(indicator).append(",");
                }
            });

            for (int run = 0; run < RUNS_TO_RUN; run++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();


            }


        }

    }


    public static void  mainPID(boolean integrated) throws IOException {

        columnsToPrint.remove("TAC from TARGET-TAC Controller");
        columnsToPrint.add("Policy from PID Controller");


        FileWriter fileWriter = new FileWriter(
                DIRECTORY.resolve(
                        integrated ?
                                "indicators_PI.csv" :
                                "indicators_P.csv").toFile());
        fileWriter.write("run,year,indicator,variable,value\n");
        fileWriter.flush();



        for (String indicator : indicatorsToUse) {

            System.out.println(indicator);

            BatchRunner runner = new BatchRunner(
                    DIRECTORY.resolve(
                            "base_islope_2.yaml"),
                    30,
                    columnsToPrint,
                    null,
                    null,
                    0,
                    -1
            );
            runner.setScenarioSetup(new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {
                    final PrototypeScenario prototype = (PrototypeScenario) scenario;
                    prototype.setFishers(200);
                    final PIDControllerIndicatorTarget pid = new PIDControllerIndicatorTarget();
                    pid.setIndicatorColumnName(indicator);
                    double multiplier = pidMultipliers.get(indicator);
                    pid.setIndicatorMultiplier(new FixedDoubleParameter(multiplier));
                    if(multiplier<1)
                        pid.setNegative(false);
                    else
                        pid.setNegative(true);

                    pid.setIntegrated(integrated);
                    prototype.getPlugins().add(
                            new HerfindalndexCollectorFactory()
                    );

                    //never turn it fully off
                    pid.setMinimumTAC(10000);

                    prototype.getPlugins().add(
                            pid
                    );

                }
            });


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.
                            append(indicator).append(",");
                }
            });

            for (int run = 0; run < RUNS_TO_RUN; run++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();


            }


        }

    }



    private static String[] indicatorsToUseForStockAssessment = new String[]{
            "Biomass Species 0 dividedAMillion",
            "Species 0 CPUE",
            "Average Trip Income",
            "Average Income per Hour Out",
            "Species 0 CPHO",
            "Species 0 CPUE" //ADD:
    };



    public static void  mainStockAssessmentFormula(boolean nomovement) throws IOException {

        columnsToPrint.remove("Policy from PID Controller");
        columnsToPrint.add("TAC from TARGET-TAC Controller");


        FileWriter fileWriter = new FileWriter(
                DIRECTORY.resolve( nomovement? "indicators_SBA_formula_nomovement_2.csv" :
                        "indicators_SBA_formula_2.csv").toFile());
        fileWriter.write("run,year,indicator,variable,value\n");
        fileWriter.flush();



        for (String indicator : indicatorsToUseForStockAssessment) {

            System.out.println(indicator);

            BatchRunner runner = new BatchRunner(
                    DIRECTORY.resolve(
                            nomovement ? "base_islope_2_nomo.yaml"
                                    :"base_islope_2.yaml"),
                    30,
                    columnsToPrint,
                    null,
                    null,
                    0,
                    -1
            );
            runner.setScenarioSetup(new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {
                    final PrototypeScenario prototype = (PrototypeScenario) scenario;
                    prototype.setFishers(200);
                    final SurplusProductionDepletionFormulaController sps =
                            new SurplusProductionDepletionFormulaController();
                    sps.setIndicatorColumnName(indicator);




                    //never turn it fully off
                    sps.setMinimumTAC(new FixedDoubleParameter(10000));

                    prototype.getPlugins().add(
                            sps
                    );

                }
            });
            //add a scaled version of the biomass (should have used transformers but for now let's just do it through here
            runner.setBeforeStartSetup(new Consumer<FishState>() {
                @Override
                public void accept(FishState fishState) {
                    fishState.registerStartable(new Startable() {
                        @Override
                        public void start(FishState model) {
                            model.getYearlyDataSet().registerGatherer(
                                    "Inverse Trip Variable Costs",
                                    new Gatherer<FishState>() {
                                        @Override
                                        public Double apply(FishState ignored) {
                                            double variableCosts = ignored.getFishers().stream().mapToDouble(
                                                    new ToDoubleFunction<Fisher>() {
                                                        @Override
                                                        public double applyAsDouble(Fisher value) {
                                                            return value.getLatestYearlyObservation(FisherYearlyTimeSeries.VARIABLE_COSTS);
                                                        }
                                                    }).filter(new DoublePredicate() { //skip boats that made no trips
                                                @Override
                                                public boolean test(double value) {
                                                    return Double.isFinite(value);
                                                }
                                            }).sum();
                                            double trips = ignored.getFishers().stream().mapToDouble(
                                                    new ToDoubleFunction<Fisher>() {
                                                        @Override
                                                        public double applyAsDouble(Fisher value) {
                                                            return value.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS);
                                                        }
                                                    }).filter(new DoublePredicate() { //skip boats that made no trips
                                                @Override
                                                public boolean test(double value) {
                                                    return Double.isFinite(value);
                                                }
                                            }).sum();

                                            return trips > 0 ? trips/variableCosts : 0d;
                                        }
                                    },
                                    0d
                            );


                            model.getYearlyDataSet().registerGatherer(
                                    "Inverse Trip Duration",
                                    new Gatherer<FishState>() {
                                        @Override
                                        public Double apply(FishState ignored) {
                                            double hoursOut = ignored.getFishers().stream().mapToDouble(
                                                    new ToDoubleFunction<Fisher>() {
                                                        @Override
                                                        public double applyAsDouble(Fisher value) {
                                                            return value.getLatestYearlyObservation(FisherYearlyTimeSeries.HOURS_OUT);
                                                        }
                                                    }).filter(new DoublePredicate() { //skip boats that made no trips
                                                @Override
                                                public boolean test(double value) {
                                                    return Double.isFinite(value);
                                                }
                                            }).sum();
                                            double trips = ignored.getFishers().stream().mapToDouble(
                                                    new ToDoubleFunction<Fisher>() {
                                                        @Override
                                                        public double applyAsDouble(Fisher value) {
                                                            return value.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS);
                                                        }
                                                    }).filter(new DoublePredicate() { //skip boats that made no trips
                                                @Override
                                                public boolean test(double value) {
                                                    return Double.isFinite(value);
                                                }
                                            }).sum();

                                            return trips > 0 ? trips/hoursOut : 0d;
                                        }
                                    },
                                    0d
                            );

                            model.getYearlyDataSet().registerGatherer(
                                    "Biomass Species 0 dividedAMillion",
                                    new Gatherer<FishState>() {
                                        @Override
                                        public Double apply(FishState fishState) {
                                            return fishState.getTotalBiomass(fishState.getSpecies("Species 0"))/1000000;
                                        }
                                    },
                                    0d
                            );
                        }
                    });
                }
            });


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.
                            append(indicator).append(",");
                }
            });

            for (int run = 0; run < RUNS_TO_RUN; run++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();


            }


        }

    }


    private static final double[] probabilityToSample = new double[]{.01,.025,
            .05,.1,.25,.5};



    public static void  mainSampling() throws IOException {

        columnsToPrint.remove("TAC from TARGET-TAC Controller");
        columnsToPrint.add("Policy from PID Controller");


        FileWriter fileWriter = new FileWriter(
                DIRECTORY.resolve(
                        "indicators_sampling").toFile());
        fileWriter.write("run,year,probability,variable,value\n");
        fileWriter.flush();



        for (double probability : probabilityToSample) {

            System.out.println(probability);

            BatchRunner runner = new BatchRunner(
                    DIRECTORY.resolve(
                            "base_islope_2.yaml"),
                    30,
                    columnsToPrint,
                    null,
                    null,
                    0,
                    -1
            );
            runner.setScenarioSetup(new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {
                    final PrototypeScenario prototype = (PrototypeScenario) scenario;
                    prototype.setFishers(200);
                    final PIDControllerIndicatorTarget pid = new PIDControllerIndicatorTarget();
                    pid.setIndicatorColumnName("Species 0 CPHO Scaled Sample" );
                    pid.setOffsetColumnName("Species 0 Landings Scaled Sample");
                    double multiplier = 1.5;
                    pid.setIndicatorMultiplier(new FixedDoubleParameter(multiplier));
                    pid.setNegative(true);
                    //never turn it fully off
                    pid.setMinimumTAC(10000);

                    prototype.getPlugins().add(
                            pid
                    );

                    SimpleFishSamplerFactory samplerFactory = new SimpleFishSamplerFactory();
                    samplerFactory.setPercentageSampled(new FixedDoubleParameter(probability));
                    prototype.getPlugins().add(
                            samplerFactory
                    );


                }
            });


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.
                            append(probability).append(",");
                }
            });

            for (int run = 0; run < RUNS_TO_RUN; run++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();


            }


        }

    }



    private static final Map<String, Actuator<FishState, Double>> effortActuators =
            new LinkedHashMap<>();
    static {
        effortActuators.put("season",
                IndexTargetController.RATIO_TO_SEASONAL_CLOSURE);
        effortActuators.put("input",
                IndexTargetController.RATIO_TO_CATCHABILITY(.01));
        effortActuators.put("triplength",
                IndexTargetController.RATIO_TO_DAYSATSEA(5));
        effortActuators.put("fleetsize",
                IndexTargetController.RATIO_TO_FLEET_SIZE);
    }




    public static void  effortTest() throws IOException {

        columnsToPrint.remove("TAC from TARGET-TAC Controller");
        columnsToPrint.add("Index Ratio");

        FileWriter fileWriter = new FileWriter(
                DIRECTORY.resolve("efforts_50_v2.csv").toFile());
        fileWriter.write("run,year,indicator,effort,variable,value\n");
        fileWriter.flush();


        for (Map.Entry<String, Double> indicator : pidMultipliers.entrySet()) {
            if(indicator.getKey().equals("Number Of Active Fishers"))
                continue;

            for (Map.Entry<String, Actuator<FishState, Double>> actuator : effortActuators.entrySet()) {



                BatchRunner runner = new BatchRunner(
                        DIRECTORY.resolve(
                                "base_islope_2.yaml"),
                        30,
                        columnsToPrint,
                        null,
                        null,
                        0,
                        -1
                );
                runner.setScenarioSetup(new Consumer<Scenario>() {
                    @Override
                    public void accept(Scenario scenario) {
                        final PrototypeScenario prototype = (PrototypeScenario) scenario;
                        prototype.setFishers(200);
                        prototype.getPlugins().add(
                                new AlgorithmFactory<AdditionalStartable>() {
                                    @Override
                                    public AdditionalStartable apply(FishState fishState) {
                                        return new AdditionalStartable() {
                                            @Override
                                            public void start(FishState model) {
                                                fishState.scheduleOnceInXDays(
                                                        new Steppable() {
                                                            @Override
                                                            public void step(SimState simState) {
                                                                IndexTargetController controller =
                                                                        new IndexTargetController(
                                                                                new PastAverageSensor(
                                                                                        indicator.getKey(),
                                                                                        1
                                                                                ),
                                                                                new FixedTargetAsMultipleOfOriginalObservation(
                                                                                        indicator.getKey(),
                                                                                        indicator.getValue(),
                                                                                        5
                                                                                ),
                                                                                actuator.getValue(),
                                                                                365,
                                                                                .5,
                                                                                indicator.getValue()<1,


                                                                                false);

                                                                controller.start(model);
                                                                controller.step(model);
                                                                model.getYearlyDataSet().registerGatherer("Index Ratio",
                                                                        new Gatherer<FishState>() {
                                                                            @Override
                                                                            public Double apply(FishState fishState) {
                                                                                return controller.getLastPolicy();
                                                                            }
                                                                        }
                                                                        , Double.NaN);

                                                            }
                                                        },
                                                        StepOrder.DAWN,
                                                        365 * 10

                                                );
                                            }
                                        };

                                    }
                                }


                        );

                    }
                });


                runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                    @Override
                    public void consume(StringBuffer writer, FishState model, Integer year) {
                        writer.
                                append(indicator).append(",").
                                append(actuator.getKey()).append(",");
                    }
                });

                for (int run = 0; run < RUNS_TO_RUN; run++) {
                    System.out.println(indicator.getKey());
                    System.out.println(actuator.getKey());

                    StringBuffer tidy = new StringBuffer();
                    runner.run(tidy);
                    fileWriter.write(tidy.toString());
                    fileWriter.flush();
                }

            }


        }

    }


    static private void fishingFrontExample() throws IOException {
        FishStateUtilities.run(
                "simulation",
                DIRECTORY.resolve("show_fishing_front").resolve("front.yaml"),
                DIRECTORY.resolve("show_fishing_front").resolve("output"),
                0l,
                99,
                false,
                null,
                15,
                false,
                -1,
                null,
                null
        );
    }
}
