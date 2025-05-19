/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.regs.mpa;

import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * A simple MPA rectangle to be constructed
 * Created by carrknight on 11/18/15.
 */
public class StartingMPA {

    private int topLeftX;

    private int topLeftY;

    private int width;

    private int height;

    public StartingMPA() {
    }

    public StartingMPA(int topLeftX, int topLeftY, int width, int height) {
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.width = width;
        this.height = height;
    }

    public static void quicklyAddMPAToSeaTile(SeaTile tile) {
        tile.assignMpa(NauticalMap.MPA_SINGLETON);
    }

    public MasonGeometry buildMPA(NauticalMap map) {


        MasonGeometry geometry = NauticalMap.MPA_SINGLETON;

        for (SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {
            if (seaTile.getGridX() >= topLeftX &&
                seaTile.getGridX() <= topLeftX + width &&
                seaTile.getGridY() >= topLeftY &&
                seaTile.getGridY() <= topLeftY + height)
                seaTile.assignMpa(geometry);
        }

        return geometry;
    }

    public int getTopLeftX() {
        return topLeftX;
    }

    public void setTopLeftX(int topLeftX) {
        this.topLeftX = topLeftX;
    }

    public int getTopLeftY() {
        return topLeftY;
    }

    public void setTopLeftY(int topLeftY) {
        this.topLeftY = topLeftY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
