/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.data;

import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.FishingRecord;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;

/**
 * very similar to discretization histogrammer but this one works over map cells rather than map groups!
 */
public class TowOutput implements OutputPlugin, TripListener {


    private static final long serialVersionUID = 3904451565853395807L;
    final private DoubleGrid2D tows;
    private String fileName = "tow_map.csv";


    public TowOutput(final NauticalMap map) {
        tows = new DoubleGrid2D(map.getWidth(), map.getHeight());

    }

    /**
     * turn most fished tile into a group number and add 1 to the list
     *
     * @param record
     * @param fisher
     */
    @Override
    public void reactToFinishedTrip(final TripRecord record, final Fisher fisher) {

        for (final Map.Entry<SeaTile, FishingRecord> effort : record.getFishingRecords()) {
            final SeaTile tile = effort.getKey();

            tows.set(tile.getGridX(), tile.getGridY(),
                1 + tows.get(
                    tile.getGridX(),
                    tile.getGridY()
                )
            );

        }

    }


    @Override
    public void reactToEndOfSimulation(final FishState state) {

    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String composeFileContents() {
        final StringBuilder builder = new StringBuilder(2000);
        builder.append("x,y,z").append("\n");
        for (int x = 0; x < tows.getWidth(); x++)
            for (int y = 0; y < tows.getHeight(); y++)
                builder.append(x).append(",").append(y).append(",").append(tows.get(x, y)).append("\n");

        return builder.toString();
    }
}
