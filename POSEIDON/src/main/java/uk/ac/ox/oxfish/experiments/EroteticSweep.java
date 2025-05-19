/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory.AverageProfitsThresholdFactory;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory.FixedProfitThresholdFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.SNALSARDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQStringFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EroteticSweep {


    private static final int RUNS = 5;
    //public static final Path MAIN_DIRECTORY = Paths.get("docs", "20170322 cali_catch", "results");
    //public static final Path MAIN_DIRECTORY = Paths.get("docs", "20170606 cali_catchability_2", "results");
    private static final Path MAIN_DIRECTORY = Paths.get("inputs", "erotetic_paper");
    private static final int YEARS_PER_RUN = 5;


    private static final double MIN_FAILURE_THRESHOLD = 1;
    private static final double MAX_FAILURE_THRESHOLD = 10;

    private static final int MIN_MPA_WIDTH = 1;
    private static final int MAX_MPA_WIDTH = 3;
    private static final double MIN_ADAPTIVE_THRESHOLD = 0.1;
    private static final double MAX_ADAPTIVE_THRESHOLD = 2;
    private static final int MIN_QUOTA = 500;
    private static final int MAX_QUOTA = 1000;

    public static void main(String[] args) throws IOException {
        //cheatingSweep();
        //cheatingSweepAdaptive();
        ITQSweep();
    }

    public static void ITQSweep() throws IOException {

        FileWriter writer = new FileWriter(MAIN_DIRECTORY.resolve("itq.csv").toFile());

        writer.write("run,quota,acceptable,cashflow,red_biomass,blue_biomass,red_landings,blue_landings" + '\n');
        writer.flush();

        for (int run = 0; run < RUNS; run++) {
            for (int blueQuota = MIN_QUOTA; blueQuota <= MAX_QUOTA; blueQuota += 500) {
                for (double failure = MIN_ADAPTIVE_THRESHOLD;
                     failure <= MAX_ADAPTIVE_THRESHOLD;
                     failure = FishStateUtilities.round(failure + 0.1)) {


                    adaptiveITQRun(blueQuota, failure, writer, run);
                }

                eeiQuota(blueQuota, writer, run);

            }
        }


        writer.flush();
        writer.close();


    }

    public static void adaptiveITQRun(
        int blueQuota, double adaptiveThreshold,
        FileWriter writer, int seed
    ) throws IOException {
        System.out.println("starting: " + seed + "," +
            blueQuota + "," +
            adaptiveThreshold);
        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(
            new FileReader(MAIN_DIRECTORY.resolve("itq.yaml").toFile()),
            PrototypeScenario.class
        );

        //set it up!
        ((SNALSARDestinationFactory) scenario.getDestinationStrategy()).setAcceptableThreshold(
            new AverageProfitsThresholdFactory(adaptiveThreshold)
        );
        ((MultiITQStringFactory) scenario.getRegulation()).setYearlyQuotaMaps("0:4000,1:" + blueQuota);

        //run it!
        FishState state = new FishState(seed);
        state.setScenario(scenario);
        state.start();
        while (state.getYear() < YEARS_PER_RUN) {
            state.schedule.step(state);

        }

        double cashflow = state.getYearlyDataSet().getColumn("Average Cash-Flow").stream().mapToDouble(
            value -> value).sum();

        //"run,quota,acceptable,illegal,cashflow,red_biomass,blue_biomass,red_landings,blue_landings"
        writer.write(
            seed + "," +
                blueQuota + "," +
                adaptiveThreshold + "," +
                cashflow + "," +
                state.getYearlyDataSet().getLatestObservation("Biomass Species 0") + "," +
                state.getYearlyDataSet().getLatestObservation("Biomass Species 1") + "," +
                state.getYearlyDataSet().getLatestObservation("Species 0 Landings") + "," +
                state.getYearlyDataSet().getLatestObservation("Species 1 Landings") + "\n"
        );
        writer.flush();

    }

    public static void eeiQuota(
        int blueQuota,
        FileWriter writer, int seed
    ) throws IOException {
        System.out.println("starting: " + seed + "," +
            blueQuota + "," +
            9999);
        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(
            new FileReader(MAIN_DIRECTORY.resolve("itq.yaml").toFile()),
            PrototypeScenario.class
        );

        //set it up!
        PerTripImitativeDestinationFactory destinationStrategy = new PerTripImitativeDestinationFactory();
        destinationStrategy.setProbability(new FixedProbabilityFactory(.2, 1d));
        scenario.setDestinationStrategy(destinationStrategy);
        ((MultiITQStringFactory) scenario.getRegulation()).setYearlyQuotaMaps("0:4000,1:" + blueQuota);

        //run it!
        FishState state = new FishState(seed);
        state.setScenario(scenario);
        state.start();
        while (state.getYear() < YEARS_PER_RUN) {
            state.schedule.step(state);

        }

        double cashflow = state.getYearlyDataSet().getColumn("Average Cash-Flow").stream().mapToDouble(
            value -> value).sum();

        //"run,quota,acceptable,illegal,cashflow,red_biomass,blue_biomass,red_landings,blue_landings"
        writer.write(
            seed + "," +
                blueQuota + "," +
                9999 + "," +
                cashflow + "," +
                state.getYearlyDataSet().getLatestObservation("Biomass Species 0") + "," +
                state.getYearlyDataSet().getLatestObservation("Biomass Species 1") + "," +
                state.getYearlyDataSet().getLatestObservation("Species 0 Landings") + "," +
                state.getYearlyDataSet().getLatestObservation("Species 1 Landings") + "\n"
        );
        writer.flush();

    }

    public static void cheatingSweep() throws IOException {

        FileWriter writer = new FileWriter(MAIN_DIRECTORY.resolve("cheating.csv").toFile());
        writer.write("run,width,failure,day,illegal,cashflow,biomass,landings" + '\n');
        writer.flush();

        for (int run = 0; run < RUNS; run++) {
            for (int width = MIN_MPA_WIDTH; width <= MAX_MPA_WIDTH; width++) {
                for (double failure = MIN_FAILURE_THRESHOLD;
                     failure <= MAX_FAILURE_THRESHOLD;
                     failure = FishStateUtilities.round(failure + 0.5)) {


                    cheatingRun(width, failure, writer, run);
                }

            }
        }

        writer.flush();
        writer.close();


    }

    public static void cheatingRun(
        int mpaWidth, double failureThreshold,
        FileWriter writer, int seed
    ) throws IOException {
        System.out.println("starting: " + seed + "," +
            mpaWidth + "," +
            failureThreshold);
        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(
            new FileReader(MAIN_DIRECTORY.resolve("cheating.yaml").toFile()),
            PrototypeScenario.class
        );

        //set it up!
        ((SNALSARDestinationFactory) scenario.getDestinationStrategy()).setFailureThreshold(
            new FixedProfitThresholdFactory(failureThreshold)
        );
        ((ProtectedAreasOnlyFactory) scenario.getRegulation()).getStartingMPAs().clear();
        ((ProtectedAreasOnlyFactory) scenario.getRegulation()).getStartingMPAs().add(
            new StartingMPA(
                0, 0, 19 - mpaWidth, 30
            )
        );

        //run it!
        FishState state = new FishState(seed);
        state.setScenario(scenario);
        state.start();
        while (state.getYear() < YEARS_PER_RUN) {
            state.schedule.step(state);
            //"run,width,failure,day,illegal,cashflow,biomass,landings"
            writer.write(
                seed + "," +
                    mpaWidth + "," +
                    failureThreshold + "," +
                    state.getDay() + "," +
                    state.getDailyDataSet().getLatestObservation("% of Illegal Tows") + "," +
                    state.getDailyDataSet().getLatestObservation("Average Cash-Flow") + "," +
                    state.getDailyDataSet().getLatestObservation("Biomass Species 0") + "," +
                    state.getDailyDataSet().getLatestObservation("Species 0 Landings") + "\n"
            );
            writer.flush();
        }


    }

    public static void cheatingSweepAdaptive() throws IOException {

        FileWriter writer = new FileWriter(MAIN_DIRECTORY.resolve("cheating_adaptive.csv").toFile());
        writer.write("run,width,failure,day,illegal,cashflow,biomass,landings" + '\n');
        writer.flush();

        for (int run = 0; run < RUNS; run++) {
            for (int width = MIN_MPA_WIDTH; width <= MAX_MPA_WIDTH; width++) {
                for (double failure = MIN_ADAPTIVE_THRESHOLD;
                     failure <= MAX_ADAPTIVE_THRESHOLD;
                     failure = FishStateUtilities.round(failure + 0.1)) {


                    adaptiveCheatingRun(width, failure, writer, run);
                }

            }
        }

        writer.flush();
        writer.close();


    }

    public static void adaptiveCheatingRun(
        int mpaWidth, double adaptiveThreshold,
        FileWriter writer, int seed
    ) throws IOException {
        System.out.println("starting: " + seed + "," +
            mpaWidth + "," +
            adaptiveThreshold);
        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(
            new FileReader(MAIN_DIRECTORY.resolve("cheating.yaml").toFile()),
            PrototypeScenario.class
        );

        //set it up!
        ((SNALSARDestinationFactory) scenario.getDestinationStrategy()).setFailureThreshold(
            new AverageProfitsThresholdFactory(adaptiveThreshold)
        );
        ((ProtectedAreasOnlyFactory) scenario.getRegulation()).getStartingMPAs().clear();
        ((ProtectedAreasOnlyFactory) scenario.getRegulation()).getStartingMPAs().add(
            new StartingMPA(
                0, 0, 19 - mpaWidth, 30
            )
        );

        //run it!
        FishState state = new FishState(seed);
        state.setScenario(scenario);
        state.start();
        while (state.getYear() < YEARS_PER_RUN) {
            state.schedule.step(state);
            //"run,width,failure,day,illegal,cashflow,biomass,landings"
            writer.write(
                seed + "," +
                    mpaWidth + "," +
                    adaptiveThreshold + "," +
                    state.getDay() + "," +
                    state.getDailyDataSet().getLatestObservation("% of Illegal Tows") + "," +
                    state.getDailyDataSet().getLatestObservation("Average Cash-Flow") + "," +
                    state.getDailyDataSet().getLatestObservation("Biomass Species 0") + "," +
                    state.getDailyDataSet().getLatestObservation("Species 0 Landings") + "\n"
            );
            writer.flush();
        }


    }


}
