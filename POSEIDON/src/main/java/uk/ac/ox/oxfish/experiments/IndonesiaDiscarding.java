/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.experiments;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.DiscreteRandomAlgorithm;
import uk.ac.ox.oxfish.fisher.strategies.discarding.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMarketFactory;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.scenario.IndonesiaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by carrknight on 7/12/17.
 */
public class IndonesiaDiscarding {

    // original
//    private static final Path DIRECTORY = Paths.get("docs","20170715 minimum_indonesia");
//    private static final Path SCENARIO_FILE = DIRECTORY.resolve("market.yaml");
//    public static final int DISCARDING_BIN = 1;
//    public static final int MAXIMUM_FINED_BIN = 0;
//    public static final int EXPECTED_NUMBER_OF_BINS = 3;
//    public static final int NUMBER_OF_YEARS_NO_FISHING = 5;
    //   public static final int NUMBER_OF_YEARS_FISHING = 20;

    //boxcar
    private static final Path DIRECTORY = Paths.get("docs","20171214 boxcar_indonesia");
    private static final Path SCENARIO_FILE = DIRECTORY.resolve("boxcar_indonesia.yaml");
    public static final int DISCARDING_BIN = 56;
    public static final int MAXIMUM_FINED_BIN = 55;
    public static final int EXPECTED_NUMBER_OF_BINS = 100;
    public static final int NUMBER_OF_RUNS = 5;
    public static final int NUMBER_OF_YEARS_NO_FISHING = 0;
    public static final int NUMBER_OF_YEARS_FISHING = 5;

    public static void discardingFine(String[] args) throws IOException {

        File outputFile = DIRECTORY.resolve("discarding_fine.csv").toFile();
        FileWriter writer = prepWriter(outputFile);
        for(double fine=10; fine>-30; fine= fine-1d)
        {

            for(int run = 0; run< NUMBER_OF_RUNS; run++) {
                FishState state = new FishState(System.currentTimeMillis());
                FishYAML yaml = new FishYAML();
                IndonesiaScenario scenario = yaml.loadAs(
                        new FileReader(
                                SCENARIO_FILE.toFile()
                        ), IndonesiaScenario.class
                );
                state.setScenario(scenario);

                ThreePricesMarketFactory market = new ThreePricesMarketFactory();
                scenario.setMarket(market);
                market.setLowAgeThreshold(new FixedDoubleParameter(MAXIMUM_FINED_BIN));
                market.setPriceBelowThreshold(new FixedDoubleParameter(fine));
                market.setPriceBetweenThresholds(new FixedDoubleParameter(10));

                state.start();
                while (state.getYear() <= NUMBER_OF_YEARS_NO_FISHING)
                    state.schedule.step(state);

                for (Fisher fisher : state.getFishers()) {
                    DiscardUnderagedFactory discardUnderagedFactory = new DiscardUnderagedFactory();
                    discardUnderagedFactory.setMinAgeRetained(new FixedDoubleParameter(1d));
                    PeriodicUpdateDiscarding discarding = new PeriodicUpdateDiscarding(
                            Lists.newArrayList(NoDiscarding.class,
                                    DiscardUnderaged.class),
                            Lists.newArrayList(new NoDiscardingFactory(),
                                    discardUnderagedFactory)
                    );
                    discarding.start(state, fisher);

                    fisher.setRegulation(new Anarchy());
                }

                while (state.getYear() <= NUMBER_OF_YEARS_FISHING)
                    state.schedule.step(state);


                dumpObservation(writer, run,fine, state, 10d);

            }

        }



    }

    @NotNull
    public static FileWriter prepWriter(File outputFile) throws IOException {
        FileWriter writer = new FileWriter(outputFile);
        //writer.write("price_low,price_high,landings,earnings,cash-flow,landings_0,landings_1,landings_2,discarding_agents,catches_0");
        writer.write("run,price_low,price_high,landings,earnings,cash-flow,");
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
        {
            writer.write("landings_" + i);
            writer.write(",");
        }
        writer.write("discarding_agents");
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
        {
            writer.write(",");
            writer.write("catches_" + i);
        }

        writer.write("\n");
        writer.flush();
        return writer;
    }

