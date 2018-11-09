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
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class IndonesiaBatchRuns {


    public static final String FILENAME = "ns_200";
    public static final String DIRECTORY = "docs/indonesia_hub/runs/712/sclice2/calibration/results";

    public static void main(String[] args) throws IOException {


        BatchRunner runner = new BatchRunner(
                Paths.get(DIRECTORY,
                          FILENAME + ".yaml"),
                4,
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
                          FILENAME),
                null,
                System.currentTimeMillis(),
                -1
        );


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
