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

package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.collect.Lists;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.regs.MaxHoursOutRegulation;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class Slice1Sweeps {

    public static final String DIRECTORY = "docs/indonesia_hub/runs/712/slice3/policy/";
    public static final int MIN_DAYS_OUT = 50;
    public static final int RUNS_PER_POLICY = 10;


    public static void main(String[] args) throws IOException {
        //policy("large", new String[]{"big"}, "very_pessimistic");
        //policy("medium", new String[]{"big","medium"}, "very_pessimistic");
        //policy("all", new String[]{"big","small","medium"}, "very_pessimistic");


        //policy("large", new String[]{"big"}, "optimistic");
        //policy("medium", new String[]{"big","medium"}, "optimistic");
        //policy("all", new String[]{"big","small","medium"}, "optimistic");

        //policy("large", new String[]{"big"}, "pessimistic_2");
        //policy("medium", new String[]{"big","medium"}, "pessimistic_2");
        //policy("all", new String[]{"big","small","medium"}, "pessimistic_2");


       // enforcement("all","small","very_pessimistic");


//        policy("large", new String[]{"big"}, "fixed_recruits", 4);
//        policy("medium", new String[]{"big","medium"}, "fixed_recruits", 4);
//        policy("all", new String[]{"big","small","medium"}, "fixed_recruits", 4);
//
        policy("large", new String[]{"big"}, "optimistic_recruits", 1);
        policy("medium", new String[]{"big","medium"}, "optimistic_recruits", 1);
        policy("all", new String[]{"big","small","medium"}, "optimistic_recruits", 1);


    }

    public static void policy(
            String name,
            String[] modifiedTags, final String filename, final int shockYear) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut = 200; maxDaysOut>= MIN_DAYS_OUT; maxDaysOut-=10) {

            BatchRunner runner = new BatchRunner(
                    Paths.get(DIRECTORY,
                              filename + ".yaml"),
                    15,
                    Lists.newArrayList(
                            "Average Cash-Flow",
                            "Average Cash-Flow of population0",
                            "Average Cash-Flow of population1",
                            "Average Cash-Flow of population2",
                            "Average Number of Trips of population0",
                            "Average Number of Trips of population1",
                            "Average Number of Trips of population2",
                            "Average Distance From Port of population0",
                            "Average Distance From Port of population1",
                            "Average Distance From Port of population2",
                            "Average Trip Duration of population0",
                            "Average Trip Duration of population1",
                            "Average Trip Duration of population2",
                            "Epinephelus areolatus Landings of population0",
                            "Pristipomoides multidens Landings of population0",
                            "Lutjanus malabaricus Landings of population0",
                            "Lutjanus erythropterus Landings of population0",
                            "Others Landings of population0",

                            "Epinephelus areolatus Landings of population1",
                            "Pristipomoides multidens Landings of population1",
                            "Lutjanus malabaricus Landings of population1",
                            "Lutjanus erythropterus Landings of population1",
                            "Others Landings of population1",
                            "Epinephelus areolatus Landings of population2",
                            "Pristipomoides multidens Landings of population2",
                            "Lutjanus malabaricus Landings of population2",
                            "Lutjanus erythropterus Landings of population2",
                            "Others Landings of population2",

                            "Biomass Epinephelus areolatus",
                            "Biomass Pristipomoides multidens",
                            "Biomass Lutjanus malabaricus",
                            "Biomass Lutjanus erythropterus",
                            "Total Landings of population0",
                            "Total Landings of population1",
                            "Total Landings of population2"

                    ),
                    Paths.get(DIRECTORY,
                              filename),
                    null,
                    System.currentTimeMillis(),
                    -1
            );


            int finalMaxDaysOut = maxDaysOut;

            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            runner.setScenarioSetup(
                    scenario -> {

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;
                        flexible.getPlugins().add(
                                fishState -> new AdditionalStartable() {
                                    @Override
                                    public void start(FishState model) {

                                        model.scheduleOnceAtTheBeginningOfYear(
                                                (Steppable) simState -> {
                                                    fisherloop:
                                                    for (Fisher fisher :
                                                            ((FishState) simState).getFishers()) {

                                                        for (String tag : modifiedTags) {
                                                            if (fisher.getTags().contains(tag)) {
                                                                fisher.setRegulation(
                                                                        new MaxHoursOutRegulation(
                                                                                new ProtectedAreasOnly(),
                                                                                finalMaxDaysOut*24d
                                                                        ));
                                                                continue fisherloop;
                                                            }
                                                        }
                                                    }
                                                },
                                                StepOrder.DAWN,
                                                shockYear
                                        );


                                    }

                                    @Override
                                    public void turnOff() {

                                    }
                                }
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMaxDaysOut).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }




    public static void enforcement(
            String name,
            String cheatingTag, final String filename) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+"_enforcement.csv").toFile());
        fileWriter.write("run,year,enforcement,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut=200; maxDaysOut>=50; maxDaysOut-=10) {
            for(double probabilityOfCheating = 0; probabilityOfCheating<=1; probabilityOfCheating+=.2) {

                probabilityOfCheating = FishStateUtilities.round(probabilityOfCheating);
                BatchRunner runner = new BatchRunner(
                        Paths.get(DIRECTORY,
                                  filename + ".yaml"),
                        15,
                        Lists.newArrayList(
                                "Average Cash-Flow",
                                "Average Cash-Flow of population0",
                                "Average Cash-Flow of population1",
                                "Average Cash-Flow of population2",
                                "Average Number of Trips of population0",
                                "Average Number of Trips of population1",
                                "Average Number of Trips of population2",
                                "Average Distance From Port of population0",
                                "Average Distance From Port of population1",
                                "Average Distance From Port of population2",
                                "Average Trip Duration of population0",
                                "Average Trip Duration of population1",
                                "Average Trip Duration of population2",
                                "Epinephelus areolatus Landings of population0",
                                "Pristipomoides multidens Landings of population0",
                                "Lutjanus malabaricus Landings of population0",
                                "Lutjanus erythropterus Landings of population0",
                                "Others Landings of population0",

                                "Epinephelus areolatus Landings of population1",
                                "Pristipomoides multidens Landings of population1",
                                "Lutjanus malabaricus Landings of population1",
                                "Lutjanus erythropterus Landings of population1",
                                "Others Landings of population1",
                                "Epinephelus areolatus Landings of population2",
                                "Pristipomoides multidens Landings of population2",
                                "Lutjanus malabaricus Landings of population2",
                                "Lutjanus erythropterus Landings of population2",
                                "Others Landings of population2",

                                "Biomass Epinephelus areolatus",
                                "Biomass Pristipomoides multidens",
                                "Biomass Lutjanus malabaricus",
                                "Biomass Lutjanus erythropterus",
                                "Total Landings of population0",
                                "Total Landings of population1",
                                "Total Landings of population2"

                        ),
                        Paths.get(DIRECTORY,
                                  filename),
                        null,
                        System.currentTimeMillis(),
                        -1
                );


                int finalMaxDaysOut = maxDaysOut;

                //basically we want year 4 to change big boats regulations.
                //because I coded "run" poorly, we have to go through this series of pirouettes
                //to get it done right
                double finalProbabilityOfCheating = probabilityOfCheating;
                runner.setScenarioSetup(
                        scenario -> {

                            //at year 4, impose regulation
                            FlexibleScenario flexible = (FlexibleScenario) scenario;
                            flexible.getPlugins().add(
                                    fishState -> new AdditionalStartable() {
                                        @Override
                                        public void start(FishState model) {

                                            model.scheduleOnceAtTheBeginningOfYear(
                                                    (Steppable) simState -> {
                                                        fisherloop:
                                                        for (Fisher fisher :
                                                                ((FishState) simState).getFishers()) {

                                                            if (!fisher.getTags().contains(cheatingTag)) {
                                                                fisher.setRegulation(
                                                                        new FishingSeason(true, finalMaxDaysOut));
                                                            } else {
                                                                if (!model.getRandom().nextBoolean(
                                                                        finalProbabilityOfCheating))
                                                                    fisher.setRegulation(
                                                                            new FishingSeason(true, finalMaxDaysOut));

                                                            }


                                                        }
                                                    },
                                                    StepOrder.DAWN,
                                                    4
                                            );


                                        }

                                        @Override
                                        public void turnOff() {

                                        }
                                    }
                            );

                        }
                );


                final String cheatingString = Double.toString(probabilityOfCheating);
                runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                    @Override
                    public void consume(StringBuffer writer, FishState model, Integer year) {
                        writer.append(cheatingString).append(",").append(finalMaxDaysOut).append(",");
                    }
                });


                //while (runner.getRunsDone() < 1) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
                //   }
            }
        }
        fileWriter.close();
    }
}