    public static void noDiscardingFine(String[] args) throws IOException {

        File outputFile = DIRECTORY.resolve( "nodiscarding_fine.csv").toFile();
        FileWriter writer = prepWriter(outputFile);
        for(double fine=10; fine>-30; fine= fine-1d)
        {

            for(int run = 0; run< NUMBER_OF_RUNS; run++) {
                FishState state = new FishState(System.currentTimeMillis());
                FishYAML yaml = new FishYAML();
                IndonesiaScenario scenario = yaml.loadAs(
                        new FileReader(
                                SCENARIO_FILE.toFile()
                        ), IndonesiaScenario.class
                );
                state.setScenario(scenario);

                ThreePricesMarketFactory market = new ThreePricesMarketFactory();
                scenario.setMarket(market);
                market.setLowAgeThreshold(new FixedDoubleParameter(MAXIMUM_FINED_BIN));
                market.setPriceBelowThreshold(new FixedDoubleParameter(fine));
                market.setPriceBetweenThresholds(new FixedDoubleParameter(10));

                state.start();
                while (state.getYear() <= NUMBER_OF_YEARS_NO_FISHING)
                    state.schedule.step(state);

                for (Fisher fisher : state.getFishers()) {
                /*    DiscardUnderagedFactory discardUnderagedFactory = new DiscardUnderagedFactory();
                    discardUnderagedFactory.setMinAgeRetained(new FixedDoubleParameter(1d));
                    PeriodicUpdateDiscarding discarding = new PeriodicUpdateDiscarding(
                            Lists.newArrayList(NoDiscarding.class,
                                               DiscardUnderaged.class),
                            Lists.newArrayList(new NoDiscardingFactory(),
                                               discardUnderagedFactory)
                    );
                    discarding.start(state, fisher);
                    */
                    fisher.setRegulation(new Anarchy());
                }

                while (state.getYear() <= NUMBER_OF_YEARS_FISHING)
                    state.schedule.step(state);


                dumpObservation(writer,run, fine, state, 10d);

            }

        }



    }

    public   static void dumpObservation(FileWriter writer,int run, double fine, FishState state, double subsidy) throws IOException {
        StringBuffer observation = new StringBuffer();
        observation.append(Integer.toString(run)).append(",");
        observation.append(fine).append(",");
        observation.append(subsidy).append(",");
        observation.append(state.getLatestYearlyObservation("Red Fish Landings")).append(",");
        observation.append(state.getLatestYearlyObservation("Red Fish Earnings")).append(",");
        observation.append(state.getLatestYearlyObservation("Average Cash-Flow")).append(",");
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
            observation.append(state.getLatestYearlyObservation("Red Fish Landings - age bin "+i)).append(",");

        int discarders = 0;
        for (Fisher fisher : state.getFishers())
            if (!fisher.getDiscardingStrategy().getClass().equals(NoDiscarding.class))
                discarders++;

        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
            observation.append(",").append(state.getLatestYearlyObservation("Red Fish Catches - age bin "+i));
        observation.append("\n");
        writer.write(observation.toString());
        writer.flush();
        System.out.println(observation);
    }


    public static void noDiscardingSubsidy(String[] args) throws IOException {

        File outputFile = DIRECTORY.resolve("nodiscarding_subsidy.csv").toFile();
        FileWriter writer = prepWriter(outputFile);

        for(double subsidy=9; subsidy<100; subsidy= subsidy+1d)
        {

            for(int run = 0; run< NUMBER_OF_RUNS; run++) {
                FishState state = new FishState(System.currentTimeMillis());
                FishYAML yaml = new FishYAML();
                IndonesiaScenario scenario = yaml.loadAs(
                        new FileReader(
                                SCENARIO_FILE.toFile()
                        ), IndonesiaScenario.class
                );
                state.setScenario(scenario);

                ThreePricesMarketFactory market = new ThreePricesMarketFactory();
                scenario.setMarket(market);
                market.setLowAgeThreshold(new FixedDoubleParameter(MAXIMUM_FINED_BIN));
                market.setPriceBelowThreshold(new FixedDoubleParameter(10));
                market.setPriceBetweenThresholds(new FixedDoubleParameter(subsidy));

                state.start();
                while (state.getYear() <= NUMBER_OF_YEARS_NO_FISHING)
                    state.schedule.step(state);

                for (Fisher fisher : state.getFishers()) {
                /*    DiscardUnderagedFactory discardUnderagedFactory = new DiscardUnderagedFactory();
                    discardUnderagedFactory.setMinAgeRetained(new FixedDoubleParameter(1d));
                    PeriodicUpdateDiscarding discarding = new PeriodicUpdateDiscarding(
                            Lists.newArrayList(NoDiscarding.class,
                                               DiscardUnderaged.class),
                            Lists.newArrayList(new NoDiscardingFactory(),
                                               discardUnderagedFactory)
                    );
                    discarding.start(state, fisher);
                    */
                    fisher.setRegulation(new Anarchy());
                }

                while (state.getYear() <= NUMBER_OF_YEARS_FISHING)
                    state.schedule.step(state);


                dumpObservation(writer, run,10d,state,subsidy);


            }

        }



    }

