/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

import com.esotericsoftware.minlog.Log;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.network.ClubNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.network.FixedNumberOfEdges;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.*;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class FriendsAndTac {


    private static final Path DIRECTORY = Paths.get("docs", "20180510 matt");
    private static final Path OUTPUT_DIRECTORY = Paths.get("docs", "20180510 matt","basic");
    private static final String SCENARIO_NAME = "basic.yaml";
    public static final int NUMBER_OF_RUNS = 1000;
    public static final int NUMBER_OF_FISHERS = 20;
    public static final int NUMBER_OF_YEARS_NOT_RECORDED = 1;
    public static final int NUMBER_OF_YEARS_FISHING = 5; //20;
    public static final int MIN_NUMBER_OF_FRIENDS = 0;
    public static final int MAX_NUMBER_OF_FRIENDS = 20;


    private static final LinkedHashMap<String, AlgorithmFactory<? extends Regulation>> regulations =
            new LinkedHashMap<>();
    public static final String COMMON_HEADINGS = "regulation,run,year";

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


        IQMonoFactory iq = new IQMonoFactory();
        iq.setIndividualQuota(new FixedDoubleParameter(10000d));
        regulations.put(
                "high_iq",
                iq
        );


        MultiITQFactory itq = new MultiITQFactory();
        itq.setQuotaFirstSpecie(new FixedDoubleParameter(10000d));
        itq.setAllowMultipleTrades(true);
        //itq.setIndividualQuota(new FixedDoubleParameter(5000d));
        regulations.put(
                "high_itq",
                itq
        );



    }



    private static final LinkedHashMap<String, Function<Fisher,String>> columns =
            new LinkedHashMap<>();
    static {

        columns.put("id",
                    new Function<Fisher, String>() {
                        @Override
                        public String apply(Fisher fisher) {
                            return String.valueOf(fisher.getID());
                        }
                    });

        columns.put("successors",
                    new Function<Fisher, String>() {
                        @Override
                        public String apply(Fisher fisher) {
                            Collection<Fisher> directedNeighbors = fisher.getSocialNetwork().getDirectedNeighbors(
                                    fisher);
                            return String.valueOf(directedNeighbors != null ? directedNeighbors.size() : 0);
                        }
                    });

        columns.put("predecessors",
                    new Function<Fisher, String>() {
                        @Override
                        public String apply(Fisher fisher) {
                            Collection<Fisher> predecessors = fisher.getSocialNetwork().getPredecessors(fisher);
                            return String.valueOf(predecessors != null ? predecessors.size() : 0);
                        }
                    });

        columns.put("cash_flow",
                    new Function<Fisher, String>() {
                        @Override
                        public String apply(Fisher fisher) {
                            return String.valueOf(fisher.getLatestYearlyObservation(
                                    FisherYearlyTimeSeries.CASH_FLOW_COLUMN));
                        }
                    });
        columns.put("landings",
                    new Function<Fisher, String>() {
                        @Override
                        public String apply(Fisher fisher) {
                            return String.valueOf(fisher.getLatestYearlyObservation(
                                    "Species 0 " + AbstractMarket.LANDINGS_COLUMN_NAME));
                        }
                    });

    }

    public static void main(String[] args) throws IOException {
        //spreadOut();
        spreadOutClub();
        //allEqual(null);
        //allEqualRaw(null);
        //allEqualClubs();
    }



    public static void allEqualClubs() throws IOException {

        Path scenarioFile = DIRECTORY.resolve(SCENARIO_NAME);
        if(!OUTPUT_DIRECTORY.toFile().exists())
            OUTPUT_DIRECTORY.toFile().mkdir();
        File outputFile = OUTPUT_DIRECTORY.resolve("clubs2.csv").toFile();
        FileWriter fileWriter = writeHeading(outputFile, COMMON_HEADINGS);

        for (Map.Entry<String, AlgorithmFactory<? extends Regulation>> regulation : regulations
                .entrySet()) {

            for (int friends = MIN_NUMBER_OF_FRIENDS; friends <= MAX_NUMBER_OF_FRIENDS; friends++)
            {
                for (int run = 0; run < NUMBER_OF_RUNS; run++) {

                    Log.info("STARTING WITH  CLUB SCENARIO " + regulation.getKey() + " - RUN " + run + " - FRIENDS " + friends);
                    FishState state = new FishState(run);
                    FishYAML yaml = new FishYAML();
                    PrototypeScenario scenario = yaml.loadAs(
                            new FileReader(
                                    scenarioFile.toFile()
                            ), PrototypeScenario.class
                    );
                    state.setScenario(scenario);
                    scenario.setFishers(NUMBER_OF_FISHERS);

                    //these runs have all the same number of friends

                    ClubNetworkBuilder clubs = new ClubNetworkBuilder();
                    clubs.setClubSize(new FixedDoubleParameter(friends));
                    scenario.setNetworkBuilder(clubs);



                    scenario.setRegulation(regulation.getValue());


                    state.start();
                    while (state.getYear() <= NUMBER_OF_YEARS_NOT_RECORDED)
                        state.schedule.step(state);
                    state.schedule.step(state);

                    while (state.getYear() <= NUMBER_OF_YEARS_FISHING) {
                        state.schedule.step(state);
                        //first day of the year!
                        if(state.getDayOfTheYear()==1)
                        {
                            String commonRows = regulation.getKey() + "," + run +"," + state.getYear();
                            for (Fisher fisher : state.getFishers()) {
                                writeLine(fisher,fileWriter,commonRows);
                            }
                        }
                    }
                }


            }
        }
        fileWriter.close();

    }

    public static void spreadOutClub() throws IOException {

        Path scenarioFile = DIRECTORY.resolve(SCENARIO_NAME);
        if(!OUTPUT_DIRECTORY.toFile().exists())
            OUTPUT_DIRECTORY.toFile().mkdir();
        File outputFile = OUTPUT_DIRECTORY.resolve("spread_clubs_2.csv").toFile();
        FileWriter fileWriter = writeHeading(outputFile, COMMON_HEADINGS);

        for (Map.Entry<String, AlgorithmFactory<? extends Regulation>> regulation : regulations
                .entrySet()) {

            for (int run = 0; run < NUMBER_OF_RUNS; run++) {

                Log.info("STARTING WITH SPREAD SCENARIO " + regulation.getKey() + " - RUN " + run);
                FishState state = new FishState(run);
                FishYAML yaml = new FishYAML();
                PrototypeScenario scenario = yaml.loadAs(
                        new FileReader(
                                scenarioFile.toFile()
                        ), PrototypeScenario.class
                );
                state.setScenario(scenario);
                scenario.setFishers(NUMBER_OF_FISHERS);

                scenario.setRegulation(regulation.getValue());

                //these runs have all the same number of friends
                ClubNetworkBuilder networkBuilder = new ClubNetworkBuilder();
                networkBuilder.setClubSize(new UniformDoubleParameter(MIN_NUMBER_OF_FRIENDS,
                                                                    MAX_NUMBER_OF_FRIENDS));
                scenario.setNetworkBuilder(networkBuilder);


                state.start();
                while (state.getYear() <= NUMBER_OF_YEARS_NOT_RECORDED)
                    state.schedule.step(state);
                state.schedule.step(state);

                while (state.getYear() <= NUMBER_OF_YEARS_FISHING) {
                    state.schedule.step(state);
                    //first day of the year!
                    if(state.getDayOfTheYear()==1)
                    {
                        String commonRows = regulation.getKey() + "," + run +"," + state.getYear();
                        for (Fisher fisher : state.getFishers()) {
                            writeLine(fisher,fileWriter,commonRows);
                        }
                    }
                }



            }
        }
        fileWriter.close();

    }

    public static void spreadOut() throws IOException {

        Path scenarioFile = DIRECTORY.resolve(SCENARIO_NAME);
        if(!OUTPUT_DIRECTORY.toFile().exists())
            OUTPUT_DIRECTORY.toFile().mkdir();
        File outputFile = OUTPUT_DIRECTORY.resolve("spread.csv").toFile();
        FileWriter fileWriter = writeHeading(outputFile, COMMON_HEADINGS);

        for (Map.Entry<String, AlgorithmFactory<? extends Regulation>> regulation : regulations
                .entrySet()) {

            for (int run = 0; run < NUMBER_OF_RUNS; run++) {

                Log.info("STARTING WITH SPREAD SCENARIO " + regulation.getKey() + " - RUN " + run);
                FishState state = new FishState(run);
                FishYAML yaml = new FishYAML();
                PrototypeScenario scenario = yaml.loadAs(
                        new FileReader(
                                scenarioFile.toFile()
                        ), PrototypeScenario.class
                );
                state.setScenario(scenario);
                scenario.setFishers(NUMBER_OF_FISHERS);

                scenario.setRegulation(regulation.getValue());

                //these runs have all the same number of friends
                FixedNumberOfEdges networkBuilder = new FixedNumberOfEdges();
                networkBuilder.setEdges(new FixedDoubleParameter(MAX_NUMBER_OF_FRIENDS));
                //set some runs for

                scenario.setNetworkBuilder(networkBuilder);


                state.start();
                while (state.getYear() <= NUMBER_OF_YEARS_NOT_RECORDED)
                    state.schedule.step(state);
                state.schedule.step(state);

                while (state.getYear() <= NUMBER_OF_YEARS_FISHING) {
                    state.schedule.step(state);
                    //first day of the year!
                    if(state.getDayOfTheYear()==1)
                    {
                        String commonRows = regulation.getKey() + "," + run +"," + state.getYear();
                        for (Fisher fisher : state.getFishers()) {
                            writeLine(fisher,fileWriter,commonRows);
                        }
                    }
                }



            }
        }
        fileWriter.close();

    }

    public static void allEqual(String[] args) throws IOException {

        Path scenarioFile = DIRECTORY.resolve(SCENARIO_NAME);
        if(!OUTPUT_DIRECTORY.toFile().exists())
            OUTPUT_DIRECTORY.toFile().mkdir();
        File outputFile = OUTPUT_DIRECTORY.resolve("all_equal.csv").toFile();
        FileWriter fileWriter = writeHeading(outputFile, COMMON_HEADINGS);

        Files.copy(scenarioFile,
                   OUTPUT_DIRECTORY.resolve("scenario.yaml"),
                   StandardCopyOption.REPLACE_EXISTING);

        for (Map.Entry<String, AlgorithmFactory<? extends Regulation>> regulation : regulations
                .entrySet()) {

            for (int friends = MIN_NUMBER_OF_FRIENDS; friends <= MAX_NUMBER_OF_FRIENDS; friends++)
            {
                for (int run = 0; run < NUMBER_OF_RUNS; run++) {

                    Log.info("STARTING WITH SCENARIO " + regulation.getKey() + " - RUN " + run);
                    FishState state = new FishState(run);
                    FishYAML yaml = new FishYAML();
                    PrototypeScenario scenario = yaml.loadAs(
                            new FileReader(
                                    scenarioFile.toFile()
                            ), PrototypeScenario.class
                    );
                    state.setScenario(scenario);
                    scenario.setFishers(NUMBER_OF_FISHERS);

                    //these runs have all the same number of friends
                    EquidegreeBuilder networkBuilder = new EquidegreeBuilder();
                    networkBuilder.setDegree(new FixedDoubleParameter(friends));
                    scenario.setNetworkBuilder(networkBuilder);

                    scenario.setRegulation(regulation.getValue());


                    state.start();
                    while (state.getYear() <= NUMBER_OF_YEARS_NOT_RECORDED)
                        state.schedule.step(state);
                    state.schedule.step(state);

                    while (state.getYear() <= NUMBER_OF_YEARS_FISHING) {
                        state.schedule.step(state);
                        //first day of the year!
                        if(state.getDayOfTheYear()==1)
                        {
                            String commonRows = regulation.getKey() + "," + run +"," + state.getYear();
                            for (Fisher fisher : state.getFishers()) {
                                writeLine(fisher,fileWriter,commonRows);
                            }
                        }
                    }
                }


            }
        }
        fileWriter.close();

    }


    public static void allEqualRaw(String[] args) throws IOException {

        Path scenarioFile = DIRECTORY.resolve(SCENARIO_NAME);
        if(!OUTPUT_DIRECTORY.toFile().exists())
            OUTPUT_DIRECTORY.toFile().mkdir();
        File outputFile = OUTPUT_DIRECTORY.resolve("all_equal_raw.csv").toFile();
        FileWriter fileWriter = writeHeading(outputFile, COMMON_HEADINGS +",edges");

        Files.copy(scenarioFile,
                   OUTPUT_DIRECTORY.resolve("scenario.yaml"),
                   StandardCopyOption.REPLACE_EXISTING);

        for (Map.Entry<String, AlgorithmFactory<? extends Regulation>> regulation : regulations
                .entrySet()) {

            for (int friends = MIN_NUMBER_OF_FRIENDS; friends <= MAX_NUMBER_OF_FRIENDS*10; friends++)
            {
                for (int run = 0; run < NUMBER_OF_RUNS; run++) {

                    Log.info("STARTING WITH SCENARIO " + regulation.getKey() + " - RUN " + run);
                    FishState state = new FishState(run);
                    FishYAML yaml = new FishYAML();
                    PrototypeScenario scenario = yaml.loadAs(
                            new FileReader(
                                    scenarioFile.toFile()
                            ), PrototypeScenario.class
                    );
                    state.setScenario(scenario);
                    scenario.setFishers(NUMBER_OF_FISHERS);

                    //these runs have all the same number of friends
                    FixedNumberOfEdges networkBuilder = new FixedNumberOfEdges();
                    networkBuilder.setEdges(new FixedDoubleParameter(friends));
                    scenario.setNetworkBuilder(networkBuilder);

                    scenario.setRegulation(regulation.getValue());


                    state.start();
                    while (state.getYear() <= NUMBER_OF_YEARS_NOT_RECORDED)
                        state.schedule.step(state);
                    state.schedule.step(state);

                    while (state.getYear() <= NUMBER_OF_YEARS_FISHING) {
                        state.schedule.step(state);
                        //first day of the year!
                        if(state.getDayOfTheYear()==1)
                        {
                            String commonRows = regulation.getKey() + "," + run +"," + state.getYear() +"," + friends;
                            for (Fisher fisher : state.getFishers()) {
                                writeLine(fisher,fileWriter,commonRows);
                            }
                        }
                    }
                }


            }
        }
        fileWriter.close();

    }

    @NotNull
    private static FileWriter writeHeading(File outputFile, final String commonRows) throws IOException {
        FileWriter writer = new FileWriter(outputFile);
        //writer.write("price_low,price_high,landings,earnings,cash-flow,landings_0,landings_1,landings_2,discarding_agents,catches_0");
        writer.write(commonRows);
        for (Map.Entry<String, Function<Fisher, String>> column : columns.entrySet()) {
            writer.write(",");
            writer.write(column.getKey());
        }
        writer.write("\n");
        writer.flush();
        return writer;

    }

    //common rows is all the rows that do not depend on the fisher
    private static void writeLine(Fisher fisher, FileWriter writer, String commonRows) throws IOException {
        writer.write(commonRows);
        for (Map.Entry<String, Function<Fisher, String>> column : columns.entrySet()) {
            writer.write(",");
            writer.write(column.getValue().apply(fisher));
        }
        writer.write("\n");
        writer.flush();


    }

}
