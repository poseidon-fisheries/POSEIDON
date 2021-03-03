package uk.ac.ox.oxfish.experiments.mera;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgentBuilder;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesBoxcarFactory;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.policymakers.LBSPRffortPolicyAdaptingFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WrongMKInSprPolicy {

    public static final Path DIRECTORY = Paths.get("docs",
            "20201110 lbspr_policy");

    private static ArrayList<String> columnsToPrint =
            Lists.newArrayList(
                    "Lutjanus malabaricus CPHO",
                    "Lutjanus malabaricus CPUE",
                    "Average Trip Income",
                    "Average Trip Duration",
                    "Lutjanus malabaricus Landings",
                    "Biomass Lutjanus malabaricus",
                    "Bt/K Lutjanus malabaricus",
                    "Average Hours Out",
                    "SPR Lutjanus malabaricus spr_agent_total",
                    "Number Of Active Fishers",
                    "Average Income per Hour Out",
                    "Number Of Active Fishers",
                    "Average Cash-Flow");



    private static final double[] MK_VALUES = new double[]{0.6,0.8,1,1.2,1.5,2};

    private static final int RUNS = 10;



    public static void main(String[] args) throws IOException {
        //realvsguessed();
        adaptiveWrong();
    }



    public static void realvsguessed() throws IOException {

        FileWriter fileWriter = new FileWriter(
                DIRECTORY.resolve("wrong_mk_test_100fishers.csv").toFile());
        fileWriter.write("run,year,real_mk,guessed_mk,variable,value\n");
        fileWriter.flush();


        for(double realMK : MK_VALUES)
        {
            for(double guessedMK : MK_VALUES)
            {
                BatchRunner runner = new BatchRunner(
                        DIRECTORY.resolve(
                                "base_sprpolicy.yaml"),
                        50,
                        columnsToPrint,
                        null,
                        null,
                        0,
                        -1
                );
                runner.setScenarioSetup(new Consumer<Scenario>() {
                    @Override
                    public void accept(Scenario scenario) {

                        ((PrototypeScenario) scenario).setFishers(100);

                        final List<AlgorithmFactory<? extends AdditionalStartable>> plugins =
                                ((PrototypeScenario) scenario).getPlugins();

                        //change the observer K so that it's just original M * (guessed K/M)
                        final SPRAgentBuilder sprAgent = (SPRAgentBuilder) plugins.get(0);
                        sprAgent.setAssumedKParameter(
                                new FixedDoubleParameter(
                                        ((FixedDoubleParameter) sprAgent.getAssumedNaturalMortality()).getFixedValue() /
                                                guessedMK
                                )
                        );

                        System.out.println("guessed mk " +
                                (sprAgent.getAssumedNaturalMortality().apply(new MersenneTwisterFast()))/
                                        (sprAgent.getAssumedKParameter().apply(new MersenneTwisterFast()))
                        );

                        //change the REAL K so that it's just original M * (real K/M)
                        final SingleSpeciesBoxcarFactory biology = (SingleSpeciesBoxcarFactory)
                                ((PrototypeScenario) scenario).getBiologyInitializer();
                        biology.setK(
                                new FixedDoubleParameter(
                                        ((FixedDoubleParameter) biology.getYearlyMortality()).getFixedValue() /
                                                realMK
                                )

                        );

                        System.out.println("real mk " +
                                (biology.getYearlyMortality().apply(new MersenneTwisterFast()))/
                                        (biology.getK().apply(new MersenneTwisterFast()))
                        );


                    }
                });

                runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                    @Override
                    public void consume(StringBuffer writer, FishState model, Integer year) {
                        writer.
                                append(realMK).append(",").append(guessedMK).append(",");
                    }
                });



                for (int run = 0; run < RUNS; run++) {
                    StringBuffer tidy = new StringBuffer();
                    runner.run(tidy);
                    fileWriter.write(tidy.toString());
                    fileWriter.flush();


                }


            }


        }


    }


    public static void adaptiveWrong() throws IOException {

        FileWriter fileWriter = new FileWriter(
                DIRECTORY.resolve("adaptive").resolve("wrong_mk_adaptive_test_100fishers.csv").toFile());
        fileWriter.write("run,year,real_mk,guessed_mk,variable,value\n");
        fileWriter.flush();


        for(double realMK : MK_VALUES)
        {
            BatchRunner runner = new BatchRunner(
                    DIRECTORY.resolve("adaptive").resolve(
                            "base_adaptive_sprpolicy.yaml"),
                    50,
                    columnsToPrint,
                    null,
                    null,
                    0,
                    -1
            );
            runner.setScenarioSetup(new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {

                    ((PrototypeScenario) scenario).setFishers(100);

                    final List<AlgorithmFactory<? extends AdditionalStartable>> plugins =
                            ((PrototypeScenario) scenario).getPlugins();

                    //change the observer K so that it's just original M * (guessed K/M)
                    final SPRAgentBuilder sprAgent = (SPRAgentBuilder) ((LBSPRffortPolicyAdaptingFactory) plugins.get(0)).getSprAgentDelegate();
                    //always start by assuming M/K is 0.6
                    sprAgent.setAssumedKParameter(
                            new FixedDoubleParameter(
                                    ((FixedDoubleParameter) sprAgent.getAssumedNaturalMortality()).getFixedValue() /
                                            0.6
                            )
                    );


                    //change the REAL K so that it's just original M * (real K/M)
                    final SingleSpeciesBoxcarFactory biology = (SingleSpeciesBoxcarFactory)
                            ((PrototypeScenario) scenario).getBiologyInitializer();
                    biology.setK(
                            new FixedDoubleParameter(
                                    ((FixedDoubleParameter) biology.getYearlyMortality()).getFixedValue() /
                                            realMK
                            )

                    );

                    System.out.println("real mk " +
                            (biology.getYearlyMortality().apply(new MersenneTwisterFast()))/
                                    (biology.getK().apply(new MersenneTwisterFast()))
                    );


                }
            });

            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.
                            append(realMK).append(",").append("adaptive").append(",");
                }
            });



            for (int run = 0; run < RUNS; run++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();


            }





        }


    }
}
