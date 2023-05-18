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

import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public class AltitudeOutput implements OutputPlugin {


    final private DoubleGrid2D altitude;
    private String fileName = "altitude_map.csv";


    public AltitudeOutput(NauticalMap map) {
        altitude = new DoubleGrid2D(map.getWidth(), map.getHeight());
        for (SeaTile seaTile : map.getAllSeaTilesAsList()) {
            altitude.set(seaTile.getGridX(), seaTile.getGridY(), seaTile.getAltitude());
        }

    }


    @Override
    public void reactToEndOfSimulation(FishState state) {

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
        StringBuilder builder = new StringBuilder(2000);
        builder.append("x,y,z").append("\n");
        for (int x = 0; x < altitude.getWidth(); x++)
            for (int y = 0; y < altitude.getHeight(); y++)
                builder.append(x).append(",").append(y).append(",").append(altitude.get(x, y)).append("\n");

        return builder.toString();

    }
}