    public static void discarding(String[] args) throws IOException {

        File outputFile = DIRECTORY.resolve("discarding_subsidy.csv").toFile();
        FileWriter writer = prepWriter(outputFile);
        writer.write("price_low,price_high,landings,earnings,cash-flow,landings_0,landings_1,landings_2,discarding_agents,catches_0");
        writer.write("\n");
        writer.flush();
        for(double subsidy=9; subsidy<100; subsidy= subsidy+1d)
        {

            for(int run = 0; run< NUMBER_OF_RUNS; run++) {
                FishState state = new FishState(System.currentTimeMillis());
                FishYAML yaml = new FishYAML();
                IndonesiaScenario scenario = yaml.loadAs(
                        new FileReader(
                                SCENARIO_FILE.toFile()
                        ), IndonesiaScenario.class
                );
                state.setScenario(scenario);

                ThreePricesMarketFactory market = new ThreePricesMarketFactory();
                scenario.setMarket(market);
                market.setLowAgeThreshold(new FixedDoubleParameter(MAXIMUM_FINED_BIN));
                market.setPriceBelowThreshold(new FixedDoubleParameter(10));
                market.setPriceBetweenThresholds(new FixedDoubleParameter(subsidy));

                state.start();
                while (state.getYear() <= NUMBER_OF_YEARS_NO_FISHING)
                    state.schedule.step(state);

                for (Fisher fisher : state.getFishers()) {
                    DiscardUnderagedFactory discardUnderagedFactory = new DiscardUnderagedFactory();
                    discardUnderagedFactory.setMinAgeRetained(new FixedDoubleParameter(DISCARDING_BIN));
                    PeriodicUpdateDiscarding discarding = new PeriodicUpdateDiscarding(
                            Lists.newArrayList(NoDiscarding.class,
                                    DiscardUnderaged.class),
                            Lists.newArrayList(new NoDiscardingFactory(),
                                    discardUnderagedFactory)
                    );
                    discarding.start(state, fisher);
                    fisher.setRegulation(new Anarchy());
                }

                while (state.getYear() <= NUMBER_OF_YEARS_FISHING)
                    state.schedule.step(state);


                StringBuffer observation = new StringBuffer();
                observation.append(10d).append(",");
                observation.append(subsidy).append(",");
                observation.append(state.getLatestYearlyObservation("Red Fish Landings")).append(",");
                observation.append(state.getLatestYearlyObservation("Red Fish Earnings")).append(",");
                observation.append(state.getLatestYearlyObservation("Average Cash-Flow")).append(",");
                observation.append(state.getLatestYearlyObservation("Red Fish Landings - age bin 0")).append(",");
                observation.append(state.getLatestYearlyObservation("Red Fish Landings - age bin 1")).append(",");
                observation.append(state.getLatestYearlyObservation("Red Fish Landings - age bin 2")).append(",");


                int discarders = 0;
                for (Fisher fisher : state.getFishers())
                    if (!fisher.getDiscardingStrategy().getClass().equals(NoDiscarding.class))
                        discarders++;

                observation.append(discarders).append(",");
                observation.append(state.getLatestYearlyObservation("Red Fish Catches - age bin 0")).append("\n");
                writer.write(observation.toString());
                writer.flush();
                System.out.println(observation);

            }

        }

        writer.close();

    }








}


class PeriodicUpdateDiscarding implements FisherStartable
{


    private final BiMap<Class<? extends DiscardingStrategy>,
            AlgorithmFactory<? extends DiscardingStrategy>> options;




    public PeriodicUpdateDiscarding(List<Class<? extends DiscardingStrategy>> discards,
                                    List<AlgorithmFactory<? extends DiscardingStrategy>> factories)
    {

        Preconditions.checkArgument(discards.size() == factories.size());

        options = HashBiMap.create(discards.size());
        for(int i=0; i<discards.size(); i++)
        {
            options.put(discards.get(i),factories.get(i));

        }

    }


    @Override
    public void start(FishState model, Fisher fisher) {

        fisher.addBiMonthlyAdaptation(
                new ExploreImitateAdaptation<Class<? extends DiscardingStrategy>>(
                        new Predicate<Fisher>() {
                            @Override
                            public boolean test(Fisher fisher1) {
                                return true;
                            }
                        },
                        new DiscreteRandomAlgorithm<Class<? extends DiscardingStrategy>>(
                                Lists.newArrayList(options.keySet())),
                        new Actuator<Fisher, Class<? extends DiscardingStrategy>>() {
                            @Override
                            public void apply(
                                    Fisher subject, Class<? extends DiscardingStrategy> policy, FishState model) {

                                subject.setDiscardingStrategy(
                                        options.get(policy).apply(model)
                                );
                            }
                        },
                        new Sensor<Fisher, Class<? extends DiscardingStrategy>>() {
                            @Override
                            public Class<? extends DiscardingStrategy> scan(Fisher system) {
                                return system.getDiscardingStrategy().getClass();
                            }
                        },
                        new CashFlowObjective(60),
                        new FixedProbability(.1, .8),
                        new Predicate<Class<? extends DiscardingStrategy>>() {
                            @Override
                            public boolean test(Class<? extends DiscardingStrategy> aClass) {
                                return true;
                            }
                        }

                )
        );


    }

    @Override
    public void turnOff(Fisher fisher) {

    }
}
