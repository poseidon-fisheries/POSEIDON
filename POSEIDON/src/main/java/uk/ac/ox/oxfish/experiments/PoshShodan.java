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

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.AdditionalFishStateDailyCollectors;
import uk.ac.ox.oxfish.model.regs.ExternalOpenCloseSeason;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.dynapro.OLSDynamicProgram;
import uk.ac.ox.oxfish.utility.dynapro.OLSDynamicProgramUCB;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Basically what I did for shodan but with a proper object instead
 * Created by carrknight on 12/16/16.
 */
public class PoshShodan {


    public static final int GENERATIONS = 500;
    private static final int SIMULATION_PER_STEP = 10;

    public static void main(String[] args) throws IOException {

        //countingGas();
        //countingDays();
        //kitchensink();
        kitchensinkUCB();

    }

    private static void kitchensinkUCB() throws IOException {
        OLSDynamicProgram shodan = new OLSDynamicProgramUCB(
            2,
            new Function<FishState, Double>() {
                @Override
                public Double apply(FishState state) {
                    Iterator<Double> landings = state.getDailyDataSet().getColumn(
                        "Average Cash-Flow").descendingIterator();
                    double reward = 0;
                    for (int i = 0; i < 30; i++)
                        reward += landings.next();
                    return reward;
                }
            },
            true,
            true,
            true,
            true,
            true,
            true,
            0,
            new Predicate<double[]>() {
                @Override
                public boolean test(double[] doubles) {
                    return doubles[1] <= FishStateUtilities.EPSILON;
                }
            },
            1.5,
            //months lefts
            //gas price
            //landings
            //effort
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    return (double) Math.round(240 - fishStateDoublePair.getFirst().getDay() / 30);
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    Iterator<Double> landings = fishStateDoublePair.getFirst()
                        .getDailyDataSet().getColumn(
                            "Average X Towed").descendingIterator();
                    double sum = 0;
                    for (int i = 0; i < 30; i++)
                        sum += landings.next();
                    return sum;
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    Iterator<Double> landings = fishStateDoublePair.getFirst()
                        .getDailyDataSet().getColumn(
                            "Species 0 Landings").descendingIterator();
                    double sum = 0;
                    for (int i = 0; i < 30; i++)
                        sum += landings.next();
                    return sum;
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    Iterator<Double> effort = fishStateDoublePair.getFirst()
                        .getDailyDataSet().getColumn(
                            "Total Effort").descendingIterator();
                    double sum = 0;
                    for (int i = 0; i < 30; i++)
                        sum += effort.next();
                    return sum;
                }
            }


        );
        mainLoop(shodan, "kitchensink_ucb_2", .15, .05);
    }

    private static void mainLoop(
        OLSDynamicProgram shodan, final String name, final double initialErrorRate, final double minErrorRate
    ) throws IOException {

        Paths.get("runs", "posh_shodan", name).toFile().mkdirs();

        //do one lspiRun everything open
        FishState initialRun = gasRun(shodan, 0);
        double initialScore = 0;
        for (Double landing : initialRun.getYearlyDataSet().getColumn("Average Cash-Flow"))
            initialScore += landing;

        File masterCSV = Paths.get("runs", "posh_shodan", name, "shodan.csv").toFile();
        BufferedWriter writer = new BufferedWriter(
            new FileWriter(masterCSV, false));
        writer.newLine();
        writer.write("open," + initialScore);
        System.out.println("open," + initialScore);
        writer.close();

        //another lspiRun everything random
        shodan.setErrorRate(1);
        initialRun = gasRun(shodan, 0);
        initialScore = 0;
        for (Double landing : initialRun.getYearlyDataSet().getColumn("Average Cash-Flow"))
            initialScore += landing;

        writer = new BufferedWriter(
            new FileWriter(masterCSV, true));
        writer.newLine();
        writer.write("random," + initialScore);
        System.out.println("random," + initialScore);
        writer.close();

        //now start for real
        shodan.setErrorRate(initialErrorRate);
        for (int generation = 0; generation < GENERATIONS; generation++) {

            for (int run = 0; run < SIMULATION_PER_STEP; run++) {
                gasRun(shodan, System.currentTimeMillis());
                //less error rate
                shodan.setErrorRate(Math.max(shodan.getErrorRate() * .999, minErrorRate));
            }
            shodan.regress();
            //save regression coefficients
            File regressionFile = Paths.get("runs", "posh_shodan", name, "coefficients_" + generation + ".txt")
                .toFile();
            writer = new BufferedWriter(
                new FileWriter(regressionFile, false));
            writer.write(FishStateUtilities.deepToStringArray(shodan.getLinearParameters(), ",", "\n"));
            writer.close();
            System.out.println(FishStateUtilities.deepToStringArray(shodan.getLinearParameters(), ",", "\n"));
            //make one lspiRun with error rate 0
            double errorRate = shodan.getErrorRate();
            shodan.setErrorRate(0d);
            FishState referenceRun = gasRun(shodan, 0);
            double score = 0;
            for (Double landing : referenceRun.getYearlyDataSet().getColumn("Average Cash-Flow"))
                score += landing;

            shodan.setErrorRate(errorRate);
            writer = new BufferedWriter(
                new FileWriter(masterCSV, true));
            writer.newLine();
            System.out.println((generation + 1) + "," + score);
            writer.write(generation + "," + score);
            writer.close();
        }
    }

    private static FishState gasRun(final OLSDynamicProgram shodan, final long seed) {
        //object we use to control season
        ExternalOpenCloseSeason controller = new ExternalOpenCloseSeason();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(100);

        scenario.setRegulation(new AlgorithmFactory<Regulation>() {
            @Override
            public Regulation apply(FishState state) {
                return controller;
            }
        });

        FishState state = new FishState(seed);
        state.attachAdditionalGatherers();
        state.registerStartable(new AdditionalFishStateDailyCollectors());
        state.setScenario(scenario);
        state.start();


        state.scheduleEveryXDay(new Steppable() {
            @Override
            public void step(SimState simState) {
                if (state.getDay() < 30)
                    return;

                //change oil price
                for (Port port : state.getPorts())
                    port.setGasPricePerLiter(state.getDayOfTheYear() / 1000d);

                int action = shodan.step(state);
                controller.setOpen(action == 0);

                System.out.println("Is controller open? " + controller.isOpen());
            }
        }, StepOrder.AFTER_DATA, 30);

        while (state.getDay() <= 7200)
            state.schedule.step(state);
        return state;
    }

    private static void kitchensink() throws IOException {
        OLSDynamicProgram shodan = new OLSDynamicProgram(
            2,
            new Function<FishState, Double>() {
                @Override
                public Double apply(FishState state) {
                    Iterator<Double> landings = state.getDailyDataSet().getColumn(
                        "Average Cash-Flow").descendingIterator();
                    double reward = 0;
                    for (int i = 0; i < 30; i++)
                        reward += landings.next();
                    return reward;
                }
            },
            false,
            false,
            true,
            true,
            true,
            true,
            0,
            new Predicate<double[]>() {
                @Override
                public boolean test(double[] doubles) {
                    return doubles[1] <= FishStateUtilities.EPSILON;
                }
            },
            //months lefts
            //gas price
            //landings
            //effort
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    return (double) Math.round(240 - fishStateDoublePair.getFirst().getDay() / 30);
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    Iterator<Double> landings = fishStateDoublePair.getFirst()
                        .getDailyDataSet().getColumn(
                            "Average X Towed").descendingIterator();
                    double sum = 0;
                    for (int i = 0; i < 30; i++)
                        sum += landings.next();
                    return sum;
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    Iterator<Double> landings = fishStateDoublePair.getFirst()
                        .getDailyDataSet().getColumn(
                            "Species 0 Landings").descendingIterator();
                    double sum = 0;
                    for (int i = 0; i < 30; i++)
                        sum += landings.next();
                    return sum;
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    Iterator<Double> effort = fishStateDoublePair.getFirst()
                        .getDailyDataSet().getColumn(
                            "Total Effort").descendingIterator();
                    double sum = 0;
                    for (int i = 0; i < 30; i++)
                        sum += effort.next();
                    return sum;
                }
            }


        );
        mainLoop(shodan, "kitchensink", .9, .15);
    }

    private static void countingDays() throws IOException {
        OLSDynamicProgram shodan = new OLSDynamicProgram(
            2,
            new Function<FishState, Double>() {
                @Override
                public Double apply(FishState state) {
                    Iterator<Double> landings = state.getDailyDataSet().getColumn(
                        "Average Cash-Flow").descendingIterator();
                    double reward = 0;
                    for (int i = 0; i < 30; i++)
                        reward += landings.next();
                    return reward;
                }
            },
            false,
            false,
            false,
            true,
            false,
            false,
            0,
            new Predicate<double[]>() {
                @Override
                public boolean test(double[] doubles) {
                    return doubles[1] <= FishStateUtilities.EPSILON;
                }
            },
            //months lefts
            //gas price
            //landings
            //effort
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    return (double) Math.round(240 - fishStateDoublePair.getFirst().getDay() / 30);
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    return (double) fishStateDoublePair.getFirst().getDayOfTheYear();
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    Iterator<Double> landings = fishStateDoublePair.getFirst()
                        .getDailyDataSet().getColumn(
                            "Species 0 Landings").descendingIterator();
                    double sum = 0;
                    for (int i = 0; i < 30; i++)
                        sum += landings.next();
                    return sum;
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    Iterator<Double> effort = fishStateDoublePair.getFirst()
                        .getDailyDataSet().getColumn(
                            "Total Effort").descendingIterator();
                    double sum = 0;
                    for (int i = 0; i < 30; i++)
                        sum += effort.next();
                    return sum;
                }
            }


        );
        mainLoop(shodan, "counting_days", .9, .15);
    }

    private static void countingGas() throws IOException {
        OLSDynamicProgram shodan = new OLSDynamicProgram(
            2,
            new Function<FishState, Double>() {
                @Override
                public Double apply(FishState state) {
                    Iterator<Double> landings = state.getDailyDataSet().getColumn(
                        "Average Cash-Flow").descendingIterator();
                    double reward = 0;
                    for (int i = 0; i < 30; i++)
                        reward += landings.next();
                    return reward;
                }
            },
            false,
            false,
            false,
            false,
            false,
            false,
            0,
            new Predicate<double[]>() {
                @Override
                public boolean test(double[] doubles) {
                    return doubles[1] <= FishStateUtilities.EPSILON;
                }
            },
            //months lefts
            //gas price
            //landings
            //effort
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    return (double) Math.round(240 - fishStateDoublePair.getFirst().getDay() / 30);
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    return fishStateDoublePair.getFirst().getPorts().iterator().next().getGasPricePerLiter();
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    Iterator<Double> landings = fishStateDoublePair.getFirst()
                        .getDailyDataSet().getColumn(
                            "Species 0 Landings").descendingIterator();
                    double sum = 0;
                    for (int i = 0; i < 30; i++)
                        sum += landings.next();
                    return sum;
                }
            },
            new Function<Pair<FishState, Double>, Double>() {
                @Override
                public Double apply(Pair<FishState, Double> fishStateDoublePair) {
                    Iterator<Double> effort = fishStateDoublePair.getFirst()
                        .getDailyDataSet().getColumn(
                            "Total Effort").descendingIterator();
                    double sum = 0;
                    for (int i = 0; i < 30; i++)
                        sum += effort.next();
                    return sum;
                }
            }


        );
        mainLoop(shodan, "counting_gas", .9, .15);
    }

}
