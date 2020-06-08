package uk.ac.ox.oxfish.experiments;

import com.google.common.collect.Lists;
import sim.display.Console;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.policymakers.ISlope;
import uk.ac.ox.oxfish.model.regs.policymakers.ITarget;
import uk.ac.ox.oxfish.model.regs.policymakers.IslopeToTacController;
import uk.ac.ox.oxfish.model.regs.policymakers.TargetToTACController;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

public class ISlopeDemo {


    public static final Path DIRECTORY = Paths.get("docs",
            "20200604 islope");
    public static final int RUNS_TO_RUN = 100;
    private static String[] indicatorsToUse = new String[]{
            "Species 0 CPHO",
            "Species 0 CPUE",
            "Average Trip Income",
            "Number Of Active Fishers",
            "Biomass Species 0"
    };

    private static List<String> columnsToPrint = Lists.newArrayList(
            "Species 0 CPHO",
            "Species 0 CPUE",
            "Average Trip Income",
            "Average Trip Duration",
            "Species 0 Landings",
            "Biomass Species 0",
            "TAC from TARGET-TAC Controller",
            "Average Cash-Flow");



    public static void main(String[] args) throws IOException {
        mainIslope(args);
        mainITarget(args);
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
                                                                            5
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
                                                                            5
                                                                    )
                                                            );
                                                            controller.start(model);
                                                            controller.step(model);
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


}
