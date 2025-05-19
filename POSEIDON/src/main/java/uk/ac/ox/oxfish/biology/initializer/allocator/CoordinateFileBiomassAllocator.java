/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.nio.file.Path;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;

public class CoordinateFileBiomassAllocator extends FileBiomassAllocator implements BiomassAllocator {


    /**
     * a link containing for each seatile an object computing the current average
     * (check for nulls)
     */
    private final HashMap<SeaTile, DoubleSummaryStatistics> observations = new HashMap<>();


    public CoordinateFileBiomassAllocator(final Path csvFile, final boolean inputFileHasHeader) {
        super(csvFile, inputFileHasHeader);
    }

    @Override
    protected void observePoint(
        final NauticalMap map,
        final Double currentX,
        final Double currentY,
        final Double nextValue
    ) {
        final SeaTile tile = map.getSeaTile(new Coordinate(currentX, currentY));
        if (tile == null) //if you are off the depth map, ignore!
            return;
        //get the object computing the averages, or put one in the map if
        //this is the first time we see this tile
        DoubleSummaryStatistics statistics = observations.get(tile);
        if (statistics == null) {
            statistics = new DoubleSummaryStatistics();
            observations.put(tile, statistics);
        }
        assert observations.containsKey(tile); //should be in, now!
        statistics.accept(nextValue);
    }


    protected double allocateNumerically(final SeaTile tile, final NauticalMap map, final MersenneTwisterFast random) {
        final DoubleSummaryStatistics statistic = observations.get(tile);
        if (statistic == null)
            return Double.NaN;
        else
            return statistic.getAverage();
    }


}
