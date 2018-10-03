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
import uk.ac.ox.oxfish.fisher.strategies.departing.MaxHoursPerYearDepartingStrategy;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class Slice0Sweeps {


    public static final String FILENAME = "pessimistic";
    public static final String DIRECTORY = "docs/indonesia_hub/runs/712/slice0/policy/";


    public static void main(String[] args) throws IOException {
        policy("large",true);
    }

    public static void policy(String name,
                              boolean onlyLarge) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, FILENAME + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut=200; maxDaysOut>=50; maxDaysOut--) {

            BatchRunner runner = new BatchRunner(
                    Paths.get(DIRECTORY,
                              FILENAME + ".yaml"),
                    15,
                    Lists.newArrayList(
                            "Snapper Landings",
                            "Snapper Landings of population0",
                            "Snapper Landings of population1",
                            "Snapper Landings of population2",
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
                            "Biomass Snapper"

                    ),
                    Paths.get(DIRECTORY,
                              FILENAME),
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
                                                    for (Fisher fisher :
                                                            ((FishState) simState).getFishers()) {

                                                        if(!onlyLarge || fisher.getTags().contains("big"))
                                                            fisher.setDepartingStrategy(new MaxHoursPerYearDepartingStrategy(
                                                                    finalMaxDaysOut *24
                                                            ));
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


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMaxDaysOut).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            StringBuffer tidy = new StringBuffer();
            runner.run(tidy);
            fileWriter.write(tidy.toString());
            fileWriter.flush();
            //   }

        }
        fileWriter.close();
    }




}
