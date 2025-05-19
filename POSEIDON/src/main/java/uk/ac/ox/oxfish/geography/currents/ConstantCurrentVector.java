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

package uk.ac.ox.oxfish.geography.currents;

import sim.util.Double2D;
import sim.util.Int2D;

/**
 * a very simple, "dummy" current vector that just always returns the same vector at all positions
 */
public class ConstantCurrentVector implements CurrentVectors {


    private final Double2D currentVector;

    /**
     * the boundaries of the map, which should mean the boundaries of the currents field
     */
    private final int gridHeight;

    /**
     * the boundaries of the map, which should mean the boundaries of the currents field
     */
    private final int gridWidth;


    public ConstantCurrentVector(double xCurrent, double yCurrent, int gridHeight, int gridWidth) {
        this.currentVector = new Double2D(xCurrent, yCurrent);
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
    }

    @Override
    public int getGridHeight() {
        return gridHeight;
    }

    @Override
    public int getGridWidth() {
        return gridWidth;
    }

    @Override
    public Double2D getVector(int step, Int2D location) {
        return currentVector;
    }
}
