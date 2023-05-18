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

import com.google.common.base.Joiner;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.FishingRecord;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;

/**
 * Listens to trips, keeps a count of how many times each area was visited.
 * Depending on flag this can either be counting trips or hours spent fishing
 * Created by carrknight on 1/10/17.
 */
public class DiscretizationHistogrammer implements TripListener, OutputPlugin {

    private final MapDiscretization discretization;


    private final Integer[] counts;

    /**
     * when this is true, we keep track of how many hours were fished in each area. If this is false we keep track of how many trips
     * were done targeting an area.
     */
    private final boolean effortCounter;


    private String fileName = "location_histogram.csv";

    public DiscretizationHistogrammer(MapDiscretization discretization, boolean effortCounter) {
        this.discretization = discretization;
        this.effortCounter = effortCounter;
        counts = new Integer[discretization.getNumberOfGroups()];
        for (int i = 0; i < counts.length; i++)
            counts[i] = 0;
    }

    @Override
    public void reactToEndOfSimulation(FishState state) {

    }

    /**
     * turn most fished tile into a group number and add 1 to the list
     *
     * @param record
     * @param fisher
     */
    @Override
    public void reactToFinishedTrip(TripRecord record, Fisher fisher) {
        if (!effortCounter) {
            SeaTile mostFishedTileInTrip = record.getMostFishedTileInTrip();
            if (mostFishedTileInTrip != null && discretization.getGroup(mostFishedTileInTrip) != null)
                counts[discretization.getGroup(mostFishedTileInTrip)]++;
        } else {
            for (Map.Entry<SeaTile, FishingRecord> effort : record.getFishingRecords()) {
                counts[discretization.getGroup(effort.getKey())] += effort.getValue().getHoursSpentFishing();
            }
        }
    }


    /**
     * Getter for property 'discretization'.
     *
     * @return Value for property 'discretization'.
     */
    public MapDiscretization getDiscretization() {
        return discretization;
    }

    /**
     * Getter for property 'counts'.
     *
     * @return Value for property 'counts'.
     */
    public Integer[] getCounts() {
        return counts;
    }

    /**
     * Getter for property 'effortCounter'.
     *
     * @return Value for property 'effortCounter'.
     */
    public boolean isEffortCounter() {
        return effortCounter;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter for property 'fileName'.
     *
     * @param fileName Value to set for property 'fileName'.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String composeFileContents() {
        return toString();
    }

    @Override
    public String toString() {
        Joiner joiner = Joiner.on(",").skipNulls();
        return joiner.join(counts);
    }
}
