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

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.boxcars.AbundanceGatherers;
import uk.ac.ox.oxfish.biology.boxcars.SprOracle;
import uk.ac.ox.oxfish.experiments.indonesia.Slice2SPR;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Consumer;

public class IndonesiaBatchRuns {


    public static final String FILENAME = "pessimistic_recruits_spinup";
    public static final String DIRECTORY = "docs/indonesia_hub/runs/712/slice3/calibration/results";
    public static final int YEARS_TO_RUN = 4;

    public static void main(String[] args) throws IOException {


        ArrayList<String> columnsToPrint = Lists.newArrayList(
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
                "Total Landings of population2",
                "SPR " + "Epinephelus areolatus" + " " + "100_areolatus",
                "SPR " + "Pristipomoides multidens" + " " + "100_multidens",
                "SPR " + "Lutjanus malabaricus" + " " + "100_malabaricus"

        );

        for(int i=0; i<25; i++) {
            columnsToPrint.add("Epinephelus areolatus Catches (kg) - age bin " + i);
            columnsToPrint.add("Pristipomoides multidens Catches (kg) - age bin " + i);
            columnsToPrint.add("Lutjanus malabaricus Catches (kg) - age bin " + i);
            columnsToPrint.add("Epinephelus areolatus Catches (kg) - age bin " + i);
            columnsToPrint.add("Epinephelus areolatus Abundance 0."+i+" at day " + 200);
            columnsToPrint.add("Lutjanus malabaricus Abundance 0."+i+" at day " + 200);
            columnsToPrint.add("Pristipomoides multidens Abundance 0."+i+" at day " + 200);


            columnsToPrint.add("Epinephelus areolatus Catches(#) 0."+i+" 100_areolatus");
            columnsToPrint.add("Lutjanus malabaricus Catches(#) 0."+i+" 100_malabaricus");
            columnsToPrint.add("Pristipomoides multidens Catches(#) 0."+i+" 100_multidens");
            columnsToPrint.add("Lutjanus erythropterus Catches(#) 0."+i+" 100_erythropterus");
        }
//        columnsToPrint.add("SPR Oracle - Epinephelus areolatus");
//        columnsToPrint.add("SPR Oracle - Pristipomoides multidens");
//        columnsToPrint.add("SPR Oracle - Lutjanus malabaricus");
        BatchRunner runner = new BatchRunner(
                Paths.get(DIRECTORY,
                          FILENAME + ".yaml"),
                YEARS_TO_RUN,
                columnsToPrint,
                Paths.get(DIRECTORY,
                          FILENAME),
                null,
                System.currentTimeMillis(),
                -1
        );

        //add SPR counters
        runner.setScenarioSetup(new Consumer<Scenario>() {
            @Override
            public void accept(Scenario scenario) {
                MersenneTwisterFast random = new MersenneTwisterFast();
                //add a full one
                String surveyTag = "100_areolatus";
                FlexibleScenario flexible = (FlexibleScenario) scenario;
                Slice2SPR.randomAreolatusSampling(flexible, surveyTag, 1, null);
                surveyTag = "100_multidens";
                Slice2SPR.randomMultidensSampling(flexible, surveyTag, 1, null);
                surveyTag = "100_malabaricus";
                Slice2SPR.randomMalabaricusSampling(flexible, surveyTag, 1, null);
                surveyTag = "100_erythropterus";
                Slice2SPR.randomErythropterusSampling(flexible, surveyTag, 1, null);



//                flexible.getPlugins().add(
//                        new AlgorithmFactory<AdditionalStartable>() {
//                            @Override
//                            public AdditionalStartable apply(FishState fishState) {
//                                return new SprOracle(
//                                        fishState.getBiology().getSpecie("Epinephelus areolatus"),
//                                        Slice2SPR.assumedLenghtAtMaturityAreolatus,
//                                        200,
//                                        Slice2SPR.VIRGIN_SPAWNING_BIOMASS_AREOLATUS
//                                );
//                            }
//                        }
//                );
//
//                flexible.getPlugins().add(
//                        new AlgorithmFactory<AdditionalStartable>() {
//                            @Override
//                            public AdditionalStartable apply(FishState fishState) {
//                                return new SprOracle(
//                                        fishState.getBiology().getSpecie("Pristipomoides multidens"),
//                                        Slice2SPR.assumedLenghtAtMaturityMultidens,
//                                        200,
//                                        Slice2SPR.VIRGIN_SPAWNING_BIOMASS_MULTIDENS
//
//                                );
//                            }
//                        }
//                );
//
//
//                flexible.getPlugins().add(
//                        new AlgorithmFactory<AdditionalStartable>() {
//                            @Override
//                            public AdditionalStartable apply(FishState fishState) {
//                                return new SprOracle(
//                                        fishState.getBiology().getSpecie("Lutjanus malabaricus"),
//                                        Slice2SPR.assumedLenghtAtMaturityMalabaricus,
//                                        200,
//                                        Slice2SPR.VIRGIN_SPAWNING_BIOMASS_MALABARICUS
//                                );
//                            }
//                        }
//                );

                flexible.getPlugins().add(
                        new AlgorithmFactory<AdditionalStartable>() {
                            @Override
                            public AdditionalStartable apply(FishState fishState) {
                                return new AbundanceGatherers(200);

                            }
                        }
                );

            }
        });

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, FILENAME + ".csv").toFile());
        fileWriter.write("run,year,variable,value\n");
        fileWriter.flush();

        while(runner.getRunsDone()<100) {

            StringBuffer tidy = new StringBuffer();
            runner.run(tidy);
            fileWriter.write(tidy.toString());
            fileWriter.flush();
        }
        fileWriter.close();
    }
}
