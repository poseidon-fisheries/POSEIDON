/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.data.webviz.heatmaps;

import static java.util.stream.IntStream.range;

public class AveragingAtIntervalTimestepsBuilder extends AbstractTimestepBuilder {

    private final int interval;

    private int numObservations = 0;
    private double[] cellValues = null;

    public AveragingAtIntervalTimestepsBuilder(int interval) { this.interval = interval; }

    @Override public void add(Timestep timestep) {

        numObservations++;
        if (numObservations == 1)
            // The first observation resets the array of cell values
            cellValues = timestep.getCellValues();
        else
            // Afterwards, we update the array with the iterative average
            range(0, cellValues.length).forEach(i ->
                cellValues[i] += (timestep.getCellValues()[i] - cellValues[i]) / numObservations
            );

        // When we have all the observations we need, we store the aggregate timestep
        // and reset the number of observations in order to start anew on the next call
        if (numObservations == interval) {
            builder.add(new Timestep(timestep.getTimeInDays(), cellValues));
            numObservations = 0;
        }
    }

}
