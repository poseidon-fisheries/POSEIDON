/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.network.ClubNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.network.FixedNumberOfEdges;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.IQMonoFactory;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public class FriendsAndTac {

    public static final int NUMBER_OF_RUNS = 1000;
    public static final int NUMBER_OF_FISHERS = 20;
    public static final int NUMBER_OF_YEARS_NOT_RECORDED = 1;
    public static final int NUMBER_OF_YEARS_FISHING = 5; // 20;
    public static final int MIN_NUMBER_OF_FRIENDS = 0;
    public static final int MAX_NUMBER_OF_FRIENDS = 20;
    public static final String COMMON_HEADINGS = "regulation,run,year";
    private static final Path DIRECTORY = Paths.get("docs", "20180510 matt");
    private static final Path OUTPUT_DIRECTORY = Paths.get("docs", "20180510 matt", "basic");
    private static final String SCENARIO_NAME = "basic.yaml";
    private static final LinkedHashMap<String, AlgorithmFactory<? extends Regulation>> regulations =
        new LinkedHashMap<>();
    private static final LinkedHashMap<String, Function<Fisher, String>> columns =
        new LinkedHashMap<>();

    static {

//        regulations.put(
//                "anarchy",
//                new AnarchyFactory()
//        );
//
//
//        TACMonoFactory tac = new TACMonoFactory();
//        tac.setQuota(new FixedDoubleParameter(10000d*NUMBER_OF_FISHERS));
//        regulations.put(
//                "high_tac",
//                tac
//        );

        final IQMonoFactory iq = new IQMonoFactory();
        iq.setIndividualQuota(new FixedDoubleParameter(10000d));
        regulations.put(
            "high_iq",
            iq
        );

        final MultiITQFactory itq = new MultiITQFactory();
        itq.setQuotaFirstSpecie(new FixedDoubleParameter(10000d));
        itq.setAllowMultipleTrades(true);
        // itq.setIndividualQuota(new FixedDoubleParameter(5000d));
        regulations.put(
            "high_itq",
            itq
        );

    }

    static {

        columns.put(
            "id",
            fisher -> String.valueOf(fisher.getID())
        );

        columns.put(
            "successors",
            fisher -> {
                final Collection<Fisher> directedNeighbors = fisher.getSocialNetwork().getDirectedNeighbors(
                    fisher);
                return String.valueOf(directedNeighbors != null ? directedNeighbors.size() : 0);
            }
        );

        columns.put(
            "predecessors",
            fisher -> {
                final Collection<Fisher> predecessors = fisher.getSocialNetwork().getPredecessors(fisher);
                return String.valueOf(predecessors != null ? predecessors.size() : 0);
            }
        );

        columns.put(
            "cash_flow",
            fisher -> String.valueOf(fisher.getLatestYearlyObservation(
                FisherYearlyTimeSeries.CASH_FLOW_COLUMN))
        );
        columns.put(
            "landings",
            fisher -> String.valueOf(fisher.getLatestYearlyObservation(
                "Species 0 " + AbstractMarket.LANDINGS_COLUMN_NAME))
        );

    }

    public static void main(final String[] args) throws IOException {
        // spreadOut();
        spreadOutClub();
        // allEqual(null);
        // allEqualRaw(null);
        // allEqualClubs();
    }

    public static void spreadOutClub() throws IOException {

        final Path scenarioFile = DIRECTORY.resolve(SCENARIO_NAME);
        if (!OUTPUT_DIRECTORY.toFile().exists())
            OUTPUT_DIRECTORY.toFile().mkdir();
        final File outputFile = OUTPUT_DIRECTORY.resolve("spread_clubs_2.csv").toFile();
        final FileWriter fileWriter = writeHeading(outputFile, COMMON_HEADINGS);

        for (final Map.Entry<String, AlgorithmFactory<? extends Regulation>> regulation : regulations
            .entrySet()) {

            for (int run = 0; run < NUMBER_OF_RUNS; run++) {

                Logger.getGlobal().info("STARTING WITH SPREAD SCENARIO " + regulation.getKey() + " - RUN " + run);
                final FishState state = new FishState(run);
                final FishYAML yaml = new FishYAML();
                final PrototypeScenario scenario = yaml.loadAs(
                    new FileReader(
                        scenarioFile.toFile()
                    ), PrototypeScenario.class
                );
                state.setScenario(scenario);
                scenario.setFishers(NUMBER_OF_FISHERS);

                scenario.setRegulation(regulation.getValue());

                // these runs have all the same number of friends
                final ClubNetworkBuilder networkBuilder = new ClubNetworkBuilder();
                networkBuilder.setClubSize(new UniformDoubleParameter(
                    MIN_NUMBER_OF_FRIENDS,
                    MAX_NUMBER_OF_FRIENDS
                ));
                scenario.setNetworkBuilder(networkBuilder);

                state.start();
                while (state.getYear() <= NUMBER_OF_YEARS_NOT_RECORDED)
                    state.schedule.step(state);
                state.schedule.step(state);

                while (state.getYear() <= NUMBER_OF_YEARS_FISHING) {
                    state.schedule.step(state);
                    // first day of the year!
                    if (state.getDayOfTheYear() == 1) {
                        final String commonRows = regulation.getKey() + "," + run + "," + state.getYear();
                        for (final Fisher fisher : state.getFishers()) {
                            writeLine(fisher, fileWriter, commonRows);
                        }
                    }
                }

            }
        }
        fileWriter.close();

    }

    private static FileWriter writeHeading(
        final File outputFile,
        final String commonRows
    ) throws IOException {
        final FileWriter writer = new FileWriter(outputFile);
        // writer.write("price_low,price_high,landings,earnings,cash-flow,landings_0,landings_1,landings_2,
        // discarding_agents,catches_0");
        writer.write(commonRows);
        for (final Map.Entry<String, Function<Fisher, String>> column : columns.entrySet()) {
            writer.write(",");
            writer.write(column.getKey());
        }
        writer.write("\n");
        writer.flush();
        return writer;

    }

    // common rows is all the rows that do not depend on the fisher
    private static void writeLine(
        final Fisher fisher,
        final FileWriter writer,
        final String commonRows
    ) throws IOException {
        writer.write(commonRows);
        for (final Map.Entry<String, Function<Fisher, String>> column : columns.entrySet()) {
            writer.write(",");
            writer.write(column.getValue().apply(fisher));
        }
        writer.write("\n");
        writer.flush();

    }

    public static void allEqualClubs() throws IOException {

        final Path scenarioFile = DIRECTORY.resolve(SCENARIO_NAME);
        if (!OUTPUT_DIRECTORY.toFile().exists())
            OUTPUT_DIRECTORY.toFile().mkdir();
        final File outputFile = OUTPUT_DIRECTORY.resolve("clubs2.csv").toFile();
        final FileWriter fileWriter = writeHeading(outputFile, COMMON_HEADINGS);

        for (final Map.Entry<String, AlgorithmFactory<? extends Regulation>> regulation : regulations
            .entrySet()) {

            for (int friends = MIN_NUMBER_OF_FRIENDS; friends <= MAX_NUMBER_OF_FRIENDS; friends++) {
                for (int run = 0; run < NUMBER_OF_RUNS; run++) {

                    Logger.getGlobal()
                        .info("STARTING WITH  CLUB SCENARIO " +
                            regulation.getKey() +
                            " - RUN " +
                            run +
                            " - FRIENDS " +
                            friends);
                    final FishState state = new FishState(run);
                    final FishYAML yaml = new FishYAML();
                    final PrototypeScenario scenario = yaml.loadAs(
                        new FileReader(
                            scenarioFile.toFile()
                        ), PrototypeScenario.class
                    );
                    state.setScenario(scenario);
                    scenario.setFishers(NUMBER_OF_FISHERS);

                    // these runs have all the same number of friends

                    final ClubNetworkBuilder clubs = new ClubNetworkBuilder();
                    clubs.setClubSize(new FixedDoubleParameter(friends));
                    scenario.setNetworkBuilder(clubs);

                    scenario.setRegulation(regulation.getValue());

                    state.start();
                    while (state.getYear() <= NUMBER_OF_YEARS_NOT_RECORDED)
                        state.schedule.step(state);
                    state.schedule.step(state);

                    while (state.getYear() <= NUMBER_OF_YEARS_FISHING) {
                        state.schedule.step(state);
                        // first day of the year!
                        if (state.getDayOfTheYear() == 1) {
                            final String commonRows = regulation.getKey() + "," + run + "," + state.getYear();
                            for (final Fisher fisher : state.getFishers()) {
                                writeLine(fisher, fileWriter, commonRows);
                            }
                        }
                    }
                }

            }
        }
        fileWriter.close();

    }

    public static void spreadOut() throws IOException {

        final Path scenarioFile = DIRECTORY.resolve(SCENARIO_NAME);
        if (!OUTPUT_DIRECTORY.toFile().exists())
            OUTPUT_DIRECTORY.toFile().mkdir();
        final File outputFile = OUTPUT_DIRECTORY.resolve("spread.csv").toFile();
        final FileWriter fileWriter = writeHeading(outputFile, COMMON_HEADINGS);

        for (final Map.Entry<String, AlgorithmFactory<? extends Regulation>> regulation : regulations
            .entrySet()) {

            for (int run = 0; run < NUMBER_OF_RUNS; run++) {

                Logger.getGlobal().info("STARTING WITH SPREAD SCENARIO " + regulation.getKey() + " - RUN " + run);
                final FishState state = new FishState(run);
                final FishYAML yaml = new FishYAML();
                final PrototypeScenario scenario = yaml.loadAs(
                    new FileReader(
                        scenarioFile.toFile()
                    ), PrototypeScenario.class
                );
                state.setScenario(scenario);
                scenario.setFishers(NUMBER_OF_FISHERS);

                scenario.setRegulation(regulation.getValue());

                // these runs have all the same number of friends
                final FixedNumberOfEdges networkBuilder = new FixedNumberOfEdges();
                networkBuilder.setEdges(new FixedDoubleParameter(MAX_NUMBER_OF_FRIENDS));
                // set some runs for

                scenario.setNetworkBuilder(networkBuilder);

                state.start();
                while (state.getYear() <= NUMBER_OF_YEARS_NOT_RECORDED)
                    state.schedule.step(state);
                state.schedule.step(state);

                while (state.getYear() <= NUMBER_OF_YEARS_FISHING) {
                    state.schedule.step(state);
                    // first day of the year!
                    if (state.getDayOfTheYear() == 1) {
                        final String commonRows = regulation.getKey() + "," + run + "," + state.getYear();
                        for (final Fisher fisher : state.getFishers()) {
                            writeLine(fisher, fileWriter, commonRows);
                        }
                    }
                }

            }
        }
        fileWriter.close();

    }

    public static void allEqual(final String[] args) throws IOException {

        final Path scenarioFile = DIRECTORY.resolve(SCENARIO_NAME);
        if (!OUTPUT_DIRECTORY.toFile().exists())
            OUTPUT_DIRECTORY.toFile().mkdir();
        final File outputFile = OUTPUT_DIRECTORY.resolve("all_equal.csv").toFile();
        final FileWriter fileWriter = writeHeading(outputFile, COMMON_HEADINGS);

        Files.copy(
            scenarioFile,
            OUTPUT_DIRECTORY.resolve("scenario.yaml"),
            StandardCopyOption.REPLACE_EXISTING
        );

        for (final Map.Entry<String, AlgorithmFactory<? extends Regulation>> regulation : regulations
            .entrySet()) {

            for (int friends = MIN_NUMBER_OF_FRIENDS; friends <= MAX_NUMBER_OF_FRIENDS; friends++) {
                for (int run = 0; run < NUMBER_OF_RUNS; run++) {

                    Logger.getGlobal().info("STARTING WITH SCENARIO " + regulation.getKey() + " - RUN " + run);
                    final FishState state = new FishState(run);
                    final FishYAML yaml = new FishYAML();
                    final PrototypeScenario scenario = yaml.loadAs(
                        new FileReader(
                            scenarioFile.toFile()
                        ), PrototypeScenario.class
                    );
                    state.setScenario(scenario);
                    scenario.setFishers(NUMBER_OF_FISHERS);

                    // these runs have all the same number of friends
                    final EquidegreeBuilder networkBuilder = new EquidegreeBuilder();
                    networkBuilder.setDegree(new FixedDoubleParameter(friends));
                    scenario.setNetworkBuilder(networkBuilder);

                    scenario.setRegulation(regulation.getValue());

                    state.start();
                    while (state.getYear() <= NUMBER_OF_YEARS_NOT_RECORDED)
                        state.schedule.step(state);
                    state.schedule.step(state);

                    while (state.getYear() <= NUMBER_OF_YEARS_FISHING) {
                        state.schedule.step(state);
                        // first day of the year!
                        if (state.getDayOfTheYear() == 1) {
                            final String commonRows = regulation.getKey() + "," + run + "," + state.getYear();
                            for (final Fisher fisher : state.getFishers()) {
                                writeLine(fisher, fileWriter, commonRows);
                            }
                        }
                    }
                }

            }
        }
        fileWriter.close();

    }

    public static void allEqualRaw(final String[] args) throws IOException {

        final Path scenarioFile = DIRECTORY.resolve(SCENARIO_NAME);
        if (!OUTPUT_DIRECTORY.toFile().exists())
            OUTPUT_DIRECTORY.toFile().mkdir();
        final File outputFile = OUTPUT_DIRECTORY.resolve("all_equal_raw.csv").toFile();
        final FileWriter fileWriter = writeHeading(outputFile, COMMON_HEADINGS + ",edges");

        Files.copy(
            scenarioFile,
            OUTPUT_DIRECTORY.resolve("scenario.yaml"),
            StandardCopyOption.REPLACE_EXISTING
        );

        for (final Map.Entry<String, AlgorithmFactory<? extends Regulation>> regulation : regulations
            .entrySet()) {

            for (int friends = MIN_NUMBER_OF_FRIENDS; friends <= MAX_NUMBER_OF_FRIENDS * 10; friends++) {
                for (int run = 0; run < NUMBER_OF_RUNS; run++) {

                    Logger.getGlobal().info("STARTING WITH SCENARIO " + regulation.getKey() + " - RUN " + run);
                    final FishState state = new FishState(run);
                    final FishYAML yaml = new FishYAML();
                    final PrototypeScenario scenario = yaml.loadAs(
                        new FileReader(
                            scenarioFile.toFile()
                        ), PrototypeScenario.class
                    );
                    state.setScenario(scenario);
                    scenario.setFishers(NUMBER_OF_FISHERS);

                    // these runs have all the same number of friends
                    final FixedNumberOfEdges networkBuilder = new FixedNumberOfEdges();
                    networkBuilder.setEdges(new FixedDoubleParameter(friends));
                    scenario.setNetworkBuilder(networkBuilder);

                    scenario.setRegulation(regulation.getValue());

                    state.start();
                    while (state.getYear() <= NUMBER_OF_YEARS_NOT_RECORDED)
                        state.schedule.step(state);
                    state.schedule.step(state);

                    while (state.getYear() <= NUMBER_OF_YEARS_FISHING) {
                        state.schedule.step(state);
                        // first day of the year!
                        if (state.getDayOfTheYear() == 1) {
                            final String commonRows =
                                regulation.getKey() + "," + run + "," + state.getYear() + "," + friends;
                            for (final Fisher fisher : state.getFishers()) {
                                writeLine(fisher, fileWriter, commonRows);
                            }
                        }
                    }
                }

            }
        }
        fileWriter.close();

    }

}
