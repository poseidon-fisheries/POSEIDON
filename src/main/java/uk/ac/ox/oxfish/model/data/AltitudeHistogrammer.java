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

package uk.ac.ox.oxfish.model.data;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.util.DoubleSummaryStatistics;

public class AltitudeHistogrammer implements OutputPlugin{




    private String fileName = "altitude_histogram.csv";

    final private Integer[] altitude;


    public AltitudeHistogrammer(MapDiscretization discretization) {
        altitude = new Integer[discretization.getNumberOfGroups()];
        for(int i=0; i<altitude.length; i++) {
            DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
            for (SeaTile seaTile : discretization.getGroup(i)) {
                statistics.accept(seaTile.getAltitude());
            }
            altitude[i] = (int)statistics.getAverage();
        }
    }


    @Override
    public void reactToEndOfSimulation(FishState state) {

    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String composeFileContents() {
        return toString();
    }

    @Override
    public String toString() {
        Joiner joiner = Joiner.on(",").skipNulls();
        return joiner.join(altitude);
    }

    /**
     * Setter for property 'fileName'.
     *
     * @param fileName Value to set for property 'fileName'.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
