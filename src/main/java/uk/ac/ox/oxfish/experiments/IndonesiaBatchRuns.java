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

import java.io.IOException;
import java.nio.file.Paths;

public class IndonesiaBatchRuns {


    public static final String FILENAME = "perfectt3";

    public static void main(String[] args) throws IOException {


        BatchRunner runner = new BatchRunner(
                Paths.get("docs",
                          "20180516 medium_713",
                          "adding_large",
                          FILENAME+".yaml"),
                2,
                Lists.newArrayList(
                        "Pristipomoides multidens Landings of small",
                        "Lutjanus malabaricus Landings of small",
                        "Pristipomoides typus Landings of small",
                        "Epinephelus areolatus Landings of small",
                        "Lutjanus johnii Landings of small",
                        "Others Landings of small",
                        "Average Cash-Flow of small",
                        "Average Number of Trips of small",
                        "Average Trip Duration of small",
                        "Average Distance From Port of small",
                        //"Total Variable Costs of small",
                        //"Total Earnings of small",

                        "Pristipomoides multidens Landings of big",
                        "Lutjanus malabaricus Landings of big",
                        "Pristipomoides typus Landings of big",
                        "Epinephelus areolatus Landings of big",
                        "Lutjanus johnii Landings of big",
                        "Others Landings of big",
                        "Average Cash-Flow of big",
                        "Average Number of Trips of big",
                        "Average Trip Duration of big",
                        "Average Distance From Port of big"//,
                   //     "Total Variable Costs of big",
                   //     "Total Earnings of big"

                ),
                Paths.get("docs",
                          "20180516 medium_713",
                          "adding_large",
                          FILENAME),
                null,
                System.currentTimeMillis(),
                -1
        );


        while(runner.getRunsDone()<100)
            runner.run(null);

    }
}
