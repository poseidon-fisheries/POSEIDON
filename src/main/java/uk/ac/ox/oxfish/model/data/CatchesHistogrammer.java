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

package uk.ac.ox.oxfish.model.data;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyTimeSeries;
import uk.ac.ox.oxfish.model.market.NThresholdsMarket;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CatchesHistogrammer implements OutputPlugin {


    private StringBuilder builder = new StringBuilder().append("species,bin,length,weight,catches").append("\n");

    @Override
    public void reactToEndOfSimulation(FishState state) {

        for(Species species : state.getSpecies()) {
            String name = species.getName();
            for (int bin = 0; bin < species.getNumberOfBins(); bin++)
            {
                //get average weight and length first
                double weight = 0;
                double length = 0;
                for(int subdivision =0; subdivision<species.getNumberOfSubdivisions(); subdivision++)
                {
                    weight += species.getWeight(subdivision,bin);
                    length += species.getLength(subdivision,bin);
                }
                weight/=(double)species.getNumberOfSubdivisions();
                length/=(double)species.getNumberOfSubdivisions();

                //sum up all the catches

                Stream<Double> stream = state.getDailyDataSet().getColumn(
                        species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME +
                                NThresholdsMarket.AGE_BIN_PREFIX + bin).stream();
                double catches = stream.collect(Collectors.summarizingDouble(Double::doubleValue)).getSum();
                //now catches are in KG, but we want frequency, so re-divide
                catches = catches/weight;

                //append line
                builder.append(name).append(",");
                builder.append(bin).append(",");
                builder.append(length).append(",");
                builder.append(weight).append(",");
                builder.append(catches).append("\n");

            }
        }
    }

    @Override
    public String getFileName() {
        return "catches_histogram.csv";
    }

    @Override
    public String composeFileContents() {
        return builder.toString();
    }
}
